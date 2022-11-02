from dotenv import load_dotenv
import os, subprocess, urllib3, json
import numpy as np
import pandas as pd
import datetime as dt
import logging
import re
import time

import matplotlib.pyplot as plt
import boto3

RESULTS_FILE = "result-requests.csv"

ELAPSED = "elapsed"
THREAD_NAME = "threadName"
TIMESTAMP = "timeStamp"

START_DATETIME = "start_datetime"
END_DATETIME = "end_datetime"
MINIMUM = "minimum"
MAXIMUM = "maximum"
PERCENTILE_99 = "percentile99"
PERCENTILE_95 = "percentile95"
PERCENTILE_90 = "percentile90"
AVERAGE = "average"
OFFLOADING_OPERATIONS = "offloading_operations"
LOCAL_EXECUTIONS = "local_executions"
THROUGHPUT_SECONDS = "throughput_seconds"

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s.%(msecs)03d %(levelname)s %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)

http = urllib3.PoolManager()
all_fog_nodes = []


def locate_vm_ips():

    dotenv_path = wrap_dir(".env")
    load_dotenv(dotenv_path)

    client = boto3.client(
        "ec2",
        aws_access_key_id=os.environ.get("ACCESS_KEY"),
        aws_secret_access_key=os.environ.get("SECRET_KEY"),
        region_name="sa-east-1",
    )
    custom_filter = [{"Name": "tag:Name", "Values": ["fog_node_a"]}]
    response = client.describe_instances(Filters=custom_filter)
    for r in response["Reservations"]:
        for i in r["Instances"]:
            all_fog_nodes.append(i["PublicDnsName"])

    logging.info("IPs for running fog nodes: {}".format(all_fog_nodes))


def update_thresholds_for_virtual_machine(
    cpu_interval, warning_threshold, critical_threshold
):

    # Updates settings on every fog node
    for fog_node in all_fog_nodes:

        logging.info(
            "Updating the CPU collection interval to {}...".format(cpu_interval)
        )
        r = http.request(
            "POST",
            "http://{}:8099/machine-resources".format(fog_node),
            headers={"Content-Type": "application/json"},
            body=json.dumps({"update_interval": cpu_interval}).encode("utf-8"),
        )

        if r.status != 200:
            logging.info("Error updating the CPU interval")
            exit(1)

        logging.info("Authenticating on remote OpenFaaS running on the fog node...\n\n")
        subprocess.call(
            [
                "faas-cli",
                "login",
                "--gateway",
                fog_node + ":8080",
                "--password",
                os.environ.get("OPENFAAS_SECRET"),
            ]
        )

        print("\n\n")
        logging.info(
            "Re-deploying service-executor module on remote fog nod with {} warning threshold and {} critical threshold...".format(
                warning_threshold, critical_threshold
            )
        )
        subprocess.call(
            [
                "faas-cli",
                "--gateway",
                fog_node + ":8080",
                "deploy",
                "-f",
                "service-executor.yml",
                "-e",
                "THRESHOLD_CRITICAL_CPU_USAGE={}".format(critical_threshold),
                "-e",
                "THRESHOLD_WARNING_CPU_USAGE={}".format(warning_threshold),
            ],
            cwd="./functions",
        )


# The user priority configured on JMeter is always the same as the group number
def group_name_from_thread(thread_name):
    association = {
        "1": "5",
        "2": "4",
        "3": "3",
        "4": "2",
        "5": "1"
    }
    group_id = re.findall("Thread\sGroup\s(\d).*", thread_name)[0]
    associated_priority = association[group_id]
    return "User priority {}".format(associated_priority)


def group_name_from_user_priority(user_priority):
    return "User priority {}".format(user_priority)


def analyze_dataset():

    all_data = {}

    df = pd.read_csv(wrap_dir(RESULTS_FILE), delimiter=",")
    for index, row in df.iterrows():

        group_name = group_name_from_thread(row[THREAD_NAME])

        thread_data = all_data.get(group_name)
        if thread_data is None:
            thread_data = {}
            all_data[group_name] = thread_data

        get_array_from_thread_dict(thread_data, ELAPSED).append(row[ELAPSED])
        get_array_from_thread_dict(thread_data, TIMESTAMP).append(row[TIMESTAMP])
        get_array_from_thread_dict(thread_data, START_DATETIME).append(
            dt.datetime.fromtimestamp(row[TIMESTAMP] / 1e3)
        )
        get_array_from_thread_dict(thread_data, END_DATETIME).append(
            dt.datetime.fromtimestamp((row[TIMESTAMP] + row[ELAPSED]) / 1e3)
        )

    return all_data


def update_metric(all_data, start_date_time, metric_name, field_name):
    # The request used for testing purposes uses a health service with priority of '4',
    # therefore we map the user priority with the ranking found in the metrics
    raking_association_with_user_priority = {
        "5": "1",
        "6": "2",
        "7": "3",
        "8": "4",
        "9": "5",
    }

    r = http.request(
        "GET",
        "http://{}:9001/metrics/{}?since={}".format(
            all_fog_nodes[0], metric_name, start_date_time
        ),
        headers={"Content-Type": "application/json"},
    )

    if r.status != 200:
        logging.info("Error collecting offloading metrics during tests")
        exit(1)

    response_json = json.loads(r.data)["response"]

    for index in range(len(response_json)):
        current_json = response_json[index]
        ranking = current_json["ranking"]
        user_priority = raking_association_with_user_priority[ranking]

        thread_data = all_data[group_name_from_user_priority(user_priority)]
        get_array_from_thread_dict(thread_data, field_name).append(
            current_json["datetime"]
        )


def update_with_offloading_operations(all_data, start_date_time):
    return update_metric(all_data, start_date_time, "offloading", OFFLOADING_OPERATIONS)


def update_with_local_executions(all_data, start_date_time):
    return update_metric(all_data, start_date_time, "local-execution", LOCAL_EXECUTIONS)


def update_with_summary(all_data):
    for key, thread_data in all_data.items():
        thread_data[THROUGHPUT_SECONDS] = throughput_seconds(
            thread_data[START_DATETIME], thread_data[END_DATETIME]
        )
        thread_data[MINIMUM] = np.amin(thread_data[ELAPSED])
        thread_data[MAXIMUM] = np.amax(thread_data[ELAPSED])
        thread_data[PERCENTILE_99] = np.percentile(thread_data[ELAPSED], 99)
        thread_data[PERCENTILE_95] = np.percentile(thread_data[ELAPSED], 95)
        thread_data[PERCENTILE_90] = np.percentile(thread_data[ELAPSED], 90)
        thread_data[AVERAGE] = np.average(thread_data[ELAPSED])


def run_test_scenario(test_file):

    start_date_time = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    logging.info("Start date time: {}".format(start_date_time))

    invoke_jmeter_test(test_file)

    all_data = analyze_dataset()


    # time.sleep(5)

    update_with_offloading_operations(all_data, start_date_time)
    update_with_local_executions(all_data, start_date_time)
    update_with_summary(all_data)

    print_summary(all_data)
    plot_chart_response_time(all_data)
    plot_chart_cpu_usage(start_date_time)


def wrap_dir(file):
    return "./scripts/evaluation/" + file


def invoke_jmeter_test(test_file):

    try:
        os.remove(wrap_dir("jmeter.log"))
    except OSError:
        pass

    try:
        os.remove(wrap_dir(RESULTS_FILE))
    except OSError:
        pass

    print("\n\n")
    logging.info("Invoking JMeter...")
    subprocess.call(
        [
            "jmeter",
            "-n",
            "-t",
            wrap_dir(test_file),
            "-Jhost={}".format(all_fog_nodes[0]),
            "-l",
            wrap_dir(RESULTS_FILE),
            "-L",
            "DEBUG",
        ]
    )


def throughput_seconds(start_datetime_for_thread, end_datetime_for_thread):
    min_datetime = min(start_datetime_for_thread)
    max_datetime = max(end_datetime_for_thread)
    total_requests = len(start_datetime_for_thread)
    duration_seconds = (max_datetime - min_datetime).total_seconds()
    return total_requests / duration_seconds


def get_array_from_thread_dict(thread_data, field_name):
    list_for_thread = thread_data.get(field_name)
    if list_for_thread is None:
        list_for_thread = []
        thread_data[field_name] = list_for_thread

    return list_for_thread


def print_summary(all_data):
    logging.info("Test summary")
    for key, thread_data in sorted(all_data.items()):
        logging.info("-----------")
        logging.info("# Group: {}".format(key))
        logging.info("##  Throughput/sec: {}".format(thread_data[THROUGHPUT_SECONDS]))
        logging.info("##         Minimum: {}".format(thread_data[MINIMUM]))
        logging.info("##         Maximum: {}".format(thread_data[MAXIMUM]))
        logging.info("## 99th percentile: {}".format(thread_data[PERCENTILE_99]))
        logging.info("## 95th percentile: {}".format(thread_data[PERCENTILE_95]))
        logging.info("## 90th percentile: {}".format(thread_data[PERCENTILE_90]))
        logging.info("##         Average: {}".format(thread_data[AVERAGE]))
        logging.info(
            "## Tot.offloadings: {}".format(len(get_array_from_thread_dict(thread_data, OFFLOADING_OPERATIONS)))
        )
        logging.info("## Tot.local exec: {}".format(len(get_array_from_thread_dict(thread_data, LOCAL_EXECUTIONS))))
        logging.info("")


def plot_chart_response_time(all_data):
    legend = []
    for key, thread_data in sorted(all_data.items()):
        legend.append(key)
        plt.plot(thread_data[START_DATETIME], thread_data[ELAPSED])

    plt.title("Response time for all threads")
    plt.xlabel("Timestamp")
    plt.ylabel("Response time")
    plt.legend(legend)
    plt.show()


def plot_chart_cpu_usage(start_date_time):

    # Collects CPU usage during tests
    r = http.request(
        "GET",
        "http://{}:9001/metrics/cpu?since={}".format(all_fog_nodes[0], start_date_time),
        headers={"Content-Type": "application/json"},
    )

    if r.status != 200:
        logging.info("Error collecting CPU usage during tests")
        exit(1)

    response_json = json.loads(r.data)["response"]

    all_datetimes = []
    all_cpus = []
    for index in range(len(response_json)):
        current_json = response_json[index]
        all_datetimes.append(current_json["datetime"])
        all_cpus.append(current_json["cpu"])

    plt.plot(all_datetimes, all_cpus)

    plt.title("CPU Usage during tests")
    plt.xlabel("Timestamp")
    plt.ylabel("CPU Usage")
    plt.show()


if __name__ == "__main__":

    locate_vm_ips()
    update_thresholds_for_virtual_machine(
        cpu_interval=2, warning_threshold=30, critical_threshold=95
    )
    run_test_scenario(test_file="scenario-1.jmx")

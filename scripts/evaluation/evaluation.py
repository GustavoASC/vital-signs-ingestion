from urllib import response
from dotenv import load_dotenv
import os, subprocess, urllib3, json
import numpy as np
import pandas as pd
import datetime as dt
import logging

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


def analyze_dataset():

    all_data = {}

    df = pd.read_csv(wrap_dir(RESULTS_FILE), delimiter=",")
    for index, row in df.iterrows():

        data_for_thread = all_data.get(row[THREAD_NAME])
        if data_for_thread is None:
            data_for_thread = {}
            all_data[row[THREAD_NAME]] = data_for_thread

        get_array_from_thread_dict(data_for_thread, ELAPSED).append(row[ELAPSED])
        get_array_from_thread_dict(data_for_thread, TIMESTAMP).append(row[TIMESTAMP])
        get_array_from_thread_dict(data_for_thread, START_DATETIME).append(
            dt.datetime.fromtimestamp(row[TIMESTAMP] / 1e3)
        )
        get_array_from_thread_dict(data_for_thread, END_DATETIME).append(
            dt.datetime.fromtimestamp((row[TIMESTAMP] + row[ELAPSED]) / 1e3)
        )

    return all_data


def update_with_summary(all_data):
    for key, data_for_thread in all_data.items():
        data_for_thread[THROUGHPUT_SECONDS] = throughput_seconds(
            data_for_thread[START_DATETIME], data_for_thread[END_DATETIME]
        )
        data_for_thread[MINIMUM] = np.amin(data_for_thread[ELAPSED])
        data_for_thread[MAXIMUM] = np.amax(data_for_thread[ELAPSED])
        data_for_thread[PERCENTILE_99] = np.percentile(data_for_thread[ELAPSED], 99)
        data_for_thread[PERCENTILE_95] = np.percentile(data_for_thread[ELAPSED], 95)
        data_for_thread[PERCENTILE_90] = np.percentile(data_for_thread[ELAPSED], 90)
        data_for_thread[AVERAGE] = np.average(data_for_thread[ELAPSED])


def run_test_scenario(test_file):

    start_date_time = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    invoke_jmeter_test(test_file)

    all_data = analyze_dataset()
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


def get_array_from_thread_dict(data_for_thread, field_name):
    list_for_thread = data_for_thread.get(field_name)
    if list_for_thread is None:
        list_for_thread = []
        data_for_thread[field_name] = list_for_thread

    return list_for_thread


def print_summary(all_data):
    logging.info("Test summary")
    for key, data_for_thread in sorted(all_data.items()):
        logging.info("-----------")
        logging.info("# Thread: {}".format(key))
        logging.info(
            "##  Throughput/sec: {}".format(data_for_thread[THROUGHPUT_SECONDS])
        )
        logging.info("##         Minimum: {}".format(data_for_thread[MINIMUM]))
        logging.info("##         Maximum: {}".format(data_for_thread[MAXIMUM]))
        logging.info("## 99th percentile: {}".format(data_for_thread[PERCENTILE_99]))
        logging.info("## 95th percentile: {}".format(data_for_thread[PERCENTILE_95]))
        logging.info("## 90th percentile: {}".format(data_for_thread[PERCENTILE_90]))
        logging.info("##         Average: {}".format(data_for_thread[AVERAGE]))
        logging.info("")


def plot_chart_response_time(all_data):
    legend = []
    for key, data_for_thread in sorted(all_data.items()):
        legend.append(key)
        plt.plot(data_for_thread[START_DATETIME], data_for_thread[ELAPSED])

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

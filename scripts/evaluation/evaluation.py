from dotenv import load_dotenv
import os, subprocess, urllib3, json
import numpy as np
import pandas as pd
import datetime as dt
import logging
import re
import summary
import aws
import plot
import assertions
import metrics

JMETER_RESULTS_FILE = "result-requests.csv"
JMETER_ELAPSED = "elapsed"
JMETER_THREAD_NAME = "threadName"
JMETER_TIMESTAMP = "timeStamp"


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s.%(msecs)03d %(levelname)s %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)

http = urllib3.PoolManager()
all_fog_nodes = []


def update_thresholds_for_virtual_machine(
    cpu_interval, warning_threshold, critical_threshold
):
    for fog_node in all_fog_nodes:
        update_cpu_interval(cpu_interval, fog_node)
        authenticate_openfaas(fog_node)
        deploy_service_executor_openfaas(
            warning_threshold, critical_threshold, fog_node
        )


def deploy_service_executor_openfaas(warning_threshold, critical_threshold, fog_node):
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


def authenticate_openfaas(fog_node):
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


def check_error(response):
    if response.status != 200:
        logging.info("Error collecting offloading metrics during tests")
        exit(1)


def update_cpu_interval(cpu_interval, fog_node):
    logging.info("Updating the CPU collection interval to {}...".format(cpu_interval))
    check_error(
        http.request(
            "POST",
            "http://{}:8099/machine-resources".format(fog_node),
            headers={"Content-Type": "application/json"},
            body=json.dumps({"update_interval": cpu_interval}).encode("utf-8"),
        )
    )


# The user priority configured on JMeter is always the same as the group number
def group_name_from_thread(thread_name):
    association = {"1": "5", "2": "4", "3": "3", "4": "2", "5": "1"}
    group_id = re.findall("Thread\sGroup\s(\d).*", thread_name)[0]
    return association[group_id]


def analyze_dataset():
    all_data = {}
    df = pd.read_csv(wrap_dir(JMETER_RESULTS_FILE), delimiter=",")
    for index, row in df.iterrows():

        thread_data = get_dict_from_dict(
            all_data, group_name_from_thread(row[JMETER_THREAD_NAME])
        )

        get_array_from_dict(thread_data, "elapsed").append(row[JMETER_ELAPSED])
        get_array_from_dict(thread_data, "timestamp").append(row[JMETER_TIMESTAMP])
        get_array_from_dict(thread_data, "start_datetime").append(
            dt.datetime.fromtimestamp(row[JMETER_TIMESTAMP] / 1e3)
        )
        get_array_from_dict(thread_data, "end_datetime").append(
            dt.datetime.fromtimestamp(
                (row[JMETER_TIMESTAMP] + row[JMETER_ELAPSED]) / 1e3
            )
        )

    return all_data


def get_dict_from_dict(data, field_name):
    result_dict = data.get(field_name)
    if result_dict is None:
        result_dict = {}
        data[field_name] = result_dict

    return result_dict


def get_array_from_dict(data, field_name):
    result_array = data.get(field_name)
    if result_array is None:
        result_array = []
        data[field_name] = result_array

    return result_array


def update_with_summary(all_data):
    for user_priority, thread_data in all_data.items():
        thread_data["throughput_seconds"] = throughput_seconds(
            thread_data["start_datetime"], thread_data["end_datetime"]
        )
        thread_data["minimum"] = np.amin(thread_data["elapsed"])
        thread_data["maximum"] = np.amax(thread_data["elapsed"])
        thread_data["percentile_99"] = np.percentile(thread_data["elapsed"], 99)
        thread_data["percentile_95"] = np.percentile(thread_data["elapsed"], 95)
        thread_data["percentile_90"] = np.percentile(thread_data["elapsed"], 90)
        thread_data["percentile_50"] = np.percentile(thread_data["elapsed"], 50)
        thread_data["average"] = np.average(thread_data["elapsed"])

        metrics_summary = metrics.collect_metrics_summary_for_user_priority(
            all_fog_nodes[0], user_priority
        )
        thread_data["total_offloading"] = metrics_summary["total_offloading"]
        thread_data["total_local_execution"] = metrics_summary["total_local_execution"]
        thread_data["total_exceeded_critical_threshold"] = metrics_summary[
            "total_exceeded_critical_threshold"
        ]
        thread_data["total_triggered_heuristic_by_rankings"] = metrics_summary[
            "total_triggered_heuristic_by_rankings"
        ]
        thread_data["total_result_for_heuristic_by_ranking"] = metrics_summary[
            "total_result_for_heuristic_by_ranking"
        ]
        thread_data["total_triggered_heuristic_by_duration"] = metrics_summary[
            "total_triggered_heuristic_by_duration"
        ]
        thread_data["total_result_for_heuristic_by_duration"] = metrics_summary[
            "total_result_for_heuristic_by_duration"
        ]
        thread_data["total_assuming_fallback_for_heuristics"] = metrics_summary[
            "total_assuming_fallback_for_heuristics"
        ]


def run_test_scenario(test_file):

    metrics.clear_metrics(all_fog_nodes[0])

    start_date_time = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    logging.info("Start date time: {}".format(start_date_time))

    invoke_jmeter_test(test_file)

    all_data = analyze_dataset()
    update_with_summary(all_data)

    assertions.make_assertions(all_data)

    summary.print_summary(all_data)
    plot.plot_all_charts(all_fog_nodes, all_data)


def wrap_dir(file):
    return "./scripts/evaluation/" + file


def invoke_jmeter_test(test_file):

    delete("jmeter.log")
    delete(JMETER_RESULTS_FILE)

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
            wrap_dir(JMETER_RESULTS_FILE),
            "-L",
            "DEBUG",
        ]
    )


def delete(file):
    try:
        os.remove(wrap_dir(file))
    except OSError:
        pass


def throughput_seconds(start_datetime_for_thread, end_datetime_for_thread):
    min_datetime = min(start_datetime_for_thread)
    max_datetime = max(end_datetime_for_thread)
    total_requests = len(start_datetime_for_thread)
    duration_seconds = (max_datetime - min_datetime).total_seconds()
    return total_requests / duration_seconds


if __name__ == "__main__":

    load_dotenv(wrap_dir(".env"))
    all_fog_nodes = aws.locate_vm_ips()

    update_thresholds_for_virtual_machine(
        cpu_interval=2, warning_threshold=30, critical_threshold=95
    )
    run_test_scenario(test_file="scenario-1.jmx")

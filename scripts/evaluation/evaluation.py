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
import warm
import properties
from io import StringIO

JMETER_ELAPSED = "elapsed"
JMETER_THREAD_NAME = "threadName"
JMETER_TIMESTAMP = "timeStamp"

http = urllib3.PoolManager()


def _get_results_dir(cpu_interval, warning_threshold, critical_threshold):
    datetime = dt.datetime.today().strftime("%Y-%m-%d-%H:%M:%S")
    return "./results/cpu_interval_{}/warning_{}/critical_{}/{}".format(
        cpu_interval, warning_threshold, critical_threshold, datetime
    )


def _update_thresholds_for_virtual_machine(
    cpu_interval, warning_threshold, critical_threshold, node_name, node_public_ip
):
    _update_cpu_interval(cpu_interval, node_public_ip)
    _authenticate_openfaas(node_public_ip)
    _deploy_service_executor_openfaas(
        warning_threshold, critical_threshold, node_public_ip
    )
    _deploy_topology_mapping_with_node_name(node_name, node_public_ip)


def _deploy_service_executor_openfaas(
    warning_threshold, critical_threshold, node_public_ip
):
    logging.info("\n\n")
    logging.info(
        "Re-deploying service-executor module on remote fog node with {} warning threshold and {} critical threshold...".format(
            warning_threshold, critical_threshold
        )
    )
    subprocess.call(
        [
            "faas-cli",
            "--gateway",
            node_public_ip + ":8080",
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


def _deploy_topology_mapping_with_node_name(node_name, node_public_ip):
    logging.info("\n\n")
    logging.info(
        "Re-deploying topology-mapping module on remote fog node with given name {}...".format(
            node_name
        )
    )
    subprocess.call(
        [
            "faas-cli",
            "--gateway",
            node_public_ip + ":8080",
            "deploy",
            "-f",
            "topology-mapping.yml",
            "-e",
            "ALIAS_CURRENT_MACHINE={}".format(node_name),
        ],
        cwd="./functions",
    )


def _authenticate_openfaas(fog_node):
    logging.info("Authenticating on remote OpenFaaS running on the fog node {}...\n\n".format(fog_node))
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


def _check_error(response):
    if response.status != 200:
        logging.info("Error collecting offloading metrics during tests")
        exit(1)


def _update_cpu_interval(cpu_interval, fog_node):
    logging.info("Updating the CPU collection interval to {}...".format(cpu_interval))
    _check_error(
        http.request(
            "POST",
            "http://{}:8099/machine-resources".format(fog_node),
            headers={"Content-Type": "application/json"},
            body=json.dumps({"update_interval": cpu_interval}).encode("utf-8"),
        )
    )


# The user priority configured on JMeter is always the same as the group number
def _group_name_from_thread(thread_name):
    association = {"1": "5", "2": "4", "3": "3", "4": "2", "5": "1"}
    group_id = re.findall("Thread\sGroup\s(\d).*", thread_name)[0]
    return association[group_id]


def _analyze_dataset(response_dataset):
    all_data = {}
    df = pd.read_csv(StringIO(response_dataset), delimiter=",")
    for index, row in df.iterrows():

        thread_data = _get_dict_from_dict(
            all_data, _group_name_from_thread(row[JMETER_THREAD_NAME])
        )

        _get_array_from_dict(thread_data, "elapsed").append(row[JMETER_ELAPSED])
        _get_array_from_dict(thread_data, "timestamp").append(row[JMETER_TIMESTAMP])
        _get_array_from_dict(thread_data, "start_datetime").append(
            dt.datetime.fromtimestamp(row[JMETER_TIMESTAMP] / 1e3)
        )
        _get_array_from_dict(thread_data, "end_datetime").append(
            dt.datetime.fromtimestamp(
                (row[JMETER_TIMESTAMP] + row[JMETER_ELAPSED]) / 1e3
            )
        )

    return _update_with_summary(all_data)


def _get_dict_from_dict(data, field_name):
    result_dict = data.get(field_name)
    if result_dict is None:
        result_dict = {}
        data[field_name] = result_dict

    return result_dict


def _get_array_from_dict(data, field_name):
    result_array = data.get(field_name)
    if result_array is None:
        result_array = []
        data[field_name] = result_array

    return result_array


def _update_with_summary(all_data):
    for user_priority, thread_data in all_data.items():
        thread_data["throughput_seconds"] = _throughput_seconds(
            thread_data["start_datetime"], thread_data["end_datetime"]
        )
        thread_data["minimum"] = np.amin(thread_data["elapsed"])
        thread_data["maximum"] = np.amax(thread_data["elapsed"])
        thread_data["percentile_99"] = np.percentile(thread_data["elapsed"], 99)
        thread_data["percentile_95"] = np.percentile(thread_data["elapsed"], 95)
        thread_data["percentile_90"] = np.percentile(thread_data["elapsed"], 90)
        thread_data["percentile_80"] = np.percentile(thread_data["elapsed"], 80)
        thread_data["percentile_70"] = np.percentile(thread_data["elapsed"], 70)
        thread_data["percentile_60"] = np.percentile(thread_data["elapsed"], 60)
        thread_data["percentile_50"] = np.percentile(thread_data["elapsed"], 50)
        thread_data["average"] = np.average(thread_data["elapsed"])

        thread_data["fog_nodes_data"] = {}
        for fog_node in all_fog_nodes:
            current_fog_node_data = {}
            metrics_summary = metrics.collect_metrics_summary_for_user_priority(
                fog_node["public_ip"], user_priority
            )
            current_fog_node_data["total_offloading"] = metrics_summary[
                "total_offloading"
            ]
            current_fog_node_data["total_local_execution"] = metrics_summary[
                "total_local_execution"
            ]
            current_fog_node_data[
                "total_exceeded_critical_threshold"
            ] = metrics_summary["total_exceeded_critical_threshold"]
            current_fog_node_data[
                "total_triggered_heuristic_by_rankings"
            ] = metrics_summary["total_triggered_heuristic_by_rankings"]
            current_fog_node_data[
                "total_result_for_heuristic_by_ranking"
            ] = metrics_summary["total_result_for_heuristic_by_ranking"]
            current_fog_node_data[
                "total_triggered_heuristic_by_duration"
            ] = metrics_summary["total_triggered_heuristic_by_duration"]
            current_fog_node_data[
                "total_result_for_heuristic_by_duration"
            ] = metrics_summary["total_result_for_heuristic_by_duration"]
            current_fog_node_data[
                "total_assuming_fallback_for_heuristics"
            ] = metrics_summary["total_assuming_fallback_for_heuristics"]

            thread_data["fog_nodes_data"][fog_node["name"]] = current_fog_node_data

    return all_data


#
# Data structure fog user priority:
#
# all_data
#
#   -> Priority 1
#      - Data collected from JMeter's CSV for this priority
#      - Summary (percentiles etc) computed based on the response of JMeter
#      -> Fog nodes list
#         - Metrics for this priority (offloading operations, local executions, etc) on the fog node
#
#   ...
#
#   -> Priority 5
#      - Data collected from JMeter's CSV for this priority
#      - Summary (percentiles etc) computed based on the response of JMeter
#      -> Fog nodes list
#         - Metrics for this priority (offloading operations, local executions, etc) on the fog node
#

#
# Data structure for CPU usage
#
#  -> fog_node_a
#     - Timestamp collected
#     - Used CPU percentage
#
#  ...
#
#  -> fog_node_z
#     - Timestamp collected
#     - Used CPU percentage
#
#


def _run_test_scenario(test_file):

    for fog_node in all_fog_nodes:
        metrics.clear_metrics(fog_node["public_ip"])

    response_dataset = _invoke_jmeter_test(test_file)
    all_data = _analyze_dataset(response_dataset)
    _save_result(response_dataset, results_dir, "jmeter-results.csv")
    _save_result(all_data, results_dir, "analyzed-dataset.json")

    cpu_usage = {}
    for fog_node in all_fog_nodes:
        node_name = fog_node["name"]
        node_public_ip = fog_node["public_ip"]
        cpu_usage[node_name] = metrics.collect_cpu_usage(node_public_ip)

    _save_result(cpu_usage, results_dir, "cpu-usage.json")
    assertions.make_assertions(cpu_usage, all_data)

    summary.print_summary(all_data)
    plot.plot_all_charts(results_dir, cpu_usage, all_data)


def _save_result(data, result_dir, filename):
    with open(result_dir + "/" + filename, "w") as f:
        f.write(str(data))


def _wrap_dir(file):
    return "./scripts/evaluation/" + file


def _invoke_jmeter_test(test_file):

    logging.info("\n\n")
    logging.info("Invoking JMeter on remote edge node...")
    logging.info("Edge node public IP: {}".format(all_edge_nodes[0]["public_ip"]))
    logging.info("Fog node private IP: {}".format(all_fog_nodes[0]["private_ip"]))

    r = http.request(
        "POST",
        "http://{}:9002/invoke-test-plan".format(all_edge_nodes[0]["public_ip"]),
        headers={"Content-Type": "application/json"},
        body=json.dumps(
            {"target_fog_node": all_fog_nodes[0]["private_ip"], "test_plan": test_file}
        ).encode("utf-8"),
    )

    _check_error(r)
    return r.data.decode("UTF-8")


def _throughput_seconds(start_datetime_for_thread, end_datetime_for_thread):
    min_datetime = min(start_datetime_for_thread)
    max_datetime = max(end_datetime_for_thread)
    total_requests = len(start_datetime_for_thread)
    duration_seconds = (max_datetime - min_datetime).total_seconds()
    return total_requests / duration_seconds


if __name__ == "__main__":

    load_dotenv(_wrap_dir(".env"))

    cpu_interval = [0.5, 1, 2]
    warning_thresholds = [40, 45, 50, 55, 60, 65, 70, 75]
    critical_thresholds = [85, 90, 95, 99]

    for current_cpu_interval in cpu_interval:
        for current_warning_threshold in warning_thresholds:
            for current_critical_threshold in critical_thresholds:

                settings = {
                    "cpu_interval": current_cpu_interval,
                    "warning_threshold": current_warning_threshold,
                    "critical_threshold": current_critical_threshold,
                }

                results_dir = _get_results_dir(
                    settings["cpu_interval"],
                    settings["warning_threshold"],
                    settings["critical_threshold"],
                )
                os.makedirs(results_dir)

                logging.basicConfig(
                    filename="{}/log.txt".format(results_dir),
                    force=True,
                    level=logging.INFO,
                    format="%(asctime)s.%(msecs)03d %(levelname)s %(message)s",
                    datefmt="%Y-%m-%d %H:%M:%S",
                )

                all_fog_nodes = aws.locate_vm_data_with_name("fog_node_*")
                all_edge_nodes = aws.locate_vm_data_with_name("edge_node_*")

                properties.update_properties(
                    fog_nodes=all_fog_nodes,
                    cloud_api_adapter_url="https://x7fusq6sruwliycun2bdnfbx2e0iobzz.lambda-url.eu-west-2.on.aws/",
                )

                for fog_node in all_fog_nodes:
                    _update_thresholds_for_virtual_machine(
                        settings["cpu_interval"],
                        settings["warning_threshold"],
                        settings["critical_threshold"],
                        fog_node["name"],
                        fog_node["public_ip"],
                    )

                for fog_node in all_fog_nodes:
                    node_public_ip = fog_node["public_ip"]
                    warm.warmup_functions(node_public_ip)

                _run_test_scenario(test_file="scenario-1.jmx")

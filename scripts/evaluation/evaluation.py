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
import async_results
import properties
import utils
from io import StringIO
import time
from threading import Thread

JMETER_ELAPSED = "elapsed"
JMETER_THREAD_NAME = "threadName"
JMETER_TIMESTAMP = "timeStamp"

EXPERIMENT_ITERATIONS = 1
ASSERTION_ENABLED = False
ASYNC_PROCESSING = True

http = urllib3.PoolManager()


def _get_results_dir(_threads_cpu_collection, cpu_interval, warning_cpu_threshold, critical_cpu_threshold, critical_mem_threshold):
    datetime = dt.datetime.today().strftime("%Y-%m-%d-%H:%M:%S")
    return "./results/smoothed/cpu_interval_{}/warning_cpu_{}/critical_cpu_{}/critical_mem_{}/{}".format(
        cpu_interval, warning_cpu_threshold, critical_cpu_threshold, critical_mem_threshold, datetime
    )


def _update_thresholds_for_virtual_machine(
    threads_cpu_collection, cpu_interval, warning_cpu_threshold, critical_cpu_threshold, critical_mem_threshold, node_name, node_public_ip
):
    _update_cpu_interval(threads_cpu_collection, cpu_interval, node_public_ip)
    _authenticate_openfaas(node_public_ip)
    _deploy_service_executor_openfaas(
        warning_cpu_threshold, critical_cpu_threshold, critical_mem_threshold, node_public_ip
    )
    _deploy_topology_mapping_with_node_name(node_name, node_public_ip)
    _deploy_additional_functions(node_name, node_public_ip)


def _deploy_service_executor_openfaas(
    warning_cpu_threshold, critical_cpu_threshold, critical_mem_threshold, node_public_ip
):
    logging.info("\n\n")
    logging.info(
        "Re-deploying service-executor module on remote fog node with {} warning cpu threshold, {} critical cpu threshold, and {} critical mem threshold...".format(
            warning_cpu_threshold, critical_cpu_threshold, critical_mem_threshold
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
            "THRESHOLD_CRITICAL_MEM_USAGE={}".format(critical_mem_threshold),
            "-e",
            "THRESHOLD_CRITICAL_CPU_USAGE={}".format(critical_cpu_threshold),
            "-e",
            "THRESHOLD_WARNING_CPU_USAGE={}".format(warning_cpu_threshold),
            "-e",
            "RESULTS_URL=http://{}:9095".format(results_fog_node["public_ip"]),
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


def _deploy_additional_functions(node_name, node_public_ip):

    extra_functions = ["ranking-offloading", "duration-offloading", "predictor", "body-temperature-monitor", "heart-failure-predictor"]
    for fn_name in extra_functions:
        logging.info("\n\n")
        logging.info(
            "Re-deploying {} module on remote fog node with given name {}...".format(
                fn_name, node_name
            )
        )
        subprocess.call(
            [
                "faas-cli",
                "--gateway",
                node_public_ip + ":8080",
                "deploy",
                "-f",
                "{}.yml".format(fn_name),
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


def _update_cpu_interval(threads_cpu_collection, cpu_interval, fog_node):
    logging.info("Updating the CPU collection interval to {} seconds with {} threads...".format(cpu_interval, threads_cpu_collection))
    utils.post(f"http://{fog_node}:8099/machine-resources", {"update_interval": cpu_interval, "total_threads": threads_cpu_collection})
    logging.info("Waiting for a while until the CPU collection threads are stable...")
    time.sleep(1)


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
        _get_array_from_dict(thread_data, "start_datetime").append(
            dt.datetime.fromtimestamp(row[JMETER_TIMESTAMP] / 1e3)
        )
        _get_array_from_dict(thread_data, "end_datetime").append(
            dt.datetime.fromtimestamp(
                (row[JMETER_TIMESTAMP] + row[JMETER_ELAPSED]) / 1e3
            )
        )

    return all_data


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
        thread_data["percentile_40"] = np.percentile(thread_data["elapsed"], 40)
        thread_data["percentile_30"] = np.percentile(thread_data["elapsed"], 30)
        thread_data["percentile_20"] = np.percentile(thread_data["elapsed"], 20)
        thread_data["percentile_10"] = np.percentile(thread_data["elapsed"], 10)
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
                "total_exceeded_critical_cpu_threshold"
            ] = metrics_summary["total_exceeded_critical_cpu_threshold"]
            current_fog_node_data[
                "total_exceeded_critical_mem_threshold"
            ] = metrics_summary["total_exceeded_critical_mem_threshold"]
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

    if ASYNC_PROCESSING:
        async_results.clear_all_results(results_fog_node["public_ip"])

    jmeter_results = _invoke_test_edge_nodes(test_file)

    if ASYNC_PROCESSING:
        response = async_results.collect_async_results_awaiting(results_fog_node["public_ip"])
        all_data = async_results.analyze_dataset(response)

        _save_result(response, results_dir, "async-response.json")
    else:
        all_data = _analyze_dataset(jmeter_results)

    all_data = _update_with_summary(all_data)
    _save_result(jmeter_results, results_dir, "jmeter-results.csv")
    _save_result(all_data, results_dir, "analyzed-dataset.json")

    cpu_usage = {}
    mem_usage = {}
    for fog_node in all_fog_nodes:
        node_name = fog_node["name"]
        node_public_ip = fog_node["public_ip"]
        
        cpu_usage[node_name] = metrics.collect_cpu_usage(node_public_ip)
        _save_result(cpu_usage[node_name], results_dir, "cpu-usage_{}.json".format(node_name))
        
        mem_usage[node_name] = metrics.collect_mem_usage(node_public_ip)
        _save_result(mem_usage[node_name], results_dir, "mem-usage_{}.json".format(node_name))

    if ASSERTION_ENABLED:
        assertions.make_assertions(cpu_usage, all_data)

    summary.print_summary(all_data)
    plot.plot_all_charts(results_dir, cpu_usage, mem_usage, all_data)


def _save_result(data, result_dir, filename):
    with open(result_dir + "/" + filename, "w") as f:
        f.write(str(data))


def _wrap_dir(file):
    return "./scripts/evaluation/" + file

def _invoke_test_thread(test_file, edge_node, service_executor_url, results, index_results):

    results_public_ip = results_fog_node["public_ip"]
    request_payload = {
                "service_executor_url": f"http://{service_executor_url}:8080",
                "async_results_url": f"http://{results_public_ip}:9095",
                "test_plan": test_file,
                "total_user_priorities": 5,
                "threads_per_priority": 2,
                "vital_signs_per_thread": 1000,
                "health_services": [
                    "heart-failure-predictor",
                    "body-temperature-monitor"
                ]
            }

    logging.info("\n\n")
    logging.info("Invoking test on remote edge node...")
    logging.info(f"Request payload: {json.dumps(request_payload)}")

    r = http.request(
        "POST",
        f"http://{edge_node}:9002/invoke-test-plan",
        headers={"Content-Type": "application/json"},
        body=json.dumps(request_payload).encode("utf-8"),
    )

    _check_error(r)
    results[index_results] = (r.data.decode("UTF-8"))

def _invoke_test_edge_nodes(test_file):

    results = [None] * 4

    first_execution = Thread(target=_invoke_test_thread, args=(test_file, edge_node_a["public_ip"], fog_node_a["public_ip"], results, 0,))
    first_execution.start()

    second_execution = Thread(target=_invoke_test_thread, args=(test_file, edge_node_b["public_ip"], fog_node_a["public_ip"], results, 1,))
    second_execution.start()

    third_execution = Thread(target=_invoke_test_thread, args=(test_file, edge_node_c["public_ip"], fog_node_b["public_ip"], results, 2,))
    third_execution.start()

    fourth_execution = Thread(target=_invoke_test_thread, args=(test_file, edge_node_d["public_ip"], fog_node_b["public_ip"], results, 3,))
    fourth_execution.start()

    first_execution.join()
    second_execution.join()
    third_execution.join()
    fourth_execution.join()
    return results[0]


def _throughput_seconds(start_datetime_for_thread, end_datetime_for_thread):
    min_datetime = min(start_datetime_for_thread)
    max_datetime = max(end_datetime_for_thread)
    total_requests = len(start_datetime_for_thread)
    duration_seconds = (max_datetime - min_datetime).total_seconds()
    return total_requests / duration_seconds


if __name__ == "__main__":

    load_dotenv(_wrap_dir(".env"))

    threads_cpu_collection = [1]
    cpu_interval = [1]
    warning_cpu_thresholds = [60]
    critical_cpu_thresholds = [98]
    critical_mem_thresholds = [100]
    
    for i in range(EXPERIMENT_ITERATIONS):
        for current_thread_cpu_collection in threads_cpu_collection:
            for current_cpu_interval in cpu_interval:
                for current_warning_cpu_threshold in warning_cpu_thresholds:
                    for current_critical_cpu_threshold in critical_cpu_thresholds:
                        for current_critical_mem_threshold in critical_mem_thresholds:

                            settings = {
                                "threads_cpu_collection": current_thread_cpu_collection,
                                "cpu_interval": current_cpu_interval,
                                "warning_cpu_threshold": current_warning_cpu_threshold,
                                "critical_cpu_threshold": current_critical_cpu_threshold,
                                "critical_mem_threshold": current_critical_mem_threshold,
                            }

                            results_dir = _get_results_dir(
                                settings["threads_cpu_collection"],
                                settings["cpu_interval"],
                                settings["warning_cpu_threshold"],
                                settings["critical_cpu_threshold"],
                                settings["critical_mem_threshold"],
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
                        
                            edge_node_a = aws.locate_vm_data_with_name("edge_node_a")[0]
                            edge_node_b = aws.locate_vm_data_with_name("edge_node_b")[0]
                            edge_node_c = aws.locate_vm_data_with_name("edge_node_c")[0]
                            edge_node_d = aws.locate_vm_data_with_name("edge_node_d")[0]

                            fog_node_a = aws.locate_vm_data_with_name("fog_node_a")[0]
                            fog_node_b = aws.locate_vm_data_with_name("fog_node_b")[0]

                            results_fog_node = aws.locate_vm_data_with_name("results_fog_node")[0]

                            cloud_api_adapter_url = "https://4il3sb7a77.execute-api.eu-west-2.amazonaws.com"

                            properties.update_properties(
                                 fog_nodes=all_fog_nodes,
                                 cloud_api_adapter_url=cloud_api_adapter_url,
                            )

                            for fog_node in all_fog_nodes:
                                 _update_thresholds_for_virtual_machine(
                                     settings["threads_cpu_collection"],
                                     settings["cpu_interval"],
                                     settings["warning_cpu_threshold"],
                                     settings["critical_cpu_threshold"],
                                     settings["critical_mem_threshold"],
                                     fog_node["name"],
                                     fog_node["public_ip"],
                                )

                            for fog_node in all_fog_nodes:
                                node_public_ip = fog_node["public_ip"]
                                warm.warmup_functions(node_public_ip)

                            _run_test_scenario(test_file="scenario-1")

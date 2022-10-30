from dotenv import load_dotenv
import os, subprocess
import numpy as np
import pandas as pd
import datetime as dt

import matplotlib.pyplot as plt
import boto3

RESULTS_FILE = "result-requests.csv"

ELAPSED_NAME = "elapsed"
THREAD_NAME = "threadName"
TIMESTAMP_NAME = "timeStamp"

DATETIME = "datetime"
PERCENTILE_99 = "percentile99"
PERCENTILE_95 = "percentile95"
AVERAGE = "average"
THROUGHPUT_SECONDS = "throughput_seconds"

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
            print(i["PublicDnsName"])
            all_fog_nodes.append(i["PublicDnsName"])


def update_thresholds_for_virtual_machine(
    cpu_interval, warning_threshold, critical_threshold
):
    pass


def run_test_scenario(test_file):
    invoke_jmeter_test(test_file)

    df = pd.read_csv(wrap_dir(RESULTS_FILE), delimiter=",")

    all_data = {}
    for index, row in df.iterrows():

        data_for_thread = all_data.get(row[THREAD_NAME])
        if data_for_thread is None:
            data_for_thread = {}
            all_data[row[THREAD_NAME]] = data_for_thread

        get_array_from_thread_dict(data_for_thread, ELAPSED_NAME).append(
            row[ELAPSED_NAME]
        )
        get_array_from_thread_dict(data_for_thread, TIMESTAMP_NAME).append(
            row[TIMESTAMP_NAME]
        )
        get_array_from_thread_dict(data_for_thread, DATETIME).append(
            dt.datetime.fromtimestamp(row[TIMESTAMP_NAME] / 1e3)
        )

    for key, data_for_thread in all_data.items():
        data_for_thread[THROUGHPUT_SECONDS] = throughput_seconds(
            data_for_thread[DATETIME]
        )
        data_for_thread[PERCENTILE_99] = np.percentile(
            data_for_thread[TIMESTAMP_NAME], 99
        )
        data_for_thread[PERCENTILE_95] = np.percentile(
            data_for_thread[TIMESTAMP_NAME], 95
        )
        data_for_thread[AVERAGE] = np.average(data_for_thread[TIMESTAMP_NAME])

    plot_chart(all_data)


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


def throughput_seconds(datetime_for_thread):
    min_datetime = min(datetime_for_thread)
    max_datetime = max(datetime_for_thread)
    total_requests = len(datetime_for_thread)
    duration_seconds = (max_datetime - min_datetime).total_seconds()
    return total_requests / duration_seconds


def get_array_from_thread_dict(data_for_thread, field_name):
    list_for_thread = data_for_thread.get(field_name)
    if list_for_thread is None:
        list_for_thread = []
        data_for_thread[field_name] = list_for_thread

    return list_for_thread


def plot_chart(all_data):
    legend = []
    for key, data_for_thread in sorted(all_data.items()):
        legend.append(key)
        plt.plot(data_for_thread[DATETIME], data_for_thread[ELAPSED_NAME])

    plt.title("Response time for all threads")
    plt.xlabel("Timestamp")
    plt.ylabel("Response time")
    plt.legend(legend)
    plt.show()


if __name__ == "__main__":

    locate_vm_ips()
    run_test_scenario(test_file="scenario-1.jmx")

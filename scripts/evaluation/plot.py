import logging, urllib3
import matplotlib.pyplot as plt
import json
import metrics


http = urllib3.PoolManager()


def plot_chart_cpu_usage(fog_node_ip, start_date_time):

    response_json = metrics.collect_cpu_usage(fog_node_ip, start_date_time)

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


def plot_chart_response_time(all_data):
    legend = []
    for key, thread_data in sorted(all_data.items()):
        legend.append(key)
        plt.plot(thread_data["start_datetime"], thread_data["elapsed"])

    plt.title("Response time for all threads")
    plt.xlabel("Timestamp")
    plt.ylabel("Response time")
    plt.legend(legend)
    plt.show()

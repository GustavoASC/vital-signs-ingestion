import urllib3
import matplotlib.pyplot as plt
import metrics
import numpy as np
import datetime as dt


http = urllib3.PoolManager()


def plot_all_charts(all_fog_nodes, all_data):
    _plot_chart_cpu_usage(all_fog_nodes[0])
    _plot_chart_response_time(all_data)
    _plot_offloading_histogram(all_data)
    _plot_local_executions_histogram(all_data)
    _plot_stacked_offloading_local_executions(all_data)
    _plot_throughput(all_data)
    _plot_response_time(all_data)
    _plot_offloading_reasons(all_data)


def _plot_chart_cpu_usage(fog_node_ip):

    response_json = metrics.collect_cpu_usage(fog_node_ip)

    all_datetimes = []
    all_cpus = []
    for index in range(len(response_json)):
        current_json = response_json[index]
        all_cpus.append(current_json["cpu"])
        all_datetimes.append(dt.datetime.fromtimestamp(
            current_json["collection_timestamp"] / 1e3
        ))

    all_datetimes, all_cpus = zip(*sorted(zip(all_datetimes, all_cpus)))

    plt.plot(all_datetimes, all_cpus)
    plt.title("CPU Usage during tests")
    plt.xlabel("Timestamp")
    plt.ylabel("CPU Usage")
    plt.show()


def _plot_chart_response_time(all_data):
    legend = []
    for key, thread_data in sorted(all_data.items()):
        legend.append(key)
        plt.plot(thread_data["start_datetime"], thread_data["elapsed"])

    plt.title("Response time for all threads")
    plt.xlabel("Timestamp")
    plt.ylabel("Response time")
    plt.legend(legend)
    plt.show()


def _plot_offloading_histogram(all_data):
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["total_offloading"])

    plt.bar(x, y, color="b", width=0.4)
    plt.title("Offloading operations according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Offloading operations")
    plt.show()


def _plot_local_executions_histogram(all_data):
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["total_local_execution"])

    plt.bar(x, y, color="g", width=0.4)
    plt.title("Local executions according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Local executions")
    plt.show()


def _plot_stacked_offloading_local_executions(all_data):
    x = []
    local_execution = []
    offloading = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        local_execution.append(thread_data["total_local_execution"])
        offloading.append(thread_data["total_offloading"])

    plt.bar(x, local_execution, color="g", width=0.4, label="Local executions")
    plt.bar(
        x,
        offloading,
        bottom=local_execution,
        color="b",
        width=0.4,
        label="Offloading operations",
    )
    plt.title("Execution operations according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Execution operations")
    plt.legend(loc="lower right")
    plt.show()

def _plot_throughput(all_data):
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["throughput_seconds"])

    plt.bar(x, y, color="g", width=0.4)
    plt.title("Throughput (seconds) according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Throughout (seconds)")
    plt.show()


def _plot_response_time(all_data):
    x = []
    max_response_time = []
    p99_response_time = []
    p95_response_time = []
    p90_response_time = []
    p50_response_time = []
    avg_response_time = []
    min_response_time = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        max_response_time.append(thread_data["maximum"])
        p99_response_time.append(thread_data["percentile_99"])
        p95_response_time.append(thread_data["percentile_95"])
        p90_response_time.append(thread_data["percentile_90"])
        p50_response_time.append(thread_data["percentile_50"])
        avg_response_time.append(thread_data["average"])
        min_response_time.append(thread_data["minimum"])

    x_axis = np.arange(len(x))
    plt.bar(x_axis - 0.3, max_response_time, color="g", width=0.1, label = "Max response time")
    plt.bar(x_axis - 0.2, p99_response_time, color="r", width=0.1, label = "99th percentile")
    plt.bar(x_axis - 0.1, p95_response_time, color="y", width=0.1, label = "95th percentile")
    plt.bar(x_axis,       p90_response_time, color="m", width=0.1, label = "90th percentile")
    plt.bar(x_axis + 0.1, p50_response_time, color="gray", width=0.1, label = "50th percentile")
    plt.bar(x_axis + 0.2, avg_response_time, color="maroon", width=0.1, label = "Avg response time")
    plt.bar(x_axis + 0.3, min_response_time, color="b", width=0.1, label = "Min response time")

    plt.xticks(x_axis, x)
    plt.title("Response time (seconds) according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Response time (seconds)")
    plt.legend(loc="upper right")
    plt.show()


def _plot_offloading_reasons(all_data):
    x = []
    exceeded_critical_threshold = []
    heuristic_by_ranking = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        exceeded_critical_threshold.append(thread_data["total_exceeded_critical_threshold"])
        heuristic_by_ranking.append(thread_data["total_result_for_heuristic_by_ranking"])

    plt.bar(x, exceeded_critical_threshold, color="g", width=0.4, label="Exceeded critical threshold")
    plt.bar(x, heuristic_by_ranking, bottom = exceeded_critical_threshold, color="r", width=0.4, label="Heuristic by ranking")

    plt.title("Reasons for offloading")
    plt.xlabel("User priority")
    plt.ylabel("Offloading operations")
    plt.legend(loc = "upper right")
    plt.show()

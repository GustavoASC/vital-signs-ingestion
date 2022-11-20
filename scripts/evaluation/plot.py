import urllib3
import matplotlib.pyplot as plt
import numpy as np
import datetime as dt


http = urllib3.PoolManager()
save_chart_as_file = True

global_result_dir = ""


def _initialize_chart():
    plt.figure(figsize=(20, 10))


def _finalize_chart(name):
    if save_chart_as_file:
        plt.savefig(global_result_dir + "/" + name, bbox_inches="tight")
        plt.close()
    else:
        plt.show()


def plot_all_charts(result_dir, cpu_usage, all_data):

    global global_result_dir
    global_result_dir = result_dir

    _plot_chart_response_time(all_data)
    _plot_throughput(all_data)
    _plot_response_time(all_data)

    for machine_name, current_cpu_node_data in cpu_usage.items():
        _plot_chart_cpu_usage(machine_name, current_cpu_node_data)

    for priority, thread_data in sorted(all_data.items()):
        for fog_name in thread_data["fog_nodes_data"]:
            _plot_offloading_histogram(all_data, fog_name)
            _plot_local_executions_histogram(all_data, fog_name)
            _plot_stacked_offloading_local_executions(all_data, fog_name)
            _plot_offloading_reasons(all_data, fog_name)


def _plot_chart_cpu_usage(machine_name, cpu_usage):
    _initialize_chart()

    all_datetimes = []
    all_cpus = []
    all_last_observations = []
    for index in range(len(cpu_usage)):
        current_json = cpu_usage[index]
        all_cpus.append(current_json["cpu"])
        all_last_observations.append(current_json["last_cpu_observation"])
        all_datetimes.append(
            dt.datetime.fromtimestamp(current_json["collection_timestamp"] / 1e3)
        )

    all_datetimes, all_cpus, all_last_observations = zip(*sorted(zip(all_datetimes, all_cpus, all_last_observations)))

    plt.plot(all_datetimes, all_cpus, label = "Smoothed CPU")
    plt.plot(all_datetimes, all_last_observations, label = "Last CPU observation", color = "moccasin", linestyle="--")
    plt.title("CPU Usage during tests for machine {}".format(machine_name))
    plt.xlabel("Timestamp")
    plt.ylabel("CPU Usage")
    _finalize_chart("cpu_usage_{}.png".format(machine_name))


def _plot_chart_response_time(all_data):
    _initialize_chart()
    legend = []
    for key, thread_data in sorted(all_data.items()):
        legend.append(key)
        plt.plot(thread_data["start_datetime"], thread_data["elapsed"])

    plt.title("Response time for all threads")
    plt.xlabel("Timestamp")
    plt.ylabel("Response time")
    plt.legend(legend)
    _finalize_chart("response_time.png")


def _plot_offloading_histogram(all_data, fog_name):
    _initialize_chart()
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["fog_nodes_data"][fog_name]["total_offloading"])

    plt.bar(x, y, color="b", width=0.4)
    plt.title("Offloading operations according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Offloading operations")
    _finalize_chart("offloading_operations_{}.png".format(fog_name))


def _plot_local_executions_histogram(all_data, fog_name):
    _initialize_chart()
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["fog_nodes_data"][fog_name]["total_local_execution"])

    plt.bar(x, y, color="g", width=0.4)
    plt.title("Local executions according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Local executions")
    _finalize_chart("local_executions_{}.png".format(fog_name))


def _plot_stacked_offloading_local_executions(all_data, fog_name):
    _initialize_chart()
    x = []
    local_execution = []
    offloading = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        local_execution.append(
            thread_data["fog_nodes_data"][fog_name]["total_local_execution"]
        )
        offloading.append(thread_data["fog_nodes_data"][fog_name]["total_offloading"])

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
    _finalize_chart("execution_operations_{}.png".format(fog_name))


def _plot_throughput(all_data):
    _initialize_chart()
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["throughput_seconds"])

    plt.bar(x, y, color="g", width=0.4)
    plt.title("Throughput (seconds) according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Throughout (seconds)")
    _finalize_chart("throughput.png")


def _plot_response_time(all_data):
    _initialize_chart()
    x = []
    max_response_time = []
    p99_response_time = []
    p95_response_time = []
    p90_response_time = []
    p80_response_time = []
    p70_response_time = []
    p60_response_time = []
    p50_response_time = []
    avg_response_time = []
    min_response_time = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        max_response_time.append(thread_data["maximum"])
        p99_response_time.append(thread_data["percentile_99"])
        p95_response_time.append(thread_data["percentile_95"])
        p90_response_time.append(thread_data["percentile_90"])
        p80_response_time.append(thread_data["percentile_80"])
        p70_response_time.append(thread_data["percentile_70"])
        p60_response_time.append(thread_data["percentile_60"])
        p50_response_time.append(thread_data["percentile_50"])
        avg_response_time.append(thread_data["average"])
        min_response_time.append(thread_data["minimum"])

    x_axis = np.arange(len(x))
    plt.bar(
        x_axis - 0.5, max_response_time, color="g", width=0.1, label="Max response time"
    )
    plt.bar(
        x_axis - 0.4, p99_response_time, color="r", width=0.1, label="99th percentile"
    )
    plt.bar(
        x_axis - 0.3, p95_response_time, color="y", width=0.1, label="95th percentile"
    )
    plt.bar(
        x_axis - 0.2, p90_response_time, color="m", width=0.1, label="90th percentile"
    )
    plt.bar(
        x_axis - 0.1,
        p80_response_time,
        color="brown",
        width=0.1,
        label="80th percentile",
    )
    plt.bar(
        x_axis, p70_response_time, color="black", width=0.1, label="70th percentile"
    )
    plt.bar(
        x_axis + 0.1,
        p60_response_time,
        color="cyan",
        width=0.1,
        label="60th percentile",
    )
    plt.bar(
        x_axis + 0.2,
        p50_response_time,
        color="gray",
        width=0.1,
        label="50th percentile",
    )
    plt.bar(
        x_axis + 0.3,
        avg_response_time,
        color="maroon",
        width=0.1,
        label="Avg response time",
    )
    plt.bar(
        x_axis + 0.4, min_response_time, color="b", width=0.1, label="Min response time"
    )

    plt.xticks(x_axis, x)
    plt.title("Response time (seconds) according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Response time (seconds)")
    plt.legend(loc="upper right")
    _finalize_chart("response_time_with_priority.png")


def _plot_offloading_reasons(all_data, fog_name):
    _initialize_chart()
    x = []
    exceeded_critical_threshold = []
    heuristic_by_ranking = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        exceeded_critical_threshold.append(
            thread_data["fog_nodes_data"][fog_name]["total_exceeded_critical_threshold"]
        )
        heuristic_by_ranking.append(
            thread_data["fog_nodes_data"][fog_name][
                "total_result_for_heuristic_by_ranking"
            ]
        )

    plt.bar(
        x,
        exceeded_critical_threshold,
        color="g",
        width=0.4,
        label="Exceeded critical threshold",
    )
    plt.bar(
        x,
        heuristic_by_ranking,
        bottom=exceeded_critical_threshold,
        color="r",
        width=0.4,
        label="Heuristic by ranking",
    )

    plt.title("Reasons for offloading")
    plt.xlabel("User priority")
    plt.ylabel("Offloading operations")
    plt.legend(loc="upper right")
    _finalize_chart("offloading_reasons_{}.png".format(fog_name))

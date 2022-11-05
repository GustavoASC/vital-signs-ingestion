import urllib3
import matplotlib.pyplot as plt
import metrics


http = urllib3.PoolManager()


def plot_all_charts(all_data):
    # plot_chart_cpu_usage(all_fog_nodes[0], start_date_time)
    _plot_chart_response_time(all_data)
    _plot_offloading_histogram(all_data)
    _plot_local_executions_histogram(all_data)
    _plot_stacked_offloading_local_executions(all_data)


def _plot_chart_cpu_usage(fog_node_ip, start_date_time):

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

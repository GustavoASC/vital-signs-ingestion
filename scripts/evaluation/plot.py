import urllib3
import matplotlib.pyplot as plt
import matplotlib
import numpy as np
import datetime as dt
import datetime
from datetime import timedelta
import matplotlib.dates as mdates


http = urllib3.PoolManager()
save_chart_as_file = True
include_title = True

global_result_dir = ""


def _initialize_chart():
    plt.figure(figsize=(10, 5))
    _increase_font_size()


def _finalize_chart(name):
    if save_chart_as_file:
        plt.savefig(global_result_dir + "/" + name, bbox_inches="tight")
        plt.close()
    else:
        plt.show()


def _increase_font_size():
    font = {"size": 20}
    matplotlib.rc("font", **font)


def _set_title(title):
    if include_title:
        plt.title(title)


def plot_all_charts(result_dir, cpu_usage, mem_usage, all_data):

    global global_result_dir
    global_result_dir = result_dir

    _plot_chart_response_time(all_data)
    _plot_vital_signs_generated_per_minute(all_data)
    _plot_throughput(all_data)
    _plot_response_time(all_data)
    _plot_smaller_response_times(all_data)
    _plot_user_priority()

    for machine_name, current_cpu_node_data in cpu_usage.items():
        _plot_chart_cpu_usage(machine_name, current_cpu_node_data)

    for machine_name, current_mem_node_data in mem_usage.items():
        _plot_chart_mem_usage(machine_name, current_mem_node_data)

    for priority, thread_data in sorted(all_data.items()):
        for fog_name in thread_data["fog_nodes_data"]:
            _plot_offloading_histogram(all_data, fog_name)
            _plot_local_executions_histogram(all_data, fog_name)
            _plot_stacked_offloading_local_executions(all_data, fog_name)
            _plot_offloading_reasons(all_data, fog_name)
            _plot_stacked_local_executions_with_offloading(all_data, fog_name)


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

    all_datetimes, all_cpus, all_last_observations = zip(
        *sorted(zip(all_datetimes, all_cpus, all_last_observations))
    )

    plt.plot(all_datetimes, all_cpus, label="Smoothed CPU")
    plt.plot(
        all_datetimes,
        all_last_observations,
        label="Last CPU observation",
        color="moccasin",
        linestyle="--",
    )

    letter_fog_node = machine_name[-1]
    _set_title("CPU usage on fog node {}".format(letter_fog_node))
    plt.xlabel("Moment when CPU usage was collected")
    plt.ylabel("CPU usage")

    plt.gca().xaxis.set_major_formatter(mdates.DateFormatter('%H:%M:%S'))
    plt.setp(plt.gca().get_xticklabels(), rotation=60, ha="right")

    _finalize_chart("cpu_usage_{}.png".format(machine_name))


def _plot_chart_mem_usage(machine_name, mem_usage):
    _initialize_chart()

    all_datetimes = []
    all_mems = []
    for index in range(len(mem_usage)):
        current_json = mem_usage[index]
        all_mems.append(current_json["mem"])
        all_datetimes.append(
            dt.datetime.fromtimestamp(current_json["collection_timestamp"] / 1e3)
        )

    all_datetimes, all_mems = zip(*sorted(zip(all_datetimes, all_mems)))

    plt.plot(all_datetimes, all_mems, label="Memory percent")
    _set_title("Memory Usage during tests for machine {}".format(machine_name))
    plt.xlabel("Timestamp")
    plt.ylabel("Memory Usage")
    _finalize_chart("mem_usage_{}.png".format(machine_name))


def _plot_chart_response_time(all_data):
    _initialize_chart()
    legend = []
    
    for key, thread_data in sorted(all_data.items()):

        # Converts UTC-3 to UTC
        timezone_correct = []
        for current_dt in thread_data["start_datetime"]:
            timezone_correct.append(current_dt + timedelta(hours=3))


        legend.append(_translate_user_priority_label(key))
        plt.plot(timezone_correct, thread_data["elapsed"])

    _set_title("Response time for each user priority")
    plt.xlabel("Execution moment")
    plt.ylabel("Response time (millis)")
    plt.legend(legend)
    plt.setp(plt.gca().get_xticklabels(), rotation=60, ha="right")

    _finalize_chart("response_time.png")


def _plot_vital_signs_generated_per_minute(all_data):
    _initialize_chart()
    import collections
    data = []
    for key, thread_data in sorted(all_data.items()):
        start_datetime = thread_data["start_datetime"]

        for current_dt in start_datetime:
            data.append(current_dt.replace(microsecond=0, second=0))
            pass

    
    counter = collections.Counter(data)
    keys = counter.keys()
    values = counter.values()

    keys, values = zip(*sorted(zip(keys, values)))

    plt.plot(keys, values, color="b")
    plt.xlabel("Timestamp")
    plt.ylabel("Vital signs")
    _finalize_chart("vital_signs_generated.png")

def _plot_offloading_histogram(all_data, fog_name):
    _initialize_chart()
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        y.append(thread_data["fog_nodes_data"][fog_name]["total_offloading"])

    plt.bar(x, y, color="b", width=0.4)
    _set_title("Offloading operations according to user priority")
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
    _set_title("Local executions according to user priority")
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
    _set_title("Execution operations according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Execution operations")
    plt.legend(loc="lower right")
    _finalize_chart("execution_operations_{}.png".format(fog_name))


def _plot_stacked_local_executions_with_offloading(all_data, fog_name):
    _initialize_chart()

    x = []
    local_execution = []
    exceeded_critical_cpu = []
    heuristic_by_ranking = []
    heuristic_by_duration = []
    heuristic_by_fallback = []

    for key, thread_data in sorted(all_data.items()):
        fog_data = thread_data["fog_nodes_data"][fog_name]

        x.append(_translate_user_priority_label(key))
        local_execution.append(fog_data["total_local_execution"])
        exceeded_critical_cpu.append(fog_data["total_exceeded_critical_cpu_threshold"])
        heuristic_by_ranking.append(fog_data["total_result_for_heuristic_by_ranking"])
        heuristic_by_duration.append(fog_data["total_result_for_heuristic_by_duration"])
        heuristic_by_fallback.append(fog_data["total_assuming_fallback_for_heuristics"])

    plt.bar(x, local_execution, color="g", width=0.4, label="Local executions")

    plt.bar(
        x,
        exceeded_critical_cpu,
        bottom=local_execution,
        color="b",
        width=0.4,
        label="Offloaded by critical CPU",
    )

    combined = np.array(local_execution) + np.array(exceeded_critical_cpu)
    plt.bar(
        x,
        heuristic_by_ranking,
        bottom=combined,
        color="r",
        width=0.4,
        label="Offloaded by ranking",
    )

    combined = (
        np.array(local_execution)
        + np.array(exceeded_critical_cpu)
        + np.array(heuristic_by_ranking)
    )
    plt.bar(
        x,
        heuristic_by_duration,
        bottom=combined,
        color="y",
        width=0.4,
        label="Offloaded by duration",
    )

    # combined = np.array(local_execution) + np.array(exceeded_critical_cpu) + np.array(heuristic_by_ranking) +  + np.array(heuristic_by_duration)
    # plt.bar(
    #     x,
    #     heuristic_by_fallback,
    #     bottom=combined,
    #     color="c",
    #     width=0.4,
    #     label="Local exec by fallback",
    # )

    letter_fog_node = fog_name[-1]
    _set_title(
        f"Ingestion operations on fog node " + letter_fog_node
    )
    plt.xlabel("User priority")
    plt.ylabel("Ingestion operations")
    plt.legend(loc="lower right", fancybox=True, framealpha=1)
    plt.setp(plt.gca().get_xticklabels(), rotation=60, ha="right")
    _finalize_chart("execution_operations_with_offloading_{}.png".format(fog_name))


def _plot_throughput(all_data):
    _initialize_chart()
    x = []
    y = []
    for key, thread_data in sorted(all_data.items()):
        x.append(_translate_user_priority_label(key))
        y.append(thread_data["throughput_seconds"])

    plt.bar(x, y, color="g", width=0.4)
    _set_title("Throughput according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Throughput (vital signs/sec)")

    plt.setp(plt.gca().get_xticklabels(), rotation=60, ha="right")

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
        x.append(_translate_user_priority_label(key))
        max_response_time.append(thread_data["maximum"] / 1000)
        p99_response_time.append(thread_data["percentile_99"] / 1000)
        p95_response_time.append(thread_data["percentile_95"] / 1000)
        p90_response_time.append(thread_data["percentile_90"] / 1000)
        p80_response_time.append(thread_data["percentile_80"] / 1000)
        p70_response_time.append(thread_data["percentile_70"] / 1000)
        p60_response_time.append(thread_data["percentile_60"] / 1000)
        p50_response_time.append(thread_data["percentile_50"] / 1000)
        avg_response_time.append(thread_data["average"] / 1000)
        min_response_time.append(thread_data["minimum"] / 1000)

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
    _set_title("Response time (seconds) for higher percentiles according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Response time (seconds)")
    plt.legend(
        loc="center left", bbox_to_anchor=(0.85, 0.5), fancybox=True, framealpha=1
    )

    current_values = plt.gca().get_yticks()
    plt.gca().set_yticklabels(["{:,.0f}".format(x).replace(",", ".") for x in current_values])
    # plt.setp(plt.gca().get_xticklabels(), rotation=60, ha="right")

    _finalize_chart("response_time_with_priority.png")



def _plot_smaller_response_times(all_data):
    _initialize_chart()

    x = []
    p50_response_time = []
    p40_response_time = []
    p30_response_time = []
    p20_response_time = []
    p10_response_time = []
    for key, thread_data in sorted(all_data.items()):
        x.append(_translate_user_priority_label(key))
        p50_response_time.append(thread_data["percentile_50"] / 1000)
        p40_response_time.append(thread_data["percentile_40"] / 1000)
        p30_response_time.append(thread_data["percentile_30"] / 1000)
        p20_response_time.append(thread_data["percentile_20"] / 1000)
        p10_response_time.append(thread_data["percentile_10"] / 1000)

    x_axis = np.arange(len(x))
    plt.bar(
        x_axis - 0.2, p50_response_time, color="b", width=0.1, label="50th percentile"
    )
    plt.bar(
        x_axis - 0.1, p40_response_time, color="g", width=0.1, label="40th percentile"
    )
    plt.bar(
        x_axis,       p30_response_time, color="r", width=0.1, label="30th percentile"
    )
    plt.bar(
        x_axis + 0.1, p20_response_time, color="y", width=0.1, label="20th percentile"
    )
    plt.bar(
        x_axis + 0.2, p10_response_time, color="m", width=0.1, label="10th percentile"
    )

    plt.xticks(x_axis, x)
    _set_title("Response time (seconds) for lower percentiles according to user priority")
    plt.xlabel("User priority")
    plt.ylabel("Response time (seconds)")
    plt.legend(
        loc="center left", bbox_to_anchor=(0.85, 0.5), fancybox=True, framealpha=1
    )

    current_values = plt.gca().get_yticks()
    plt.gca().set_yticklabels(["{:,.0f}".format(x).replace(",", ".") for x in current_values])

    _finalize_chart("smaller_response_time_with_priority.png")

def _plot_user_priority():
    _initialize_chart()
    x = [_translate_user_priority_label("1"), _translate_user_priority_label("2"), _translate_user_priority_label("3"), _translate_user_priority_label("4"), _translate_user_priority_label("5")]
    ranking_body_temperature_monitor = [3, 5, 7, 9, 11]
    ranking_heart_failure_predictor = [7, 9, 11, 13, 15]

    x_axis = np.arange(len(x))
    plt.xticks(x_axis, x)

    plt.bar(x_axis - 0.2, ranking_body_temperature_monitor, color="g", width=0.4, label="Body temperature monitor")
    plt.bar(x_axis + 0.2, ranking_heart_failure_predictor, color="b", width=0.4, label="Heart failure predictor")
    _set_title("Rankings for service and user priority")
    plt.xlabel("User priority")
    plt.ylabel("Calculated ranking")
    plt.legend(loc="upper left")

    plt.setp(plt.gca().get_xticklabels(), rotation=60, ha="right")

    _finalize_chart("rankings.png")



def _plot_offloading_reasons(all_data, fog_name):
    _initialize_chart()
    x = []
    exceeded_critical_cpu_threshold = []
    # exceeded_critical_mem_threshold = []
    heuristic_by_ranking = []
    for key, thread_data in sorted(all_data.items()):
        x.append(key)
        exceeded_critical_cpu_threshold.append(
            thread_data["fog_nodes_data"][fog_name][
                "total_exceeded_critical_cpu_threshold"
            ]
        )
        # exceeded_critical_mem_threshold.append(
        #     thread_data["fog_nodes_data"][fog_name]["total_exceeded_critical_mem_threshold"]
        # )
        heuristic_by_ranking.append(
            thread_data["fog_nodes_data"][fog_name][
                "total_result_for_heuristic_by_ranking"
            ]
        )

    plt.bar(
        x,
        exceeded_critical_cpu_threshold,
        color="g",
        width=0.4,
        label="Exceeded critical CPU threshold",
    )
    # plt.bar(
    #     x,
    #     exceeded_critical_mem_threshold,
    #     color="b",
    #     width=0.4,
    #     label="Exceeded critical memory threshold",
    # )
    plt.bar(
        x,
        heuristic_by_ranking,
        bottom=exceeded_critical_cpu_threshold,
        color="r",
        width=0.4,
        label="Heuristic by ranking",
    )

    _set_title("Reasons for offloading")
    plt.xlabel("User priority")
    plt.ylabel("Offloading operations")
    plt.legend(loc="upper right")
    _finalize_chart("offloading_reasons_{}.png".format(fog_name))


def _read_json(path):
    with open(path) as f:
        content = f.read()
        return eval(content)


def _update_percentiles(all_data):
    for user_priority, thread_data in all_data.items():
        thread_data["percentile_40"] = np.percentile(thread_data["elapsed"], 40)
        thread_data["percentile_30"] = np.percentile(thread_data["elapsed"], 30)
        thread_data["percentile_20"] = np.percentile(thread_data["elapsed"], 20)
        thread_data["percentile_10"] = np.percentile(thread_data["elapsed"], 10)
        
def _translate_user_priority_label(user_priority):
    if user_priority == "1":
        return "Very healthy"
    elif user_priority == "2":
        return "Healthy"
    elif user_priority == "3":
        return "Warning"
    elif user_priority == "4":
        return "Critical"
    elif user_priority == "5":
        return "Very critical"


if __name__ == "__main__":

    result_dir = "<include results dir here>"

    all_data = _read_json(f"{result_dir}/analyzed-dataset.json")
    _update_percentiles(all_data)

    cpu_usage = {}
    mem_usage = {}

    fog_nodes = ["fog_node_a", "fog_node_b", "fog_node_c"]
    for current_node in fog_nodes:
        cpu_usage[current_node] = _read_json(
            f"{result_dir}/cpu-usage_{current_node}.json"
        )
        mem_usage[current_node] = _read_json(
            f"{result_dir}/mem-usage_{current_node}.json"
        )

    plot_all_charts(
        result_dir="<temp dir>",
        all_data=all_data,
        cpu_usage=cpu_usage,
        mem_usage=mem_usage,
    )

import logging
import datetime


def print_summary(all_data):
    logging.info("Test summary")
    for key, thread_data in sorted(all_data.items()):
        _print_summary_current_key(key, thread_data)


def _print_summary_current_key(key, thread_data):
    logging.info("-----------")
    logging.info("# User priority: {}".format(key))
    logging.info("##      Throughput/sec: {}".format(thread_data["throughput_seconds"]))
    logging.info("##             Minimum: {}".format(thread_data["minimum"] / 1000))
    logging.info("##             Maximum: {}".format(thread_data["maximum"] / 1000))
    logging.info("##     99th percentile: {}".format(thread_data["percentile_99"] / 1000))
    logging.info("##     95th percentile: {}".format(thread_data["percentile_95"] / 1000))
    logging.info("##     90th percentile: {}".format(thread_data["percentile_90"] / 1000))
    logging.info("##     80th percentile: {}".format(thread_data["percentile_80"] / 1000))
    logging.info("##     70th percentile: {}".format(thread_data["percentile_70"] / 1000))
    logging.info("##     60th percentile: {}".format(thread_data["percentile_60"] / 1000))
    logging.info("##     50th percentile: {}".format(thread_data["percentile_50"] / 1000))
    logging.info("##             Average: {}".format(thread_data["average"] / 1000))


    minimum = round(thread_data["minimum"] / 1000, 3)
    maximum = round(thread_data["maximum"] / 1000, 3)
    percentile_99 = round(thread_data["percentile_99"] / 1000, 3)
    percentile_95 = round(thread_data["percentile_95"] / 1000, 3)
    percentile_90 = round(thread_data["percentile_90"] / 1000, 3)
    percentile_80 = round(thread_data["percentile_80"] / 1000, 3)
    percentile_70 = round(thread_data["percentile_70"] / 1000, 3)
    percentile_60 = round(thread_data["percentile_60"] / 1000, 3)
    percentile_50 = round(thread_data["percentile_50"] / 1000, 3)
    average = round(thread_data["average"] / 1000, 3)

    std_dev = "?"

    print(f"{key} & {maximum} & {percentile_99} & {percentile_95} & {percentile_90} & {percentile_80} & {percentile_70} & {percentile_60} & {percentile_50} & {average} & {minimum} & {std_dev} \\\\")

    for fog_node_name in thread_data["fog_nodes_data"]:
        logging.info("## Current fog node name: {}".format(fog_node_name))
        _print_fog_node_data(thread_data["fog_nodes_data"][fog_node_name])
        logging.info("")


def _print_fog_node_data(fog_node_data):
    logging.info(
        "###     Tot.offloadings: {}".format(fog_node_data["total_offloading"])
    )
    logging.info(
        "###      Tot.local exec: {}".format(fog_node_data["total_local_execution"])
    )
    logging.info(
        "###      Tot.exceeded critical cpu threshold: {}".format(
            fog_node_data["total_exceeded_critical_cpu_threshold"]
        )
    )
    logging.info(
        "###      Tot.exceeded critical mem threshold: {}".format(
            fog_node_data["total_exceeded_critical_mem_threshold"]
        )
    )
    logging.info(
        "###  Tot.triggered heuristic by rankings: {}".format(
            fog_node_data["total_triggered_heuristic_by_rankings"]
        )
    )
    logging.info(
        "###  Tot.result for heuristic by ranking: {}".format(
            fog_node_data["total_result_for_heuristic_by_ranking"]
        )
    )
    logging.info(
        "###  Tot.triggered heuristic by duration: {}".format(
            fog_node_data["total_triggered_heuristic_by_duration"]
        )
    )
    logging.info(
        "### Tot.result for heuristic by duration: {}".format(
            fog_node_data["total_result_for_heuristic_by_duration"]
        )
    )
    logging.info(
        "###          Tot.fallback for heuristics: {}".format(
            fog_node_data["total_assuming_fallback_for_heuristics"]
        )
    )


def _read_json(path):
    with open(path) as f:
        content = f.read()
        return eval(content)

if __name__ == "__main__":


    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s.%(msecs)03d %(levelname)s %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    result_dir = "<results dir>"

    all_data = _read_json(f"{result_dir}/analyzed-dataset.json")
    print_summary(all_data)

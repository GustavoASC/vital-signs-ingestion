import logging


def print_summary(all_data):
    logging.info("Test summary")
    for key, thread_data in sorted(all_data.items()):
        print_summary_current_key(key, thread_data)


def print_summary_current_key(key, thread_data):
    logging.info("-----------")
    logging.info("# User priority: {}".format(key))
    logging.info("##      Throughput/sec: {}".format(thread_data["throughput_seconds"]))
    logging.info("##             Minimum: {}".format(thread_data["minimum"]))
    logging.info("##             Maximum: {}".format(thread_data["maximum"]))
    logging.info("##     99th percentile: {}".format(thread_data["percentile_99"]))
    logging.info("##     95th percentile: {}".format(thread_data["percentile_95"]))
    logging.info("##     90th percentile: {}".format(thread_data["percentile_90"]))
    logging.info("##     80th percentile: {}".format(thread_data["percentile_80"]))
    logging.info("##     70th percentile: {}".format(thread_data["percentile_70"]))
    logging.info("##     60th percentile: {}".format(thread_data["percentile_60"]))
    logging.info("##     50th percentile: {}".format(thread_data["percentile_50"]))
    logging.info("##             Average: {}".format(thread_data["average"]))

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
        "###      Tot.exceeded critical threshold: {}".format(
            fog_node_data["total_exceeded_critical_cpu_threshold"]
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

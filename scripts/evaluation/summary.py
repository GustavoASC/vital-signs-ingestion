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
    logging.info("##             Average: {}".format(thread_data["average"]))
    logging.info("##     Tot.offloadings: {}".format(thread_data["total_offloading"]))
    logging.info(
        "##      Tot.local exec: {}".format(thread_data["total_local_execution"])
    )
    logging.info(
        "##      Tot.exceeded critical threshold: {}".format(
            thread_data["total_exceeded_critical_threshold"]
        )
    )
    logging.info(
        "##  Tot.triggered heuristic by rankings: {}".format(
            thread_data["total_triggered_heuristic_by_rankings"]
        )
    )
    logging.info(
        "##  Tot.result for heuristic by ranking: {}".format(
            thread_data["total_result_for_heuristic_by_ranking"]
        )
    )
    logging.info(
        "##  Tot.triggered heuristic by duration: {}".format(
            thread_data["total_triggered_heuristic_by_duration"]
        )
    )
    logging.info(
        "## Tot.result for heuristic by duration: {}".format(
            thread_data["total_result_for_heuristic_by_duration"]
        )
    )
    logging.info(
        "##          Tot.fallback for heuristics: {}".format(
            thread_data["total_assuming_fallback_for_heuristics"]
        )
    )
    logging.info("")

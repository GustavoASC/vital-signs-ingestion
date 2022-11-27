import logging, utils


def collect_cpu_usage(fog_node_ip):
    return utils.get(f"http://{fog_node_ip}:9001/metrics/cpu")["response"]


def clear_metrics(fog_node_ip):
    logging.info("Clearing metrics...")
    utils.post(f"http://{fog_node_ip}:9001/clear", {})


def collect_metrics_summary_for_user_priority(fog_node_ip, user_priority_filter):
    payload = utils.get(
        f"http://{fog_node_ip}:9001/metrics/summary?user_priority={user_priority_filter}"
    )
    return payload["response"]

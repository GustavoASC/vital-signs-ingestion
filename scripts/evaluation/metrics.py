import logging, urllib3, json

http = urllib3.PoolManager()


def _check_error(response):
    if response.status != 200:
        logging.info("Error collecting offloading metrics during tests")
        exit(1)


def collect_cpu_usage(fog_node_ip, start_date_time):
    r = http.request(
        "GET",
        "http://{}:9001/metrics/cpu?since={}".format(fog_node_ip, start_date_time),
        headers={"Content-Type": "application/json"},
    )

    _check_error(r)
    return json.loads(r.data)["response"]


def clear_metrics(fog_node_ip):
    logging.info("Clearing metrics...")
    _check_error(
        http.request(
            "POST",
            "http://{}:9001/clear".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
        )
    )


def collect_metrics_summary_for_user_priority(fog_node_ip, user_priority_filter):
    r = http.request(
        "GET",
        "http://{}:9001/metrics/summary?user_priority={}".format(
            fog_node_ip, user_priority_filter
        ),
        headers={"Content-Type": "application/json"},
    )

    _check_error(r)
    return json.loads(r.data)["response"]

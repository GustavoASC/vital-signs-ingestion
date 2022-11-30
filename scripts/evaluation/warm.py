import logging, urllib3, json

http = urllib3.PoolManager()


def _warmup_ranking_offloading(fog_node_ip):
    function_cold = True
    while function_cold:
        r = http.request(
            "POST",
            "http://{}:8080/function/ranking-offloading".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
            body=json.dumps(
                {"all_rankings": [1, 3, 6, 3, 7, 8, 2], "calculated_ranking": 15}
            ).encode("utf-8"),
        )
        if r.status == 200:
            function_cold = False


def _warmup_duration_offloading(fog_node_ip):
    function_cold = True
    while function_cold:
        r = http.request(
            "POST",
            "http://{}:8080/function/duration-offloading".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
            body=json.dumps(
                {
                    "durations_running_services": [[4, 5, 6]],
                    "durations_target_service": [1, 2, 3],
                }
            ).encode("utf-8"),
        )
        if r.status == 200:
            function_cold = False


def _warmup_predictor(fog_node_ip):
    function_cold = True
    while function_cold:
        r = http.request(
            "POST",
            "http://{}:8080/function/predictor".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
            body=json.dumps(
                {
                    "data": [
                        17,
                        21,
                        23,
                        26,
                        26,
                        28,
                        30,
                        30,
                        30,
                        31,
                        32,
                        33,
                        39,
                        41,
                        41,
                    ],
                    "smoothing_level": 0.8,
                    "smoothing_trend": 0.2,
                    "future_data_points": 1,
                }
            ).encode("utf-8"),
        )
        if r.status == 200:
            function_cold = False


def _warmup_body_temperature_monitor(fog_node_ip):
    function_cold = True
    while function_cold:
        r = http.request(
            "POST",
            "http://{}:8080/function/body-temperature-monitor".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
            body=json.dumps({"temperature": 40}).encode("utf-8"),
        )
        if r.status == 200:
            function_cold = False


def _warmup_topology_mapping(fog_node_ip):
    function_cold = True
    while function_cold:
        r = http.request(
            "GET",
            "http://{}:8080/function/topology-mapping".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
        )
        if r.status == 200:
            function_cold = False


def _warmup_service_executor(fog_node_ip):
    function_cold = True
    while function_cold:
        r = http.request(
            "POST",
            "http://{}:8080/function/service-executor".format(fog_node_ip),
            headers={"Content-Type": "application/json"},
            body=json.dumps(
                {
                    "service_name": "body-temperature-monitor",
                    "vital_sign": '{"temperature": 40}',
                    "user_priority": 3,
                    "id": "3df4f9e3-f9e8-4cc1-9b18-7a8893a14838"
                }
            ).encode("utf-8"),
        )
        if r.status == 200:
            function_cold = False


def warmup_functions(fog_node_ip):
    logging.info("Warming functions...")
    _warmup_ranking_offloading(fog_node_ip)
    _warmup_duration_offloading(fog_node_ip)
    _warmup_predictor(fog_node_ip)
    _warmup_body_temperature_monitor(fog_node_ip)
    _warmup_topology_mapping(fog_node_ip)
    _warmup_service_executor(fog_node_ip)

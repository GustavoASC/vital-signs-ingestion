import logging, urllib3, json


http = urllib3.PoolManager()


def _check_error(response):
    if response.status != 200:
        logging.info("Error collecting results metrics during tests")
        exit(1)


def get(url):
    r = http.request(
        "GET",
        url,
        headers={"Content-Type": "application/json"},
    )

    _check_error(r)
    return json.loads(r.data)


def post(url, json_payload):
    _check_error(
        http.request(
            "POST",
            url,
            headers={"Content-Type": "application/json"},
            body=json.dumps(json_payload).encode("utf-8"),
        )
    )

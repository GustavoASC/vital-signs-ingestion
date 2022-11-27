import urllib.request, traceback, uuid, datetime
from threading import Thread

SERVICE_EXECUTOR_URL = "<paste here the service executor URL>"
ASYNC_RESULTS_URL = "<paste here the async results URL>"

TOTAL_USER_PRIORITIES = 5
THREADS_PER_PRIORITY = 2
HEALTH_SERVICES = ["body-temperature-monitor", "heart-failure-predictor"]

VITAL_SIGNS_PER_THREAD = 3000


def send_http_request(url, payload, method):
    req = urllib.request.Request(
        url=url,
        data=payload,
        headers={"content-type": "application/json"},
        method=method,
    )
    with urllib.request.urlopen(req) as response:
        print(f"Response: {str(response.read())}")


def register_request_started(id, data):
    #
    # Registers the initial date/time, when vital sign is sent.
    #
    # The endpoint understands that the vital sign is being processed because it does not have
    # a field with the final timestamp of the execution
    #
    payload = dict(data)
    payload["start_timestamp"] = int(datetime.datetime.now().timestamp() * 1000)
    send_http_request(f"{ASYNC_RESULTS_URL}/{id}", payload, "PUT")


def dispatch_vital_sign(id, data):
    payload = dict(data)
    payload["id"] = id
    send_http_request(SERVICE_EXECUTOR_URL, payload, "POST")


def ingest_vital_sign(payload):

    for i in range(VITAL_SIGNS_PER_THREAD):
        id = str(uuid.uuid4())
        try:
            register_request_started(id, payload)
            dispatch_vital_sign(id, payload)
        except Exception as e:
            print(f"Unexpected problem happened: {traceback.format_exc()}")


def scenario_one():

    all_threads = []

    for i in range(TOTAL_USER_PRIORITIES):
        for j in range(THREADS_PER_PRIORITY):
            for service in HEALTH_SERVICES:
                payload = {
                    "user_priority": i,
                    "service_name": service,
                    "vital_sign": '{"heartbeat": 97, "spo2": 99, "temperature": 40}',
                }
                thread = Thread(target=ingest_vital_sign, args=(payload,))
                thread.start()
                all_threads.append(thread)

    for thread in all_threads:
        thread.join()


if __name__ == "__main__":
    scenario_one()

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.request, traceback, uuid, datetime
from threading import Thread

TOTAL_USER_PRIORITIES = 5
THREADS_PER_PRIORITY = 2
HEALTH_SERVICES = ["body-temperature-monitor", "heart-failure-predictor"]

VITAL_SIGNS_PER_THREAD = 1000


def send_http_request(url, payload, method):
    req = urllib.request.Request(
        url=url,
        data=json.dumps(payload).encode("utf-8"),
        headers={"content-type": "application/json"},
        method=method,
    )
    with urllib.request.urlopen(req) as response:
        print(f"Response: {str(response.read())}")


def register_request_started(id, data, async_results_url):
    #
    # Registers the initial date/time, when vital sign is sent.
    #
    # The endpoint understands that the vital sign is being processed because it does not have
    # a field with the final timestamp of the execution
    #
    payload = dict(data)
    payload["start_timestamp"] = int(datetime.datetime.now().timestamp() * 1000)
    send_http_request(f"{async_results_url}/results/{id}", payload, "PUT")


def dispatch_vital_sign(id, data, service_executor_url):
    payload = dict(data)
    payload["id"] = id
    send_http_request(f"{service_executor_url}/function/service-executor", payload, "POST")


def ingest_vital_sign(payload, service_executor_url, async_results_url):

    for i in range(VITAL_SIGNS_PER_THREAD):
        id = str(uuid.uuid4())
        try:
            register_request_started(id, payload, async_results_url)
            dispatch_vital_sign(id, payload, service_executor_url)
        except Exception as e:
            print(f"Unexpected problem happened: {traceback.format_exc()}")


def scenario_one(service_executor_url, async_results_url):

    all_threads = []

    for i in range(TOTAL_USER_PRIORITIES):
        for j in range(THREADS_PER_PRIORITY):
            for service in HEALTH_SERVICES:
                payload = {
                    "user_priority": i + 1,
                    "service_name": service,
                    "vital_sign": '{"heartbeat": 97, "spo2": 99, "temperature": 40}',
                }
                thread = Thread(
                    target=ingest_vital_sign,
                    args=(
                        payload,
                        service_executor_url,
                        async_results_url,
                    ),
                )
                print("Starting thread...")
                thread.start()
                all_threads.append(thread)

    for thread in all_threads:
        thread.join()

    print("Finished all threads")


class Serv(BaseHTTPRequestHandler):
    def do_POST(self):

        if self.path.startswith("/invoke-test-plan"):

            settings = self.request_body()
            service_executor_url = settings["service_executor_url"]
            async_results_url = settings["async_results_url"]

            if settings["test_plan"] == "scenario-1":
                print("Invoking scenario one...")
                scenario_one(service_executor_url, async_results_url)

            self.write_json_response({})

    def request_body(self):
        content_len = int(self.headers.get("Content-Length"))
        text_content = self.rfile.read(content_len)
        return json.loads(text_content)

    def write_json_response(self, response_json):
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(response_json).encode("utf-8"))


def start():
    print("Starting HTTP server...")
    httpd = HTTPServer(("", 9002), Serv)
    httpd.serve_forever()


if __name__ == "__main__":
    start()

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.request, traceback, uuid, datetime
from threading import Thread


def send_http_request(url, payload, method):
    keep_trying = True
    while keep_trying:
        try:
            req = urllib.request.Request(
                url=url,
                data=json.dumps(payload).encode("utf-8"),
                headers={"content-type": "application/json"},
                method=method,
            )
            with urllib.request.urlopen(req) as response:
                print(f"Response: {str(response.read())}")
                keep_trying = False
        except Exception as e:
            print(
                f"Unexpected problem when sending request to {url}: {traceback.format_exc()}"
            )
            print("Will retry...")


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
    send_http_request(
        f"{service_executor_url}/function/service-executor", payload, "POST"
    )


def ingest_vital_sign(
    payload, service_executor_url, async_results_url, vital_signs_per_thread
):

    for i in range(vital_signs_per_thread):
        id = str(uuid.uuid4())
        try:
            register_request_started(id, payload, async_results_url)
            dispatch_vital_sign(id, payload, service_executor_url)
        except Exception as e:
            print(f"Unexpected problem happened: {traceback.format_exc()}")


def scenario_one(settings):

    all_threads = []

    total_user_priorities = int(settings["total_user_priorities"])
    threads_per_priority = int(settings["threads_per_priority"])
    vital_signs_per_thread = int(settings["vital_signs_per_thread"])

    service_executor_url = settings["service_executor_url"]
    async_results_url = settings["async_results_url"]
    health_services = settings["health_services"]

    for i in range(total_user_priorities):
        for j in range(threads_per_priority):
            for service in health_services:
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
                        vital_signs_per_thread,
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

            if settings["test_plan"] == "scenario-1":
                print("Invoking scenario one...")
                scenario_one(settings)

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

from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import psutil
import json
import time
import schedule

update_interval = 5
total_threads = 20

cpu_percent = 0.0


class Serv(BaseHTTPRequestHandler):
    def do_GET(self):
        global cpu_percent

        print("Current amount of cpu: " + str(cpu_percent))
        print("Current amount of memory: " + str(psutil.virtual_memory()))

        response_bytes = json.dumps({"cpu": cpu_percent}).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)

    def do_POST(self):
        global update_interval
        global total_threads

        content_len = int(self.headers.get("Content-Length"))
        post_body = self.rfile.read(content_len)
        settings = json.loads(post_body)

        _finalize_all_threads()
        update_interval = settings["update_interval"]
        total_threads = settings["total_threads"]
        _initialize_threads()

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({}).encode("utf-8"))


def job_thread():
    thread = Thread(target=update_cpu_percent_loop)
    thread.start()


def update_cpu_percent_loop():
    global cpu_percent
    global update_interval
    cpu_percent = psutil.cpu_percent(update_interval)


def _initialize_threads():
    global update_interval
    global total_threads

    thread_start_offset = update_interval / total_threads

    print("Initiating schedule...")
    for i in range(total_threads):
        schedule.every(update_interval).seconds.do(job_thread)
        time.sleep(thread_start_offset)


def _finalize_all_threads():
    print("Clearing all schedules...")
    schedule.clear()


def start():
    _initialize_threads()

    # Starts the HTTP server to listen for GET CPU requests
    print("Starting the HTTP server...")
    httpd = HTTPServer(("", 8099), Serv)
    Thread(target=httpd.serve_forever).start()

    while True:
        schedule.run_pending()
        time.sleep(0.01)


if __name__ == "__main__":
    start()

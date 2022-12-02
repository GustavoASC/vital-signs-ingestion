from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import psutil
import json
import gc
import time
from socketserver import ThreadingMixIn

MAX_WINDOW = 6

update_interval = 0.25
cpu_info = {}


def aging(cpu_observations):
    result = 0
    for x in cpu_observations:
        middle = x / 2
        result = result / 2
        result = result + middle

    return result


class Serv(BaseHTTPRequestHandler):
    def do_GET(self):
        global cpu_info

        print(
            "Current amount of cpu (last observation): "
            + str(cpu_info["last_observation"])
        )

        mem = psutil.virtual_memory()
        print("Current amount of cpu (smoothed): " + str(cpu_info["smoothed"]))
        print("Current amount of memory: " + str(mem))

        response_bytes = json.dumps(
            {
                "cpu": cpu_info["smoothed"],
                "last_observation": cpu_info["last_observation"],
                "memory": mem.percent
            }
        ).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)

    def do_POST(self):
        global update_interval

        content_len = int(self.headers.get("Content-Length"))
        post_body = self.rfile.read(content_len)
        settings = json.loads(post_body)

        update_interval = settings["update_interval"]

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({}).encode("utf-8"))


def update_cpu_percent_loop():
    global update_interval
    global cpu_info

    observations = []
    while True:
        x = psutil.cpu_percent(update_interval)
        observations.append(x)

        if len(observations) > MAX_WINDOW:
            observations.pop(0)

        cpu_info = {
            "last_observation": x,
            "smoothed": aging(observations)
        }

def garbage_collector():
    while True:
        time.sleep(10)
        
        print("Running GC...")
        gc.collect()


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""


def start():
    thread = Thread(target=update_cpu_percent_loop)
    thread.start()

    gc = Thread(target=garbage_collector)
    gc.start()

    print("Starting the HTTP server...")
    httpd = ThreadedHTTPServer(("", 8099), Serv)
    httpd.serve_forever()


if __name__ == "__main__":
    start()

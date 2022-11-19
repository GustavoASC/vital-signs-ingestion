from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import psutil
import json

MAX_WINDOW = 6

update_interval = 0.25
observations = []
cpu_percent = 0


def aging(cpu_observations):
    result = 0
    for x in cpu_observations:
        middle = x / 2
        result = result / 2
        result = result + middle

    return result


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
    global observations
    global cpu_percent

    while True:
        x = psutil.cpu_percent(update_interval)
        observations.append(x)

        if len(observations) > MAX_WINDOW:
            observations.pop(0)

        cpu_percent = aging(observations)


def start():
    thread = Thread(target=update_cpu_percent_loop)
    thread.start()

    print("Starting the HTTP server...")
    httpd = HTTPServer(("", 8099), Serv)
    httpd.serve_forever()


if __name__ == "__main__":
    start()

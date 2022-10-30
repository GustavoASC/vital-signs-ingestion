from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import psutil
import json
import time

update_interval = 1
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

        content_len = int(self.headers.get('Content-Length'))
        post_body = self.rfile.read(content_len)

        settings = json.loads(post_body)
        update_interval = settings['update_interval']

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({}).encode("utf-8"))


def update_cpu_percent_loop():
    global cpu_percent
    while True:
        cpu_percent = psutil.cpu_percent(update_interval)


if __name__ == "__main__":

    # Starts a thread to periodically update CPU percentage
    thread = Thread(target=update_cpu_percent_loop)
    thread.start()

    # Waits a small amount of time to ensure that CPU percentage
    # has been updated at least once
    time.sleep(1.5)

    # Starts the HTTP server to listen for GET CPU requests
    httpd = HTTPServer(("", 8099), Serv)
    httpd.serve_forever()

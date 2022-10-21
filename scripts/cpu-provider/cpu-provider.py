from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import psutil
import json
import time

cpu_percent = 0.0
virtual_memory = 0.0

class Serv(BaseHTTPRequestHandler):
    def do_GET(self):
        global cpu_percent
        global virtual_memory
        
        print("Current amount of cpu: " + str(cpu_percent))
        print("Current amount of memory: " + str(virtual_memory))

        response_bytes = json.dumps({"cpu": cpu_percent}).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)


def update_cpu_percent_loop():
    global cpu_percent
    global virtual_memory
    while True:
        cpu_percent = psutil.cpu_percent(1)
        virtual_memory = psutil.virtual_memory()


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

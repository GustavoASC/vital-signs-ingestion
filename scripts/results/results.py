from http.server import HTTPServer, BaseHTTPRequestHandler
from socketserver import ThreadingMixIn
import datetime
import json

#
# The scructure is similar to this:
#
# results = {
#     "998987bf-9d30-4355-8fe2-00ab1f7420bf": {
#         "service_name": "heart-failure-predictor",
#         "user_priority": 5,
#         "start_timestamp": 1663527788128,
#         "end_timestamp": 1663527788578,
#         "execution_location": "CLOUD",
#     }
# }

results = {}


class Serv(BaseHTTPRequestHandler):
    def do_GET(self):

        id = self.path.split("/")[-1]
        if id == "results":
            current_response = results
        else:
            current_response = results[id]

        response_bytes = json.dumps(current_response).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)

    def do_PATCH(self):
        global results

        content_len = int(self.headers.get("Content-Length"))
        post_body = self.rfile.read(content_len)

        id = self.path.split("/")[-1]

        current_update = json.loads(post_body)
        existing_result = results[id]

        existing_result["end_timestamp"] = current_update["end_timestamp"]
        existing_result["result_received_at"] = int(datetime.datetime.now().timestamp() * 1000)

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({}).encode("utf-8"))

    def do_PUT(self):
        global results

        content_len = int(self.headers.get("Content-Length"))
        post_body = self.rfile.read(content_len)

        id = self.path.split("/")[-1]

        current_result = json.loads(post_body)
        results[id] = current_result

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({}).encode("utf-8"))


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""


def start():

    print("Starting the HTTP server...")
    httpd = ThreadedHTTPServer(("", 9095), Serv)
    httpd.serve_forever()


if __name__ == "__main__":
    start()

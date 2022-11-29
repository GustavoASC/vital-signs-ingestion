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

running_requests = {}
finished_requests = {}


class Serv(BaseHTTPRequestHandler):
    def do_GET(self):

        id = self.id_from_path_param()
        if id == "results":
            current_response = {
                "running_requests": running_requests,
                "finished_requests": finished_requests,
            }
        elif id == "summary":
            current_response = {
                "total_running_requests": len(running_requests),
                "total_finished_requests": len(finished_requests),
            }
        else:
            if id in finished_requests:
                current_response = finished_requests[id]
            else:
                current_response = running_requests[id]

        self.write_json_response(current_response)

    def do_PATCH(self):
        global running_requests
        global finished_requests

        id = self.id_from_path_param()

        if id in finished_requests:
            self.write_json_response({})
            return

        if id not in running_requests:
            self.write_json_response({})
            return

        current_update = self.request_body()
        existing_result = running_requests[id]

        existing_result["end_timestamp"] = current_update["end_timestamp"]
        existing_result["result_received_at"] = int(
            datetime.datetime.now().timestamp() * 1000
        )

        if "origin" in current_update:
            origin = current_update["origin"]
        else:
            origin = "fog"
        existing_result["origin"] = origin
        
        finished_requests[id] = existing_result
        del running_requests[id]

        self.write_json_response({})

    def do_PUT(self):
        global running_requests

        id = self.id_from_path_param()
        running_requests[id] = self.request_body()

        self.write_json_response({})

    def do_POST(self):
        global running_requests
        global finished_requests

        if self.path.startswith("/clear"):
            running_requests = {}
            finished_requests = {}

            self.write_json_response({})

    def write_json_response(self, response_json):
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(response_json).encode("utf-8"))

    def id_from_path_param(self):
        return self.path.split("/")[-1]

    def request_body(self):
        content_len = int(self.headers.get("Content-Length"))
        text_content = self.rfile.read(content_len)
        return json.loads(text_content)


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""


def start():

    print("Starting the HTTP server...")
    httpd = ThreadedHTTPServer(("", 9095), Serv)
    httpd.serve_forever()


if __name__ == "__main__":
    start()

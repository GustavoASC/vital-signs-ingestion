from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import logging
import subprocess
import os

JMETER_RESULTS_FILE = "result-requests.csv"


def read_response_content():
    with open(JMETER_RESULTS_FILE, "r") as file:
        return file.read()


def invoke_jmeter_test(host_ip, test_plan):

    delete("jmeter.log")
    delete(JMETER_RESULTS_FILE)

    print("\n\n")
    logging.info("Invoking JMeter...")
    subprocess.call(
        [
            "jmeter",
            "-n",
            "-t",
            test_plan,
            "-Jhost={}".format(host_ip),
            "-l",
            JMETER_RESULTS_FILE,
            "-L",
            "DEBUG",
        ]
    )


def delete(file):
    try:
        os.remove(file)
    except OSError:
        pass


class Serv(BaseHTTPRequestHandler):
    def do_POST(self):

        if self.path.startswith("/invoke-test-plan"):

            content_len = int(self.headers.get("Content-Length"))
            body = self.rfile.read(content_len)
            settings = json.loads(body)

            invoke_jmeter_test(settings["target_fog_node"], settings["test_plan"])

            response_bytes = read_response_content().encode("utf-8")
            self.send_response(200)
            self.send_header("Content-type", "text/csv")

        else:
            response_bytes = json.dumps({"error": "not found"}).encode("utf-8")
            self.send_response(404)
            self.send_header("Content-type", "application/json")

        self.end_headers()
        self.wfile.write(response_bytes)


if __name__ == "__main__":

    httpd = HTTPServer(("", 9002), Serv)
    httpd.serve_forever()

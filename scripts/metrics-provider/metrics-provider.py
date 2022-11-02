from http.server import HTTPServer, BaseHTTPRequestHandler
import subprocess
import json
import urllib.parse
import re

VERTICAL_OFFLOADING_GREP_FILTER = "Making vertical offloading"
VERTICAL_OFFLOADING_REGEX = ".*stdout.*(\d\d\d\d-\d\d-\d\d\s\d\d:\d\d:\d\d).*"

CPU_GREP_FILTER = 'stdout: {"cpu'
CPU_REGEX = ".*(\d\d\d\d\/\d\d\/\d\d\s\d\d:\d\d:\d\d).*({.*cpu.*}).*"


def get_lines_from_journalctl(since, grep_filter):
    journalctl = subprocess.Popen(
        ["journalctl", "--since", since, "-t", "openfaas-fn:service-executor"],
        stdout=subprocess.PIPE,
    )
    output = subprocess.check_output(
        ("grep", "-i", grep_filter), stdin=journalctl.stdout
    )
    journalctl.wait()

    lines = output.splitlines()
    return [x.decode("utf-8") for x in lines]


def wrap_response(occurences):
    return json.dumps({"response": occurences}).encode("utf-8")


class Serv(BaseHTTPRequestHandler):
    def fetch_cpu_metrics(self, since):

        lines = get_lines_from_journalctl(since, CPU_GREP_FILTER)

        occurences = []
        for current in lines:
            date_time_with_cpu_tuple = re.findall(CPU_REGEX, current)[0]
            date_time = date_time_with_cpu_tuple[0]
            current_cpu_text = date_time_with_cpu_tuple[1]
            current_cpu_json = json.loads(current_cpu_text)
            occurences.append({"datetime": date_time, "cpu": current_cpu_json["cpu"]})

        return wrap_response(occurences)

    def fetch_offloading_metrics(self, since):

        lines = get_lines_from_journalctl(since, VERTICAL_OFFLOADING_GREP_FILTER)

        occurences = []
        for current in lines:
            date_time = re.findall(VERTICAL_OFFLOADING_REGEX, current)[0]
            occurences.append({"datetime": date_time})

        return wrap_response(occurences)

    def do_GET(self):

        parsed_url = urllib.parse.urlparse(self.path)
        since = urllib.parse.parse_qs(parsed_url.query)["since"][0]

        if self.path.startswith("/metrics/cpu"):
            response_bytes = self.fetch_cpu_metrics(since)
            self.send_response(200)
        elif self.path.startswith("/metrics/offloading"):
            response_bytes = self.fetch_offloading_metrics(since)
            self.send_response(200)
        else:
            json.dumps({"error": "not found"}).encode("utf-8")
            self.send_response(404)

        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)


if __name__ == "__main__":

    httpd = HTTPServer(("", 9001), Serv)
    httpd.serve_forever()

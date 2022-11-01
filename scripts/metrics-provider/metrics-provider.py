from http.server import HTTPServer, BaseHTTPRequestHandler
import subprocess
import json
import urllib.parse
import re

class Serv(BaseHTTPRequestHandler):
    def fetch_cpu_metrics(self):
        parsed_url = urllib.parse.urlparse(self.path)
        since = urllib.parse.parse_qs(parsed_url.query)["since"][0]

        journalctl = subprocess.Popen(
            ["journalctl", "--since", since, "-t", "openfaas-fn:service-executor"],
            stdout=subprocess.PIPE,
        )
        output = subprocess.check_output(
            ("grep", "-i", 'stdout: {"cpu'), stdin=journalctl.stdout
        )
        journalctl.wait()

        lines = output.splitlines()
        lines = [x.decode("utf-8") for x in lines]

        cpu_values = []
        for current in lines:
            date_time_with_cpu_tuple = re.findall(
                ".*(\d\d\d\d\/\d\d\/\d\d\s\d\d:\d\d:\d\d).*({.*cpu.*}).*", current
            )[0]
            date_time = date_time_with_cpu_tuple[0]
            current_cpu_text = date_time_with_cpu_tuple[1]
            current_cpu_json = json.loads(current_cpu_text)
            cpu_values.append({"datetime": date_time, "cpu": current_cpu_json["cpu"]})

        return json.dumps({"response": cpu_values}).encode("utf-8")

    def do_GET(self):

        if self.path.startswith("/metrics/cpu"):
            response_bytes = self.fetch_cpu_metrics()
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

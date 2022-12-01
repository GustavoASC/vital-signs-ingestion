from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.parse
from socketserver import ThreadingMixIn

metrics = []


def wrap_response(occurences):
    return json.dumps({"response": occurences}).encode("utf-8")


class Serv(BaseHTTPRequestHandler):
    def fetch_cpu_metrics(self):
        global metrics
        occurences = []
        for current_metric in metrics:
            json_metric = json.loads(current_metric)
            occurences.append(
                {
                    "cpu": json_metric["used_cpu"],
                    "last_cpu_observation": json_metric.get("last_cpu_observation"),
                    "collection_timestamp": json_metric["cpu_collection_timestamp"],
                }
            )

        return wrap_response(occurences)

    def fetch_mem_metrics(self):
        global metrics
        occurences = []
        for current_metric in metrics:
            json_metric = json.loads(current_metric)
            occurences.append(
                {
                    "mem": json_metric.get("used_mem"),
                    "last_mem_observation": json_metric.get("last_mem_observation"),

                    # This field is cpu_collection_timestamp but is the same for memory collection
                    "collection_timestamp": json_metric["cpu_collection_timestamp"], 
                }
            )

        return wrap_response(occurences)

    def fetch_metrics_summary(self, user_priority_filter):

        global metrics

        total_offloading = 0
        total_local_execution = 0
        total_exceeded_critical_cpu_threshold = 0
        total_exceeded_critical_mem_threshold = 0
        total_triggered_heuristic_by_rankings = 0
        total_result_for_heuristic_by_ranking = 0
        total_triggered_heuristic_by_duration = 0
        total_result_for_heuristic_by_duration = 0
        total_assuming_fallback_for_heuristics = 0

        print(user_priority_filter)

        for current_text_metric in metrics:
            json_metric = json.loads(current_text_metric)

            if json_metric["user_priority"] == user_priority_filter:
                if json_metric["offloading"]:
                    total_offloading += 1
                if json_metric["running_locally"]:
                    total_local_execution += 1
                if json_metric["exceeded_critical_cpu_threshold"]:
                    total_exceeded_critical_cpu_threshold += 1
                if json_metric["exceeded_critical_mem_threshold"]:
                    total_exceeded_critical_mem_threshold += 1
                if json_metric["triggered_heuristic_by_ranking"]:
                    total_triggered_heuristic_by_rankings += 1
                if json_metric["result_for_heuristic_by_ranking"]:
                    total_result_for_heuristic_by_ranking += 1
                if json_metric["triggered_heuristic_by_duration"]:
                    total_triggered_heuristic_by_duration += 1
                if json_metric["result_for_heuristic_by_duration"]:
                    total_result_for_heuristic_by_duration += 1
                if json_metric["assuming_fallback_for_heuristics"]:
                    total_assuming_fallback_for_heuristics += 1

        return wrap_response(
            {
                "total_offloading": total_offloading,
                "total_local_execution": total_local_execution,
                "total_exceeded_critical_cpu_threshold": total_exceeded_critical_cpu_threshold,
                "total_exceeded_critical_mem_threshold": total_exceeded_critical_mem_threshold,
                "total_triggered_heuristic_by_rankings": total_triggered_heuristic_by_rankings,
                "total_result_for_heuristic_by_ranking": total_result_for_heuristic_by_ranking,
                "total_triggered_heuristic_by_duration": total_triggered_heuristic_by_duration,
                "total_result_for_heuristic_by_duration": total_result_for_heuristic_by_duration,
                "total_assuming_fallback_for_heuristics": total_assuming_fallback_for_heuristics,
            }
        )

    def do_GET(self):

        parsed_url = urllib.parse.urlparse(self.path)

        if self.path.startswith("/metrics/cpu"):
            response_bytes = self.fetch_cpu_metrics()
            self.send_response(200)

        elif self.path.startswith("/metrics/mem"):
            response_bytes = self.fetch_mem_metrics()
            self.send_response(200)

        elif self.path.startswith("/metrics/summary"):
            user_priority = urllib.parse.parse_qs(parsed_url.query)["user_priority"][0]
            user_priority = int(user_priority)
            response_bytes = self.fetch_metrics_summary(user_priority)
            self.send_response(200)

        else:
            json.dumps({"error": "not found"}).encode("utf-8")
            self.send_response(404)

        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)

    def do_POST(self):

        global metrics

        if self.path.startswith("/metrics"):

            content_len = int(self.headers.get("Content-Length"))
            current_metric = self.rfile.read(content_len)
            metrics.append(current_metric)
            response_bytes = json.dumps({}).encode("utf-8")
            self.send_response(200)

        elif self.path.startswith("/clear"):

            metrics = []
            response_bytes = json.dumps({}).encode("utf-8")
            self.send_response(200)

        else:
            response_bytes = json.dumps({"error": "not found"}).encode("utf-8")
            self.send_response(404)

        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""


if __name__ == "__main__":

    httpd = ThreadedHTTPServer(("", 9001), Serv)
    httpd.serve_forever()

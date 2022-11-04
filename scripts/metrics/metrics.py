from http.server import HTTPServer, BaseHTTPRequestHandler
import subprocess
import json
import urllib.parse
import re

## TODO: Collect datetime from metrics

metrics = []


def wrap_response(occurences):
    return json.dumps({"response": occurences}).encode("utf-8")


class Serv(BaseHTTPRequestHandler):
    def fetch_cpu_metrics(self, since):
        occurences = []
        for current_metric in metrics:
            json_metric = json.loads(current_metric)
            occurences.append({"cpu": json_metric["used_cpu"]})

        return wrap_response(occurences)

    def fetch_metrics_summary(self, user_priority_filter):

        total_offloading = 0
        total_local_execution = 0
        total_exceeded_critical_threshold = 0
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
                if json_metric["exceeded_critical_threshold"]:
                    total_exceeded_critical_threshold += 1
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
                "total_exceeded_critical_threshold": total_exceeded_critical_threshold,
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

        if self.path.startswith("/metrics"):
            content_len = int(self.headers.get("Content-Length"))
            current_metric = self.rfile.read(content_len)
            metrics.append(current_metric)
            response_bytes = json.dumps({}).encode("utf-8")
            self.send_response(200)
        else:
            response_bytes = json.dumps({"error": "not found"}).encode("utf-8")
            self.send_response(404)

        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(response_bytes)


if __name__ == "__main__":

    httpd = HTTPServer(("", 9001), Serv)
    httpd.serve_forever()

from http.server import HTTPServer, BaseHTTPRequestHandler
import psutil
import json

class Serv(BaseHTTPRequestHandler):

    def do_GET(self):

        cpu_percent = psutil.cpu_percent()
        print(f"Current cpu: {cpu_percent}")

        response_bytes = json.dumps({
            'cpu': cpu_percent
        }).encode('utf-8')

        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(response_bytes)

httpd = HTTPServer(('', 8099), Serv)
httpd.serve_forever()

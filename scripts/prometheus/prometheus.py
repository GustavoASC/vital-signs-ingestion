from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import urllib3
import json

PROMETHEUS_URL = "localhost:9090"
http = urllib3.PoolManager()

class Serv(BaseHTTPRequestHandler):
    def do_GET(self):

        url = 'http://{}{}'.format(PROMETHEUS_URL, self.path)

        resp = http.request("GET", url)
        self.send_response(resp.status)
        self.send_resp_headers(resp)
        self.wfile.write(resp.data)

    def parse_headers(self):
        req_header = {}
        for line in self.headers.headers:
            line_parts = [o.strip() for o in line.split(':', 1)]
            if len(line_parts) == 2:
                req_header[line_parts[0]] = line_parts[1]
        return req_header

    def send_resp_headers(self, resp):
        respheaders = resp.headers
        print ('Response Header')
        for key in respheaders:
            if key not in ['Content-Encoding', 'Transfer-Encoding', 'content-encoding', 'transfer-encoding', 'content-length', 'Content-Length']:
                print (key, respheaders[key])
                self.send_header(key, respheaders[key])
        self.send_header('Content-Length', len(resp.data))
        self.end_headers()        

if __name__ == "__main__":

    httpd = HTTPServer(("", 9099), Serv)
    httpd.serve_forever()

from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
import psutil
import json
import time


class Serv(BaseHTTPRequestHandler):
    def do_POST(self):
        
        print("Received request")
        print("Headers: " + str(self.headers))

        print("\n\n")
        
        if 'Content-Length' in self.headers:
            content_len = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len)
            print("Body:")
            print(post_body)
        else:
            print("There is no body to print")

        
        response_bytes = "response".encode("utf-8")
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()
        self.wfile.write(response_bytes)


# Starts the HTTP server to listen for GET CPU requests
httpd = HTTPServer(("", 9099), Serv)
httpd.serve_forever()

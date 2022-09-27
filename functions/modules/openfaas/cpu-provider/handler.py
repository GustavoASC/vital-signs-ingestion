import json, urllib, sys

URL = "http://172.17.0.1:8099"

def handle(req):

        # headers = {"content-type": "application/json"}
        sys.stderr.write("Now it is going to send a GET request to collect used CPU...")

        req = urllib.request.Request(URL)
        sys.stderr.write("Created req")

        with urllib.request.urlopen(req) as response:
            sys.stderr.write("Getting data")
            
            data = str(response.read())
            sys.stderr.write(f"Response: {data}")
            
            return json.dumps(data)

        return json.loads('{"teste": 123}')

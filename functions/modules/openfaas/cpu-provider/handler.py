import json, urllib

URL = "http://localhost:8080/function/cpu-provider"

def handle(req):

        headers = {"content-type": "application/json"}
        req = urllib.request.Request(URL, headers=headers)
        with urllib.request.urlopen(req) as response:
            data = str(response.read())
            print(f"Response: {data}")
            
            return json.loads(data)

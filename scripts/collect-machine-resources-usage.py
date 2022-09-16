import psutil, urllib.request, socket, json, traceback

URL = "http://localhost:8080/resources"
HOSTNAME = socket.gethostname()

while True:

    try:
        cpu_percent = psutil.cpu_percent(interval=1)
        print(f"Current cpu: {cpu_percent}")

        headers = {"content-type": "application/json"}
        data = json.dumps({"hostname": HOSTNAME, "cpu": cpu_percent}).encode("utf-8")
        req = urllib.request.Request(URL, data=data, headers=headers)
        with urllib.request.urlopen(req) as response:
            print(f"Response: {str(response.read())}")

    except Exception as e:
        print(f"Unexpected problem happened: {traceback.format_exc()}")

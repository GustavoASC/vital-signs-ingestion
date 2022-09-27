import urllib.request, json, traceback

URL = "http://localhost:8097/vital-sign"

while True:

    try:
        headers = {"content-type": "application/json"}
        data = json.dumps(
            {"vital_sign": '{"temperature": 100}', "user_priority": 3}
        ).encode("utf-8")
        req = urllib.request.Request(URL, data=data, headers=headers)
        with urllib.request.urlopen(req) as response:
            print(f"Response: {str(response.read())}")

    except Exception as e:
        print(f"Unexpected problem happened: {traceback.format_exc()}")

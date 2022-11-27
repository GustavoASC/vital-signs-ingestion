import json
import boto3
import datetime
from urllib import request, error

RESULTS_URL = "https://webhook.site/5ac82c04-796e-412f-a0cd-7272da2f9176"

client = boto3.client("lambda")


def invoke_fn(fn_name, payload):
    print("Invoking function " + fn_name + "...")
    client.invoke(
        FunctionName=fn_name,
        Payload=payload,
    )


def current_timestamp():
    return int(datetime.datetime.now().timestamp() * 1000)

def notify_vital_sign_processed(id, initial_service_timestamp):
    try:
        end_timestamp = current_timestamp()
        data = {
            "initial_service_timestamp": initial_service_timestamp,
            "end_timestamp": end_timestamp
        }
        print(data)
        req = request.Request(
            url="{}/{}".format(RESULTS_URL, id),
            data=json.dumps(data).encode("utf-8"),
            method="PATCH",
        )
        request.urlopen(req)
    except error.HTTPError as e:
        body = e.read().decode()
        print(body)


def lambda_handler(event, context):
    print(event)

    body = event["Records"][0]["body"]
    payload = json.loads(body)

    initial_service_timestamp = current_timestamp()
    invoke_fn(payload["service_name"], payload["vital_sign"])
    notify_vital_sign_processed(payload["id"], initial_service_timestamp)

    return {"statusCode": 200, "body": json.dumps("")}

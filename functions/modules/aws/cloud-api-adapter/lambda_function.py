import json
import boto3
import datetime
from urllib import request, error

RESULTS_URL = "https://webhook.site/42189449-4fef-42ca-b1f6-23ff5493c405"

client = boto3.client("lambda")
health_services = ["body-temperature-monitor"]


def invoke_fn(fn_name, payload):
    print("Invoking function " + fn_name + "...")
    client.invoke(
        FunctionName=fn_name,
        Payload=payload,
    )


def notify_vital_sign_processed(id):
    try:
        end_timestamp = int(datetime.datetime.now().timestamp() * 1000)
        data = {"end_timestamp": end_timestamp}
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

    body = event["body"]
    payload = json.loads(body)

    if "service_name" in payload:
        invoke_fn(payload["service_name"], payload["vital_sign"])
    else:
        for service in health_services:
            invoke_fn(service, payload["vital_sign"])

    notify_vital_sign_processed(payload["id"])

    return {"statusCode": 200, "body": json.dumps("")}

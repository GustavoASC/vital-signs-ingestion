import json
import boto3
import datetime
from urllib import request, error

RESULTS_URL = "http://ec2-18-231-189-223.sa-east-1.compute.amazonaws.com:9095"

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
    keep_trying = True
    while keep_trying:
        try:
            end_timestamp = current_timestamp()
            data = {
                "initial_service_timestamp": initial_service_timestamp,
                "end_timestamp": end_timestamp,
                "origin": "cloud"
            }
            print(data)
            req = request.Request(
                url="{}/{}".format(RESULTS_URL, id),
                data=json.dumps(data).encode("utf-8"),
                method="PATCH",
            )
            with request.urlopen(req):
                keep_trying = False
        except error.HTTPError as e:
            body = e.read().decode()
            print(body)
            print("Will retry...")


def lambda_handler(event, context):
    print(event)

    body = event["Records"][0]["body"]
    payload = json.loads(body)

    initial_service_timestamp = current_timestamp()
    invoke_fn(payload["service_name"], payload["vital_sign"])
    notify_vital_sign_processed(payload["id"], initial_service_timestamp)

    return {"statusCode": 200, "body": json.dumps("")}

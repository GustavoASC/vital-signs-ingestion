import json
import boto3


client = boto3.client("lambda")
health_services = ["body-temperature-monitor"]


def invoke_fn(fn_name, payload):
    print("Invoking function " + fn_name + "...")
    client.invoke(
        FunctionName=fn_name,
        Payload=payload,
    )


def lambda_handler(event, context):
    print(event)

    body = event["body"]
    payload = json.loads(body)

    if "service_name" in payload:
        invoke_fn(payload["service_name"], payload["vital_sign"])
    else:
        for service in health_services:
            invoke_fn(service, payload["vital_sign"])

    return {"statusCode": 200, "body": json.dumps("Hello from Lambda!")}

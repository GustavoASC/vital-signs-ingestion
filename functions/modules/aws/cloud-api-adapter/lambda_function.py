import json
import boto3


client = boto3.client("lambda")
health_services = ["body-temperature-monitor"]


def invoke_fn(fn_name, payload):
    client.invoke(
        FunctionName=fn_name,
        Payload=payload,
    )


def lambda_handler(event, context):

    if "service_name" in event:
        invoke_fn(event["service_name"], event["vital_sign"])
    else:
        for service in health_services:
            invoke_fn(service, event["vital_sign"])

    return {"statusCode": 200, "body": json.dumps("Hello from Lambda!")}

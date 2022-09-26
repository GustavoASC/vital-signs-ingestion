import json

MAX_TEMPERATURE = 38
MIN_TEMPERATURE = 35

def alert(message):
    return json.dumps({"send_notification": True, "message": message})


def lambda_handler(event, context):

    if event["temperature"] > MAX_TEMPERATURE:
        return alert("Has fever")
    elif event["temperature"] < MIN_TEMPERATURE:
        return alert("Has hypothermia")
    else:
        return json.dumps({"send_notification": False})

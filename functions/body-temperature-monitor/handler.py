import json

MAX_TEMPERATURE = 38
MIN_TEMPERATURE = 35

def alert(message):
    return json.dumps({"send_notification": True, "message": message})


def handle(req):
    """Checks if body temperature is out of appropriate range
    Args:
        req (str): request body
    """

    vital_sign = json.loads(req)

    if vital_sign["temperature"] > MAX_TEMPERATURE:
        return alert("Has fever")
    elif vital_sign["temperature"] < MIN_TEMPERATURE:
        return alert("Has hypothermia")
    else:
        return json.dumps({"send_notification": False})

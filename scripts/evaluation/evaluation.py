import urllib.request, json, traceback

URL = "http://localhost:8080/function/service-executor"

MANCHESTER_NOT_URGENT = 1
MANCHESTER_LITTLE_URGENT = 2
MANCHESTER_URGENT = 3
MANCHESTER_VERY_URGENT = 4
MANCHESTER_EMERGENCY = 5

def ingest_vital_sign(payload):
    try:
        headers = {"content-type": "application/json"}
        data = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(URL, data=data, headers=headers)
        with urllib.request.urlopen(req) as response:
            print(f"Response: {str(response.read())}")
    except Exception as e:
        print(f"Unexpected problem happened: {traceback.format_exc()}")


def scenario_one():
    """
    Large amount of both critical and non-critical requests
    """
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_EMERGENCY,
            }
        )
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_VERY_URGENT,
            }
        )
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_URGENT,
            }
        )
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_LITTLE_URGENT,
            }
        )
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_NOT_URGENT,
            }
        )

def scenario_two():
    """
    Large amount of critical requests and few non-critical requests
    """
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_EMERGENCY,
            }
        )
    for i in range(1000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_VERY_URGENT,
            }
        )
    for i in range(100):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_URGENT,
            }
        )
    for i in range(10):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_LITTLE_URGENT,
            }
        )
    for i in range(1):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_NOT_URGENT,
            }
        )


def scenario_three():
    """
    Few amount of critical requests and large amount of non-critical
    """
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_NOT_URGENT,
            }
        )
    for i in range(1000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_LITTLE_URGENT,
            }
        )
    for i in range(100):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_URGENT,
            }
        )
    for i in range(10):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_VERY_URGENT,
            }
        )
    for i in range(1):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_EMERGENCY,
            }
        )


def scenario_four():
    """
    Small amount of both critical and non-critical request
    """
    for i in range(100):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_NOT_URGENT,
            }
        )
    for i in range(100):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_EMERGENCY,
            }
        )


def scenario_five():
    """
    Requests with the same priority for different services
    """
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "body-temperature-monitor",
                "user_priority": MANCHESTER_NOT_URGENT,
            }
        )
    for i in range(10000):
        ingest_vital_sign(
            {
                "vital_sign": '{"temperature": 100}',
                "service_name": "cardiac-insufficiency-predictor",
                "user_priority": MANCHESTER_NOT_URGENT,
            }
        )


if __name__ == "__main__":
    scenario_one()
    scenario_two()
    scenario_three()
    scenario_four()
    scenario_five()

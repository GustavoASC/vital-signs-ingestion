import json
from statsmodels.tsa.api import Holt

MIN_DATA_POINTS = 2


def _round_values(forecast):
    return [round(elem, 2) for elem in forecast]


def _wrap_response(forecast_dict):
    return {
        "statusCode": 200,
        "headers": {"Content-type": "application/json"},
        "body": json.dumps(forecast_dict),
    }


def handle(event, _context):

    input = json.loads(event.body)

    if not input["data"] or len(input["data"]) < MIN_DATA_POINTS:
        return _wrap_response({"forecast": []})

    fit = Holt(input["data"], initialization_method="estimated").fit(
        smoothing_level=input["smoothing_level"],
        smoothing_trend=input["smoothing_trend"],
        optimized=False,
    )
    forecast = fit.forecast(input["future_data_points"]).tolist()
    return _wrap_response({"forecast": _round_values(forecast)})

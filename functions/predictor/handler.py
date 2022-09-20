import json
from statsmodels.tsa.api import ExponentialSmoothing, SimpleExpSmoothing, Holt

MIN_DATA_POINTS = 2


def _round_values(forecast):
    return [round(elem, 2) for elem in forecast]


def handle(req):
    input = json.loads(req)

    if not input["data"] or len(input["data"]) < MIN_DATA_POINTS:
        return json.dumps({"forecast": []})

    fit = Holt(input["data"], initialization_method="estimated").fit(
        smoothing_level=input["smoothing_level"],
        smoothing_trend=input["smoothing_trend"],
        optimized=False,
    )
    forecast = fit.forecast(input["future_data_points"]).tolist()
    return json.dumps({"forecast": _round_values(forecast)})

import json
from statsmodels.tsa.api import ARIMA

MIN_SPO2_THRESHOLD = 95
MIN_HEARTBEAT_THRESHOLD = 60
MAX_HEARTBEAT_THRESHOLD = 100

#
# For this experiment the values are hardcoded, but in a real production environment they would be
# fetched from a database with historical duration of vital signs for this person.
#
HARDCODED_HISTORICAL_SPO2 = [93.91, 94.27, 93.34, 95.02, 96.89, 97.49, 97.04, 99.1, 96.45, 95.04, 96.28, 95.89, 96.91, 93.37, 99.85, 94.77, 97.07, 94.67, 98.74, 93.45, 94.92, 94.38, 97.43, 95.71, 95.78, 95.92, 96.03, 95.05, 95.8, 95.71, 99.09, 93.38, 96.49, 99.05, 93.62, 94.87, 93.16, 95.05, 98.38, 97.45, 94.36, 93.45, 96.28, 94.96, 95.06, 99.62, 98.2, 98.02, 97.05, 97.67, 98.19, 96.87, 98.59, 99.84, 97.31, 97.29, 94.39, 98.59, 93.07, 93.32, 98.74, 99.91, 98.64, 94.81, 98.25, 96.31, 98.11, 98.59, 96.46, 99.05, 93.35, 98.06, 95.83, 95.42, 96.82, 99.46, 97.12, 99.85, 97.9, 97.08, 99.06, 93.54, 95.44, 99.97, 97.89, 97.97, 97.89, 96.74, 98.32, 98.89, 95.85, 98.7, 96.7, 99.09, 94.04, 95.5, 97.71, 95.9, 94.37, 97.89, 99.53, 93.08, 97.35, 98.02, 93.32, 93.75, 96.16, 97.98, 96.85, 97.53, 97.84, 98.41, 99.6, 98.99, 95.37, 99.11, 94.14, 94.55, 94.99, 97.74, 93.39, 99.55, 97.25, 99.38, 98.07, 95.19, 98.15, 99.78, 96.93, 94.99, 95.2, 95.75, 96.71, 94.25, 96.83, 95.7, 94.39, 96.68, 96.78, 94.14, 94.92, 94.91, 97.49, 94.2, 94.61, 94.84, 94.14, 97.11, 93.44, 94.32, 94.35, 97.55, 98.05, 99.47, 97.86, 99.95, 94.71, 94.57, 97.65, 96.02, 94.05, 95.84, 98.47, 95.6, 93.21, 99.67, 93.21, 97.19, 94.02, 96.37, 93.44, 94.52, 96.65, 99.41, 99.85, 99.08, 94.52, 93.02, 97.16, 94.32, 99.16, 99.48, 94.63, 95.84, 97.45, 93.17, 96.34, 99.61, 96.33, 97.09, 93.44, 99.93, 97.09, 93.65, 94.99, 98.24, 93.2, 96.34, 97.2, 95.6, 98.35, 98.64, 95.13, 93.42, 99.99, 95.17, 99.28, 97.29, 93.03, 99.84, 97.12, 97.38, 96.18, 96.62, 99.85, 99.36, 93.77, 99.14, 95.39, 94.11, 96.97, 99.99, 97.8, 98.11, 99.28, 98.45, 95.05, 94.24, 98.51, 99.54, 97.11, 96.1, 96.05, 98.69, 96.44, 95.72, 96.82, 96.66, 98.43, 97.74, 97.11, 97.02, 96.03, 93.92, 97.97, 94.23, 96.32, 97.01, 97.85, 95.65, 93.48, 99.5, 96.94, 98, 99.49, 96.15, 95.45, 97.77, 96.05, 96.27, 94.6, 93.36, 95.68, 93.22, 95.32, 98.1, 95.2, 94.17, 95.16, 99.47, 93.22, 94.04, 95.79, 99.64, 96.15, 93.6, 99.1, 97.05, 98.43, 97.6, 96.39, 97.26, 98.07, 99.61, 98.25, 93.31, 99.93, 96.32, 98.06, 94.44, 97.17, 94.41, 97.74, 96.39, 99.38, 93.9, 96.06, 97.9, 98.98, 93.26, 97.36, 98.67, 96.87, 97.66, 95.99, 96.69, 96.32, 99.78, 93.08, 97.44, 97.33, 98.85, 94.01, 97.36, 95.46, 96.77, 99.77, 96.67, 99.17, 97.43, 94.44, 98.99, 99.63, 93.99, 96.6, 97.43, 96.41, 99.47, 94.77, 96.86, 99.4, 98.42, 96.97, 93.22, 97.63, 99.92, 99.76, 93.03, 96.51, 95.65, 99.93, 98.68, 93.63, 95.21, 99.21, 99.01, 98.09, 98.35, 94.15, 99.46, 95.19, 99.71, 99.04, 94.68, 93.76, 95.84, 98.83, 97.78, 97.81, 95.59, 97.9, 97.96, 94.5, 98.51, 97.09, 97.73, 97.54, 98.53, 93.12, 95.77, 96.32, 97.76, 99.22, 94.92, 99.96, 95.99, 97.43, 94.57, 97.41, 98.47, 95.84, 99.3, 98.36, 96.42, 99.14, 98.56, 95.7, 96.93, 98.9, 96.39, 98.75, 95.74, 97.36, 95.68, 99.39, 94.75, 96.92, 96.34, 98.04, 96.62]
HARDCODED_HISTORICAL_HEARTBEAT = [98, 92, 103, 103, 104, 104, 80, 94, 108, 82, 108, 86, 86, 80, 93, 99, 99, 105, 105, 107, 82, 103, 91, 101, 107, 85, 101, 105, 83, 98, 99, 110, 106, 83, 85, 100, 100, 107, 102, 109, 100, 99, 88, 96, 96, 87, 92, 85, 98, 108, 89, 105, 86, 103, 89, 100, 108, 89, 110, 80, 82, 96, 83, 93, 104, 88, 96, 101, 110, 87, 107, 101, 98, 108, 97, 80, 100, 92, 107, 81, 109, 97, 90, 103, 101, 90, 88, 86, 82, 84, 83, 109, 91, 95, 90, 110, 90, 82, 101, 97, 86, 95, 94, 82, 110, 96, 101, 106, 83, 96, 94, 97, 97, 82, 104, 89, 102, 99, 110, 107, 89, 104, 101, 80, 103, 107, 101, 91, 88, 105, 82, 109, 81, 85, 108, 88, 84, 84, 83, 88, 88, 102, 87, 96, 109, 107, 104, 108, 107, 109, 90, 80, 106, 100, 91, 97, 87, 86, 102, 101, 80, 91, 104, 82, 86, 96, 96, 81, 93, 89, 108, 95, 109, 104, 101, 84, 89, 89, 86, 106, 109, 81, 103, 107, 88, 87, 108, 85, 90, 81, 88, 97, 86, 96, 103, 100, 85, 84, 98, 84, 103, 108, 86, 97, 102, 99, 81, 91, 96, 100, 90, 109, 88, 83, 105, 80, 84, 99, 80, 102, 99, 107, 87, 105, 92, 82, 96, 83, 80, 108, 108, 81, 97, 97, 84, 93, 95, 97, 89, 110, 104, 85, 93, 103, 108, 87, 90, 85, 92, 97, 101, 86, 104, 97, 106, 110, 89, 108, 101, 96, 95, 105, 108, 84, 104, 98, 84, 93, 88, 92, 101, 80, 96, 97, 91, 102, 89, 107, 84, 82, 90, 85, 92, 83, 110, 86, 89, 108, 83, 97, 88, 110, 80, 86, 96, 80, 85, 86, 102, 92, 105, 105, 87, 91, 97, 99, 95, 85, 88, 103, 82, 110, 102, 108, 89, 101, 106, 84, 89, 92, 85, 84, 106, 101, 105, 87, 109, 101, 96, 106, 81, 109, 109, 87, 87, 102, 89, 95, 108, 102, 93, 97, 92, 95, 101, 85, 84, 105, 84, 108, 80, 81, 94, 90, 82, 105, 102, 102, 94, 92, 92, 109, 106, 81, 80, 93, 105, 97, 93, 93, 91, 81, 92, 99, 95, 110, 84, 81, 85, 80, 95, 101, 90, 98, 88, 100, 104, 96, 88, 96, 93, 105, 95, 95, 95, 101, 95, 95, 94, 100]

def forecast(input_dataset):
    mod = ARIMA(input_dataset, order=(1, 0, 0), trend='ct')
    res = mod.fit()
    return res.forecast()


def get_spo2_historical_data():
    return HARDCODED_HISTORICAL_SPO2


def get_heartbeat_historical_data():
    return HARDCODED_HISTORICAL_HEARTBEAT


def alert(message):
    return _wrap_response({"send_notification": True, "message": message})

def _wrap_response(body_dict):
    return {
        "statusCode": 200,
        "headers": {"Content-type": "application/json"},
        "body": json.dumps(body_dict),
    }



def handle(event, _context):
    """
    Predicts if the person will have a heart failure in the next few minutes
    """

    vital_sign = json.loads(event.body)

    forecast_spo2 = forecast(get_spo2_historical_data() + [vital_sign["spo2"]])
    forecast_heartbeat = forecast(get_heartbeat_historical_data() + [vital_sign["heartbeat"]])

    if forecast_spo2 < MIN_SPO2_THRESHOLD and (forecast_heartbeat < MIN_HEARTBEAT_THRESHOLD or forecast_heartbeat > MAX_HEARTBEAT_THRESHOLD):
        return alert("Will possibly have heart failure")
    else:
        return _wrap_response({"send_notification": False})

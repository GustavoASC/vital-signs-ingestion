import time, utils
import datetime as dt

TOTAL_EXECUTIONS = 40000


def _await_executions_finish(results_machine_ip):

    awaiting = True
    while awaiting:
        payload = utils.get(f"http://{results_machine_ip}:9095/results/summary")

        total_processed = payload["total_finished_requests"]
        if total_processed != TOTAL_EXECUTIONS:
            time.sleep(30)
        else:
            awaiting = False


def collect_async_results_awaiting(results_machine_ip):
    _await_executions_finish(results_machine_ip)
    return utils.get(f"http://{results_machine_ip}:9095/results")


def clear_all_results(results_machine_ip):
    utils.post(f"http://{results_machine_ip}:9095/clear", {})


def analyze_dataset(payload):
    all_data = {}
    for id in payload["finished_requests"]:
        request_data = payload["finished_requests"][id]

        user_priority_data = _get_dict_from_dict(
            all_data, str(request_data["user_priority"])
        )

        _get_array_from_dict(user_priority_data, "elapsed").append(
            request_data["result_received_at"] - request_data["start_timestamp"]
        )
        _get_array_from_dict(user_priority_data, "start_datetime").append(
            dt.datetime.fromtimestamp(request_data["start_timestamp"] / 1e3)
        )
        _get_array_from_dict(user_priority_data, "end_datetime").append(
            dt.datetime.fromtimestamp(request_data["result_received_at"] / 1e3)
        )

    return all_data


def _get_dict_from_dict(data, field_name):
    result_dict = data.get(field_name)
    if result_dict is None:
        result_dict = {}
        data[field_name] = result_dict

    return result_dict


def _get_array_from_dict(data, field_name):
    result_array = data.get(field_name)
    if result_array is None:
        result_array = []
        data[field_name] = result_array

    return result_array


if __name__ == "__main__":
    data = collect_async_results_awaiting("localhost")
    analyzed = analyze_dataset(data)
    pass

import handler
import pathlib

# Test your handler here
CURRENT_DIR = str(pathlib.Path(__file__).parent.resolve())

# To disable testing, you can set the build_arg `TEST_ENABLED=false` on the CLI or in your stack.yml
# https://docs.openfaas.com/reference/yaml/#function-build-args-build-args


def _load_test_resource(name):
    with open(CURRENT_DIR + "/test-resources/" + name, "r") as file:
        return file.read()


def test_prediction_empty_data():
    input = _load_test_resource("input-prediction-empty-data.json")
    expected = '{"forecast": []}'
    assert handler.handle(input) == expected


def test_prediction_single_data_point():
    input = _load_test_resource("input-prediction-single-data-point.json")
    expected = '{"forecast": []}'
    assert handler.handle(input) == expected


def test_prediction_two_data_points():
    input = _load_test_resource("input-prediction-two-data-points.json")
    expected = '{"forecast": [736.25]}'
    response = handler.handle(input)
    assert response == expected


def test_prediction_several_data_points_single_future_value():
    input = _load_test_resource(
        "input-prediction-several-data-points-single-future-value.json"
    )
    expected = '{"forecast": [43.63]}'
    response = handler.handle(input)
    assert response == expected


def test_prediction_several_data_points_multiple_future_values():
    input = _load_test_resource(
        "input-prediction-several-data-points-multiple-future-values.json"
    )
    expected = '{"forecast": [43.63, 45.36, 47.1, 48.84, 50.58]}'
    response = handler.handle(input)
    assert response == expected


if __name__ == "__main__":
    test_prediction_empty_data()
    test_prediction_single_data_point()
    test_prediction_two_data_points()
    test_prediction_several_data_points_multiple_future_values()
    test_prediction_several_data_points_single_future_value()

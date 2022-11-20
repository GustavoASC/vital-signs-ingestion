import handler
import datetime

# Test your handler here

# To disable testing, you can set the build_arg `TEST_ENABLED=false` on the CLI or in your stack.yml
# https://docs.openfaas.com/reference/yaml/#function-build-args-build-args


def test_heart_failure_prediction():
    input = '{"heartbeat": 97, "spo2": 99}'
    expected = '{"send_notification": false}'
    assert handler.handle(input) == expected


if __name__ == "__main__":
    test_heart_failure_prediction()

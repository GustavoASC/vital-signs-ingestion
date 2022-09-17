import handler

# Test your handler here

# To disable testing, you can set the build_arg `TEST_ENABLED=false` on the CLI or in your stack.yml
# https://docs.openfaas.com/reference/yaml/#function-build-args-build-args


def test_fever():
    input = '{"temperature": 40}'
    expected = '{"send_notification": true, "message": "Has fever"}'
    assert handler.handle(input) == expected


def test_hypothermia():
    input = '{"temperature": 34}'
    expected = '{"send_notification": true, "message": "Has hypothermia"}'
    assert handler.handle(input) == expected


def test_temperature_within_ranges():
    input = '{"temperature": 37}'
    expected = '{"send_notification": false}'
    assert handler.handle(input) == expected


if __name__ == "__main__":
    test_fever()
    test_hypothermia()
    test_temperature_within_ranges()

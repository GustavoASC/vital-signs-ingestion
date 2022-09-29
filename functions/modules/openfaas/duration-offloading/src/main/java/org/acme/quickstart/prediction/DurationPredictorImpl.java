package org.acme.quickstart.prediction;

import java.util.Formatter;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ApplicationScoped
public class DurationPredictorImpl implements DurationPredictor {

    private static final String PREDICTION_INPUT_TEMPLATE = "{\"data\": %s,\"smoothing_level\": 0.8,\"smoothing_trend\": 0.2,\"future_data_points\": 1}";

    private final static String PREDICTOR_FN = "predictor";
    private final static int MIN_HISTORICAL_DURATIONS = 2;

    private final ServerlessFunctionClient serverlessFunctionClient;

    public DurationPredictorImpl(
            @RestClient ServerlessFunctionClient serverlessFunctionClient) {
        this.serverlessFunctionClient = serverlessFunctionClient;
    }

    @Override
    public long predictDurationInMillis(List<Long> durationsForService, String service)
            throws CouldNotPredictDurationException {

        if (durationsForService.size() < MIN_HISTORICAL_DURATIONS) {
            throw new CouldNotPredictDurationException();
        }

        try (var formatter = new Formatter().format(PREDICTION_INPUT_TEMPLATE, durationsForService)) {
            String payload = formatter.toString();
            String result = serverlessFunctionClient.runFunction(PREDICTOR_FN, payload);
            return extractFirstForecast(result);
        }
    }

    private long extractFirstForecast(String result) {
        JsonObject element = JsonParser.parseString(result).getAsJsonObject();
        JsonArray array = element.get("forecast").getAsJsonArray();
        return array.get(0).getAsLong();
    }

}

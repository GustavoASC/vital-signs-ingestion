package org.acme;

import java.time.Duration;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DurationPredictorImpl implements DurationPredictor {

    private final static String PREDICTOR_FN = "predictor";
    private final static int MIN_HISTORICAL_DURATIONS = 2;

    private final RunningServicesProvider runningServices;
    private final ServerlessFunctionClient serverlessFunctionClient;

    public DurationPredictorImpl(
            RunningServicesProvider runningServices,
            ServerlessFunctionClient serverlessFunctionClient) {
        this.runningServices = runningServices;
        this.serverlessFunctionClient = serverlessFunctionClient;
    }

    @Override
    public long predictDurationInMillis(String service) throws CouldNotPredictDurationException {

        List<Long> durations = getDurations(service);
        if (durations.size() < MIN_HISTORICAL_DURATIONS) {
            throw new CouldNotPredictDurationException();
        }

        String payload = """
                {
                    "data": %s,
                    "smoothing_level": 0.8,
                    "smoothing_trend": 0.2,
                    "future_data_points": 1
                }
                """.trim().formatted(durations);

        String result = serverlessFunctionClient.runFunction(PREDICTOR_FN, payload);
        return extractFirstForecast(result);
    }

    private List<Long> getDurations(String service) {
        return runningServices.getDurationsForService(service)
                .stream()
                .map(Duration::toMillis)
                .toList();
    }

    private long extractFirstForecast(String result) {
        JsonObject element = JsonParser.parseString(result).getAsJsonObject();
        JsonArray array = element.get("forecast").getAsJsonArray();
        return array.get(0).getAsLong();
    }

}

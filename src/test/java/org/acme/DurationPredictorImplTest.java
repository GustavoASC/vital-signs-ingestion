package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DurationPredictorImplTest {

    @Mock
    private RunningServicesProvider runningServicesProvider;

    @Mock
    private ServerlessFunctionClient serverlessFunctionClient;

    @InjectMocks
    private DurationPredictorImpl predictorImpl;

    @AfterEach
    public void afterEach() {
        verifyNoMoreInteractions(
                serverlessFunctionClient,
                runningServicesProvider);
    }

    @Test
    public void shouldThrowExceptionWithSingleHistoricalDuration() {

        when(runningServicesProvider.getDurationsForService("foo"))
                .thenReturn(durations(17));

        assertThatThrownBy(() -> predictorImpl.predictDurationInMillis("foo"))
                .isInstanceOf(CouldNotPredictDurationException.class);
    }

    @Test
    public void shouldThrowExceptionWithNoHistoricalDuration() {

        when(runningServicesProvider.getDurationsForService("foo"))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> predictorImpl.predictDurationInMillis("foo"))
                .isInstanceOf(CouldNotPredictDurationException.class);
    }

    @ParameterizedTest
    @CsvSource({ "output-single-forecast-integer.json", "output-single-forecast-with-decimal.json" })
    public void shouldPredictMultipleValuesWithSingleForecastAsInteger(String responseFile) throws Throwable {

        when(runningServicesProvider.getDurationsForService("foo"))
                .thenReturn(durations(17, 21, 23, 26, 26, 28, 30, 30, 30, 31, 32, 33, 39, 41, 41));

        var fnName = "predictor";
        var payload = jsonFromResource("input-multiple-values.json");
        var response = jsonFromResource(responseFile);
        when(serverlessFunctionClient.runFunction(fnName, payload))
                .thenReturn(response);

        assertThat(predictorImpl.predictDurationInMillis("foo"))
                .isEqualTo(43);
    }

    private List<Duration> durations(long... durations) {
        return Arrays.stream(durations)
                .mapToObj(Duration::ofMillis)
                .toList();
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/duration-predictor", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}

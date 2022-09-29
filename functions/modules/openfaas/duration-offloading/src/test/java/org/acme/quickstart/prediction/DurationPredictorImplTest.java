package org.acme.quickstart.prediction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
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
    private ServerlessFunctionClient serverlessFunctionClient;

    @InjectMocks
    private DurationPredictorImpl predictorImpl;

    @AfterEach
    public void afterEach() {
        verifyNoMoreInteractions(serverlessFunctionClient);
    }

    @Test
    public void shouldThrowExceptionWithSingleHistoricalDuration() {

        assertThatThrownBy(() -> predictorImpl.predictDurationInMillis(List.of(17l), "foo"))
                .isInstanceOf(CouldNotPredictDurationException.class);
    }

    @Test
    public void shouldThrowExceptionWithNoHistoricalDuration() {

        assertThatThrownBy(() -> predictorImpl.predictDurationInMillis(Collections.emptyList(), "foo"))
                .isInstanceOf(CouldNotPredictDurationException.class);
    }

    @ParameterizedTest
    @CsvSource({ "output-single-forecast-integer.json", "output-single-forecast-with-decimal.json" })
    public void shouldPredictMultipleValuesWithSingleForecastAsInteger(String responseFile) throws Throwable {

        var fnName = "predictor";
        var payload = jsonFromResource("input-multiple-values.json");
        var response = jsonFromResource(responseFile);
        when(serverlessFunctionClient.runFunction(fnName, payload))
                .thenReturn(response);

        assertThat(predictorImpl.predictDurationInMillis(List.of(17l, 21l, 23l, 26l, 26l, 28l, 30l, 30l, 30l, 31l, 32l, 33l, 39l, 41l, 41l), "foo"))
                .isEqualTo(43);
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/duration-predictor", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}

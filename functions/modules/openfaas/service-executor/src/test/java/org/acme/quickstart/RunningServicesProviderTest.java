package org.acme.quickstart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.acme.quickstart.results.Results;
import org.acme.quickstart.results.ResultsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RunningServicesProviderTest {

    @Mock
    Clock clock;

    @Mock
    ResultsClient resultsClient;

    @InjectMocks
    RunningServicesProviderImpl runningServicesProvider;

    @Captor
    ArgumentCaptor<String> idCaptor;

    @Captor
    ArgumentCaptor<Results> resultsCaptor;

    @Test
    public void shouldReturnEmptyListWhenNoServiceIsRunning() {

        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEmpty();
    }

    @Test
    public void shouldRemoveNonExistingExecutionId() {

        runningServicesProvider.executionFinished(UUID.randomUUID());
        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEmpty();
    }

    @Test
    public void shouldRetrieveRankingAfterTheyAreStoredButRemovingWithId() {

        when(clock.instant())
                .thenReturn(Instant.ofEpochMilli(1663527788128l));

        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        var third = UUID.randomUUID();
        var fourth = UUID.randomUUID();
        var fifth = UUID.randomUUID();
        
        runningServicesProvider.executionStarted(first, "body-temperature-monitor", 1);
        runningServicesProvider.executionStarted(second, "body-temperature-monitor", 7);
        runningServicesProvider.executionStarted(third, "body-temperature-monitor", 9);
        runningServicesProvider.executionStarted(fourth, "bar-function", 15);
        runningServicesProvider.executionStarted(fifth, "bar-function", 2);

        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEqualTo(List.of(1, 7, 9, 15, 2));
        assertThat(runningServicesProvider.getRunningServices())
                .isEqualTo(List.of(
                        new RunningServicesProvider.ServiceExecution("body-temperature-monitor", 1),
                        new RunningServicesProvider.ServiceExecution("body-temperature-monitor", 7),
                        new RunningServicesProvider.ServiceExecution("body-temperature-monitor", 9),
                        new RunningServicesProvider.ServiceExecution("bar-function", 15),
                        new RunningServicesProvider.ServiceExecution("bar-function", 2)
        ));

        runningServicesProvider.executionFinished(first);
        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEqualTo(List.of(7, 9, 15, 2));

        assertThat(runningServicesProvider.getRunningServices())
                .isEqualTo(List.of(
                        new RunningServicesProvider.ServiceExecution("body-temperature-monitor", 7),
                        new RunningServicesProvider.ServiceExecution("body-temperature-monitor", 9),
                        new RunningServicesProvider.ServiceExecution("bar-function", 15),
                        new RunningServicesProvider.ServiceExecution("bar-function", 2)
        ));

        runningServicesProvider.executionFinished(second);
        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEqualTo(List.of(9, 15, 2));
        assertThat(runningServicesProvider.getRunningServices())
                .isEqualTo(List.of(
                        new RunningServicesProvider.ServiceExecution("body-temperature-monitor", 9),
                        new RunningServicesProvider.ServiceExecution("bar-function", 15),
                        new RunningServicesProvider.ServiceExecution("bar-function", 2)
        ));

        runningServicesProvider.executionFinished(third);
        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEqualTo(List.of(15, 2));
        assertThat(runningServicesProvider.getRunningServices())
                .isEqualTo(List.of(
                        new RunningServicesProvider.ServiceExecution("bar-function", 15),
                        new RunningServicesProvider.ServiceExecution("bar-function", 2)
        ));

        runningServicesProvider.executionFinished(fourth);
        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEqualTo(List.of(2));
        assertThat(runningServicesProvider.getRunningServices())
                .isEqualTo(List.of(
                        new RunningServicesProvider.ServiceExecution("bar-function", 2)
        ));

        runningServicesProvider.executionFinished(fifth);
        assertThat(runningServicesProvider.getRankingsForRunningServices())
                .isEmpty();
        assertThat(runningServicesProvider.getRunningServices())
                .isEmpty();

    }

    @Test
    public void shouldStoreDurationForServiceExecutions() throws Exception {

        when(clock.instant())
                .thenReturn(
                        Instant.ofEpochMilli(1663527788128l),
                        Instant.ofEpochMilli(1663527789759l));

        doNothing().when(resultsClient)
                .sendResults(idCaptor.capture(), resultsCaptor.capture());

        var id = UUID.fromString("84cf7632-e76d-4c3c-8fee-cc16e6e1c41e");
        runningServicesProvider.executionStarted(id, "body-temperature-monitor", 7);
        runningServicesProvider.executionFinished(id);

        assertThat(runningServicesProvider.getDurationsForService("body-temperature-monitor"))
                .isEqualTo(List.of(Duration.ofMillis(1631l)));

        assertThat(idCaptor.getValue())
                .isEqualTo("84cf7632-e76d-4c3c-8fee-cc16e6e1c41e");
        
        Results results = new Results();
        results.initialServiceTimestamp = 1663527788128l;
        results.endTimestamp = 1663527789759l;

        assertThat(resultsCaptor.getValue())
                .isEqualTo(results);
    }
}

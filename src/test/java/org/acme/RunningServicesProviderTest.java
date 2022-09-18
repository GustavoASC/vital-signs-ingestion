package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RunningServicesProviderTest {

    @Mock
    Clock clock;

    @InjectMocks
    RunningServicesProviderImpl runningServicesProvider;

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

        var first = runningServicesProvider.executionStarted("body-temperature-monitor", 1);
        var second = runningServicesProvider.executionStarted("body-temperature-monitor", 7);
        var third = runningServicesProvider.executionStarted("body-temperature-monitor", 9);
        var fourth = runningServicesProvider.executionStarted("bar-function", 15);
        var fifth = runningServicesProvider.executionStarted("bar-function", 2);

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

        runningServicesProvider.executionFinished(
                runningServicesProvider.executionStarted("body-temperature-monitor", 7));

        assertThat(runningServicesProvider.getDurationsForService("body-temperature-monitor"))
                .isEqualTo(List.of(Duration.ofMillis(1631l)));

    }
}

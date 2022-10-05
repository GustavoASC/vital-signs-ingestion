package org.acme.quickstart.offloading.duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.acme.quickstart.RunningServicesProvider;
import org.acme.quickstart.RunningServicesProvider.ServiceExecution;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffloadingHeuristicByDurationImplTest {
    private static final String FOO_SERVICE = "foo";
    private static final int RANKING = 7;
    private static final List<Duration> DURATIONS_OBJECT = List.of(Duration.ofMillis(235));
    private static final List<Long> DURATIONS_MILLIS = List.of(235l);

    @Mock
    RunningServicesProvider servicesProvider;

    @Mock
    ServerlessFunctionClient serverlessFunctionClient;

    @InjectMocks
    OffloadingHeuristicByDurationImpl offloadingHeuristicByDuration;

    @ParameterizedTest
    @MethodSource({"singleExecutionForService", "multipleExecutionsSameService"})
    public void shouldInvokeDurationHeuristic(List<ServiceExecution> executions) throws Throwable {

        when(servicesProvider.getRunningServices())
            .thenReturn(executions);

        when(servicesProvider.getDurationsForService(FOO_SERVICE))
            .thenReturn(DURATIONS_OBJECT);

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", anInputForSingleService()))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(RANKING, FOO_SERVICE))
            .isFalse();    
    }

    private OffloadDurationInputDto anInputForSingleService() {
        List<PreviousDurationInputDto> previousDurations = List.of(
                new PreviousDurationInputDto(FOO_SERVICE, DURATIONS_MILLIS));

        return new OffloadDurationInputDto(
                previousDurations,
                FOO_SERVICE);
    }

    private static Stream<Arguments> singleExecutionForService() {
        return Stream.of(Arguments.of(
                List.of(new ServiceExecution(FOO_SERVICE, RANKING))));
    }

    private static Stream<Arguments> multipleExecutionsSameService() {
        return Stream.of(Arguments.of(
                List.of(
                        new ServiceExecution(FOO_SERVICE, RANKING),
                        new ServiceExecution(FOO_SERVICE, RANKING))));
    }

}

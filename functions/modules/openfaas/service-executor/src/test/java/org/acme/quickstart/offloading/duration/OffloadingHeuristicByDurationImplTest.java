package org.acme.quickstart.offloading.duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.acme.quickstart.RunningServicesProvider;
import org.acme.quickstart.RunningServicesProvider.ServiceExecution;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.junit.jupiter.api.Test;
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
    private static final String BAR_SERVICE = "bar";
    private static final int RANKING_A = 7;
    private static final int RANKING_B = 14;
    private static final long DURATION_FOO_SERVICE = 235l;
    private static final long DURATION_BAR_SERVICE = 173l;

    @Mock
    RunningServicesProvider servicesProvider;

    @Mock
    ServerlessFunctionClient serverlessFunctionClient;

    @InjectMocks
    OffloadingHeuristicByDurationImpl offloadingHeuristicByDuration;

    @ParameterizedTest
    @MethodSource({"singleExecutionForService", "multipleExecutionsSameService"})
    public void shouldInvokeDurationHeuristicForSingleService(List<ServiceExecution> executions) throws Throwable {

        OffloadDurationInputDto inputForSingleService = new OffloadDurationInputDto(
            List.of(),
            List.of(DURATION_FOO_SERVICE)
        );

        when(servicesProvider.getRunningServices())
            .thenReturn(executions);

        when(servicesProvider.getDurationsForService(FOO_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_FOO_SERVICE)));

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", inputForSingleService))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(FOO_SERVICE))
            .isFalse();    
    }

    private static Stream<Arguments> singleExecutionForService() {
        return Stream.of(Arguments.of(
                List.of(new ServiceExecution(FOO_SERVICE, RANKING_A))));
    }

    private static Stream<Arguments> multipleExecutionsSameService() {
        return Stream.of(Arguments.of(
                List.of(
                        new ServiceExecution(FOO_SERVICE, RANKING_A),
                        new ServiceExecution(FOO_SERVICE, RANKING_A))));
    }

    @Test
    public void shouldInvokeDurationHeuristicForMultipleServicesWithDifferentRankings() throws Throwable {

        OffloadDurationInputDto inputForMultipleServices = new OffloadDurationInputDto(
            List.of(List.of(DURATION_BAR_SERVICE)),
            List.of(DURATION_FOO_SERVICE)
        );

        when(servicesProvider.getRunningServices())
            .thenReturn(List.of(
                new ServiceExecution(FOO_SERVICE, RANKING_A),
                new ServiceExecution(FOO_SERVICE, RANKING_A),
                new ServiceExecution(BAR_SERVICE, RANKING_B)
        ));

        when(servicesProvider.getDurationsForService(FOO_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_FOO_SERVICE)));

        when(servicesProvider.getDurationsForService(BAR_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_BAR_SERVICE)));

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", inputForMultipleServices))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(FOO_SERVICE))
            .isFalse();    
    }

    @Test
    public void shouldInvokeDurationHeuristicForMultipleServicesWithSameRankings() throws Throwable {

        OffloadDurationInputDto inputForMultipleServices = new OffloadDurationInputDto(
            List.of(List.of(DURATION_BAR_SERVICE)),
            List.of(DURATION_FOO_SERVICE)
        );

        when(servicesProvider.getRunningServices())
            .thenReturn(List.of(
                new ServiceExecution(FOO_SERVICE, RANKING_A),
                new ServiceExecution(FOO_SERVICE, RANKING_A),
                new ServiceExecution(BAR_SERVICE, RANKING_A)
        ));

        when(servicesProvider.getDurationsForService(FOO_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_FOO_SERVICE)));

        when(servicesProvider.getDurationsForService(BAR_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_BAR_SERVICE)));

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", inputForMultipleServices))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(FOO_SERVICE))
            .isFalse();    
    }

    @Test
    public void shouldInvokeDurationHeuristicWithEmptyListsWhenNoServiceIsRunning() throws Throwable {
        
        OffloadDurationInputDto inputForMultipleServices = new OffloadDurationInputDto(
            Collections.emptyList(),
            Collections.emptyList()
        );

        when(servicesProvider.getRunningServices())
            .thenReturn(Collections.emptyList());

        when(servicesProvider.getDurationsForService(FOO_SERVICE))
            .thenReturn(Collections.emptyList());

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", inputForMultipleServices))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(FOO_SERVICE))
            .isFalse();
    }

    @Test
    public void shouldInvokeDurationHeuristicWithSingleEmptyListWhenSingleServiceIsRunningWithoutDurations() throws Throwable {
        
        OffloadDurationInputDto inputForMultipleServices = new OffloadDurationInputDto(
            List.of(Collections.emptyList()),
            Collections.emptyList()
        );

        when(servicesProvider.getRunningServices())
            .thenReturn(List.of(new ServiceExecution(BAR_SERVICE, RANKING_A)));

        when(servicesProvider.getDurationsForService(BAR_SERVICE))
            .thenReturn(Collections.emptyList());

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", inputForMultipleServices))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(FOO_SERVICE))
            .isFalse();
    }

    @Test
    void shouldIgnoreTargetServiceOnDurationsForRunningServices() throws Throwable {
        OffloadDurationInputDto inputForMultipleServices = new OffloadDurationInputDto(
            List.of(
                List.of(DURATION_BAR_SERVICE)
            ),
            List.of(DURATION_FOO_SERVICE)
        );

        when(servicesProvider.getRunningServices())
            .thenReturn(List.of(
                new ServiceExecution(FOO_SERVICE, RANKING_A),
                new ServiceExecution(FOO_SERVICE, RANKING_A),
                new ServiceExecution(BAR_SERVICE, RANKING_A)
        ));

        when(servicesProvider.getDurationsForService(FOO_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_FOO_SERVICE)));

            when(servicesProvider.getDurationsForService(BAR_SERVICE))
            .thenReturn(List.of(Duration.ofMillis(DURATION_BAR_SERVICE)));

        when(serverlessFunctionClient.runOffloadingDuration("duration-offloading", inputForMultipleServices))
            .thenReturn(new OffloadDurationOutputDto("RUN_LOCALLY"));
        
        assertThat(offloadingHeuristicByDuration.shouldOffloadVitalSigns(FOO_SERVICE))
            .isFalse();    
    
    }

}

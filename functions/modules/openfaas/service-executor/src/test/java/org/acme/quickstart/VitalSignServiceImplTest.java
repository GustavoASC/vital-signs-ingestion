package org.acme.quickstart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.acme.quickstart.calculator.RankingCalculator;
import org.acme.quickstart.input.ServiceExecutorInputDto;
import org.acme.quickstart.offloading.duration.OffloadingHeuristicByDuration;
import org.acme.quickstart.offloading.ranking.OffloadingHeuristicByRanking;
import org.acme.quickstart.offloading.shared.CouldNotDetermineException;
import org.acme.quickstart.resources.ResourcesLocator;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.acme.quickstart.serverless.ServiceExecutorClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VitalSignServiceImplTest {

        private static final String VITAL_SIGN = "{ \"heartbeat\": 100}";
        private static final int USER_PRIORITY = 7;

        private static final int CRITICAL_CPU_USAGE = 90;
        private static final int WARNING_CPU_USAGE = 75;

        VitalSignServiceImpl vitalSignService;

        @Mock
        ServiceExecutorClient serviceExecutorClient;

        @Mock
        ServerlessFunctionClient serverlessFunctionClient;

        @Mock
        ResourcesLocator resourcesLocator;

        @Mock
        OffloadingHeuristicByRanking offloadingHeuristicByRanking;

        @Mock
        OffloadingHeuristicByDuration offloadingHeuristicByDuration;

        @Mock
        RankingCalculator rankingCalculator;

        @Mock
        RunningServicesProvider runningServicesProvider;

        @BeforeEach
        public void beforeEach() {
            this.vitalSignService = new VitalSignServiceImpl(CRITICAL_CPU_USAGE, WARNING_CPU_USAGE,
                    serverlessFunctionClient, serviceExecutorClient, resourcesLocator, offloadingHeuristicByRanking,
                    offloadingHeuristicByDuration, rankingCalculator, runningServicesProvider);
        }

        @AfterEach
        public void afterEach() {
                verifyNoMoreInteractions(
                                serverlessFunctionClient,
                                resourcesLocator,
                                offloadingHeuristicByRanking,
                                offloadingHeuristicByDuration,
                                rankingCalculator,
                                runningServicesProvider);
        }

        @Test
        public void shouldTriggerAllServicesLocallyWithLowCpuUsed() throws Throwable {

                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(74);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(resourcesLocator, times(2))
                                .getUsedCpuPercentage();
                verify(rankingCalculator, times(1))
                                .calculate(USER_PRIORITY, "body-temperature-monitor");
                verify(rankingCalculator, times(1))
                                .calculate(USER_PRIORITY, "bar-function");

                InOrder orderVerifier = inOrder(runningServicesProvider, serverlessFunctionClient);

                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionStarted("body-temperature-monitor", 13);
                orderVerifier.verify(serverlessFunctionClient, times(1))
                                .runFunction("body-temperature-monitor", VITAL_SIGN);
                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionFinished(any());

                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionStarted("bar-function", 17);
                orderVerifier.verify(serverlessFunctionClient, times(1))
                                .runFunction("bar-function", VITAL_SIGN);
                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionFinished(any());
        }

        @Test
        public void shouldOffloadVitalSignsWithHighCpuUsed() throws Throwable {

                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(14);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(91);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("body-temperature-monitor", VITAL_SIGN, USER_PRIORITY));
                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("bar-function", VITAL_SIGN, USER_PRIORITY));
                verify(resourcesLocator, times(2))
                                .getUsedCpuPercentage();
        }

        @Test
        public void shouldTriggerOffloadingHeuristicOnAlertScenario() throws Throwable {
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(80);
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(14);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(13))
                                .thenReturn(true);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(14))
                                .thenReturn(true);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("body-temperature-monitor", VITAL_SIGN, USER_PRIORITY));
                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("bar-function", VITAL_SIGN, USER_PRIORITY));
                verify(resourcesLocator, times(2))
                                .getUsedCpuPercentage();
                verify(offloadingHeuristicByRanking, times(1))
                                .shouldOffloadVitalSigns(13);
                verify(offloadingHeuristicByRanking, times(1))
                                .shouldOffloadVitalSigns(14);
        }

        @Test
        public void shouldNotOffloadWhenHeuristicCannotDetermineOperation() throws Throwable {
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(80);
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);

                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(13))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(17))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns(13, "body-temperature-monitor"))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns(17, "bar-function"))
                                .thenThrow(new CouldNotDetermineException());

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(runningServicesProvider, times(1))
                                .executionStarted("body-temperature-monitor", 13);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("body-temperature-monitor", VITAL_SIGN);

                verify(runningServicesProvider, times(1))
                                .executionStarted("bar-function", 17);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("bar-function", VITAL_SIGN);

                verify(runningServicesProvider, times(2))
                                .executionFinished(any());
        }

        @Test
        public void shouldNotOffloadAccordingToDurationHeuristic() throws Throwable {
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(80);
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);

                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(13))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(17))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns(13, "body-temperature-monitor"))
                                .thenReturn(false);
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns(17, "bar-function"))
                                .thenReturn(false);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(runningServicesProvider, times(1))
                                .executionStarted("body-temperature-monitor", 13);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("body-temperature-monitor", VITAL_SIGN);

                verify(runningServicesProvider, times(1))
                                .executionStarted("bar-function", 17);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("bar-function", VITAL_SIGN);

                verify(runningServicesProvider, times(2))
                                .executionFinished(any());
        }

}

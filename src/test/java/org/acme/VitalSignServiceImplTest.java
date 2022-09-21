package org.acme;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VitalSignServiceImplTest {

        private static final String VITAL_SIGN = "{ \"heartbeat\": 100}";
        private static final int USER_PRIORITY = 7;

        @InjectMocks
        VitalSignServiceImpl vitalSignService;

        @RestClient
        @Inject
        @Mock
        ServerlessFunctionClient serverlessFunctionClient;

        @RestClient
        @Inject
        @Mock
        VitalSignIngestionClient vitalSignIngestionClient;

        @Mock
        ResourcesLocator resourcesLocator;

        @Mock
        OffloadingHeuristicByRanking offloadingHeuristicByRanking;

        @Mock
        RankingCalculator rankingCalculator;

        @Mock
        RunningServicesProvider runningServicesProvider;

        @AfterEach
        public void afterEach() {
                verifyNoMoreInteractions(
                                serverlessFunctionClient,
                                vitalSignIngestionClient,
                                resourcesLocator,
                                offloadingHeuristicByRanking,
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
                                .thenReturn(90);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("body-temperature-monitor", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("bar-function", VITAL_SIGN,
                                                USER_PRIORITY));
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

                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("body-temperature-monitor", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("bar-function", VITAL_SIGN,
                                                USER_PRIORITY));
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
        public void shouldUpdateUsedCpuPercentage() {

                vitalSignService.updateUsedCpuPercentage(37);
                verify(resourcesLocator, times(1))
                                .updateUsedCpuPercentage(37);
        }

}

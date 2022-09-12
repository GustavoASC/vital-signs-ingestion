package org.acme;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

        @AfterEach
        public void afterEach() {
                verifyNoMoreInteractions(
                                serverlessFunctionClient,
                                vitalSignIngestionClient,
                                resourcesLocator,
                                offloadingHeuristicByRanking);
        }

        @Test
        public void shouldTriggerAllServicesLocallyWithLowCpuUsed() throws Throwable {

                when(resourcesLocator.usedCpuPercentage())
                                .thenReturn(74);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(serverlessFunctionClient, times(1))
                                .runAsyncHealthService("foo-function", VITAL_SIGN);
                verify(serverlessFunctionClient, times(1))
                                .runAsyncHealthService("bar-function", VITAL_SIGN);
                verify(resourcesLocator, times(2))
                                .usedCpuPercentage();
        }

        @Test
        public void shouldOffloadVitalSignsWithHighCpuUsed() throws Throwable {

                when(resourcesLocator.usedCpuPercentage())
                                .thenReturn(90);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("foo-function", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("bar-function", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(resourcesLocator, times(2))
                                .usedCpuPercentage();
        }

        @Test
        public void shouldTriggerOffloadingHeuristicOnAlertScenario() throws Throwable {
                when(resourcesLocator.usedCpuPercentage())
                                .thenReturn(80);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, "foo-function"))
                                .thenReturn(true);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, "bar-function"))
                                .thenReturn(true);

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("foo-function", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("bar-function", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(resourcesLocator, times(2))
                                .usedCpuPercentage();
                verify(offloadingHeuristicByRanking, times(2))
                                .shouldOffloadVitalSigns(anyInt(), any());
        }

        @Test
        public void shouldOffloadWhenHeuristicCannotDetermineOperation() throws Throwable {
                when(resourcesLocator.usedCpuPercentage())
                                .thenReturn(80);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, "foo-function"))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(USER_PRIORITY, "bar-function"))
                                .thenThrow(new CouldNotDetermineException());

                vitalSignService.ingestVitalSignRunningAllServices(VITAL_SIGN, USER_PRIORITY);

                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("foo-function", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(new VigalSignIngestionClientInputDto("bar-function", VITAL_SIGN,
                                                USER_PRIORITY));
                verify(resourcesLocator, times(2))
                                .usedCpuPercentage();
                verify(offloadingHeuristicByRanking, times(2))
                                .shouldOffloadVitalSigns(anyInt(), any());
        }

}

package org.acme;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VitalSignServiceImplTest {

        private static final String VITAL_SIGN = "{ \"heartbeat\": 100}";

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

        @Test
        public void shouldTriggerAllServicesLocallyWhenLowCpuUsed() {

                when(resourcesLocator.usedCpuPercentage())
                                .thenReturn(74);

                vitalSignService.ingestVitalSign(VITAL_SIGN);

                verify(vitalSignIngestionClient, never())
                                .ingestVitalSigns(any());
                verify(serverlessFunctionClient, times(2))
                                .runAsyncHealthService(any(), any());
                verify(serverlessFunctionClient, times(1))
                                .runAsyncHealthService("foo-function", VITAL_SIGN);
                verify(serverlessFunctionClient, times(1))
                                .runAsyncHealthService("bar-function", VITAL_SIGN);
                verify(resourcesLocator, times(1))
                                .usedCpuPercentage();
        }

        @Test
        public void shouldOffloadVitalSignsWithHighCpuUsed() {

                when(resourcesLocator.usedCpuPercentage())
                                .thenReturn(95);

                vitalSignService.ingestVitalSign(VITAL_SIGN);

                verify(vitalSignIngestionClient, times(1))
                                .ingestVitalSigns(VITAL_SIGN);
                verify(serverlessFunctionClient, never())
                                .runAsyncHealthService(any(), any());
                verify(resourcesLocator, times(1))
                                .usedCpuPercentage();
        }

}

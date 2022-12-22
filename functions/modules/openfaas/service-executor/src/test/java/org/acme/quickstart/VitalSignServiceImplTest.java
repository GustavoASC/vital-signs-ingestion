package org.acme.quickstart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.acme.quickstart.calculator.RankingCalculator;
import org.acme.quickstart.input.ServiceExecutorInputDto;
import org.acme.quickstart.metrics.Metrics;
import org.acme.quickstart.metrics.MetricsClient;
import org.acme.quickstart.offloading.duration.OffloadingHeuristicByDuration;
import org.acme.quickstart.offloading.ranking.OffloadingHeuristicByRanking;
import org.acme.quickstart.offloading.shared.CouldNotDetermineException;
import org.acme.quickstart.resources.ResourcesLocator;
import org.acme.quickstart.resources.ResourcesLocatorResponse;
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

        private static final UUID ID = UUID.fromString("18404589-0e1b-4f72-b6e2-b6765b949c10");
        private static final String VITAL_SIGN = "{ \"heartbeat\": 100}";
        private static final int USER_PRIORITY = 7;

        private static final int CRITICAL_MEM_USAGE = 95;
        private static final int CRITICAL_CPU_USAGE = 90;
        private static final int WARNING_CPU_USAGE = 75;

        VitalSignServiceImpl vitalSignService;

        Clock clock;
    
        @Mock
        ServiceExecutorClient serviceExecutorClient;

        @Mock
        ServerlessFunctionClient serverlessFunctionClient;

        @Mock
        MetricsClient metricsClient;

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
            this.clock = Clock.fixed(Instant.ofEpochMilli(1663527788128l), ZoneId.of("UTC"));
            this.vitalSignService = new VitalSignServiceImpl(CRITICAL_MEM_USAGE, CRITICAL_CPU_USAGE, WARNING_CPU_USAGE,
                    serverlessFunctionClient, metricsClient, serviceExecutorClient, resourcesLocator, offloadingHeuristicByRanking,
                    offloadingHeuristicByDuration, rankingCalculator, runningServicesProvider, clock);
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
        public void shouldGenerateMetricsWithouLastCpuObservation() {

                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("97.75"), null));

                ingestVitalSign();

                Metrics metrics;
                metrics = new Metrics();
                metrics.userPriority = USER_PRIORITY;
                metrics.ranking = 13;
                metrics.usedCpu = BigDecimal.valueOf(97.75);
                metrics.cpuCollectionTimestamp = 1663527788128l;
                metrics.function = "body-temperature-monitor";
                metrics.offloading = true;
                metrics.exceededCriticalCpuThreshold = true;

                verify(metricsClient, times(1))
                    .sendMetrics(metrics);
        }

        @Test
        public void shouldGenerateMetricsWithLastCpuObservation() {

                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("97.75"), new BigDecimal("87.3")));

                ingestVitalSign();

                Metrics metrics;
                metrics = new Metrics();
                metrics.userPriority = USER_PRIORITY;
                metrics.ranking = 13;
                metrics.usedCpu = BigDecimal.valueOf(97.75);
                metrics.lastCpuObservation = BigDecimal.valueOf(87.3);
                metrics.cpuCollectionTimestamp = 1663527788128l;
                metrics.function = "body-temperature-monitor";
                metrics.offloading = true;
                metrics.exceededCriticalCpuThreshold = true;

                verify(metricsClient, times(1))
                    .sendMetrics(metrics);
        }
        @Test
        public void shouldGenerateMetricsWithCpuAndMemory() {

                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("10.75"), new BigDecimal("15.3"), new BigDecimal("99.24"), new BigDecimal("14.27")));

                ingestVitalSign();

                Metrics metrics;
                metrics = new Metrics();
                metrics.userPriority = USER_PRIORITY;
                metrics.ranking = 13;
                metrics.usedCpu = BigDecimal.valueOf(10.75);
                metrics.usedMem = BigDecimal.valueOf(99.24);
                metrics.lastCpuObservation = BigDecimal.valueOf(15.3);
                metrics.lastMemObservation = BigDecimal.valueOf(14.27);
                metrics.cpuCollectionTimestamp = 1663527788128l;
                metrics.function = "body-temperature-monitor";
                metrics.offloading = true;
                metrics.exceededCriticalMemThreshold = true;

                verify(metricsClient, times(1))
                    .sendMetrics(metrics);
        }

        @Test
        void shouldOffloadConsideringDecimals() {
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(14);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("90.01"), null));

                ingestVitalSign();

                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("body-temperature-monitor", VITAL_SIGN, USER_PRIORITY, ID));
                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("bar-function", VITAL_SIGN, USER_PRIORITY, ID));
                verify(resourcesLocator, times(2))
                                .getUsedCpuPercentage();
        }

        @Test
        public void shouldTriggerAllServicesLocallyWithLowCpuUsed() throws Throwable {

                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("74"), null));

                ingestVitalSign();

                verify(resourcesLocator, times(2))
                                .getUsedCpuPercentage();
                verify(rankingCalculator, times(1))
                                .calculate(USER_PRIORITY, "body-temperature-monitor");
                verify(rankingCalculator, times(1))
                                .calculate(USER_PRIORITY, "bar-function");

                InOrder orderVerifier = inOrder(runningServicesProvider, serverlessFunctionClient);

                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionStarted(ID, "body-temperature-monitor", 13);
                orderVerifier.verify(serverlessFunctionClient, times(1))
                                .runFunction("body-temperature-monitor", VITAL_SIGN);
                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionFinished(any());

                orderVerifier.verify(runningServicesProvider, times(1))
                                .executionStarted(ID, "bar-function", 17);
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
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("91"), null));

                ingestVitalSign();

                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("body-temperature-monitor", VITAL_SIGN, USER_PRIORITY, ID));
                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("bar-function", VITAL_SIGN, USER_PRIORITY, ID));
                verify(resourcesLocator, times(2))
                                .getUsedCpuPercentage();
        }

        @Test
        public void shouldTriggerOffloadingHeuristicOnAlertScenario() throws Throwable {
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("80"), null));
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(14);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(13))
                                .thenReturn(true);
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(14))
                                .thenReturn(true);

                ingestVitalSign();

                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("body-temperature-monitor", VITAL_SIGN, USER_PRIORITY, ID));
                verify(serviceExecutorClient, times(1))
                                .runServiceExecutor(new ServiceExecutorInputDto("bar-function", VITAL_SIGN, USER_PRIORITY, ID));
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
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("80"), null));
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);

                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(13))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(17))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns("body-temperature-monitor"))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns("bar-function"))
                                .thenThrow(new CouldNotDetermineException());

                ingestVitalSign();

                verify(runningServicesProvider, times(1))
                                .executionStarted(ID, "body-temperature-monitor", 13);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("body-temperature-monitor", VITAL_SIGN);

                verify(runningServicesProvider, times(1))
                                .executionStarted(ID, "bar-function", 17);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("bar-function", VITAL_SIGN);

                verify(runningServicesProvider, times(2))
                                .executionFinished(any());
        }

        @Test
        public void shouldNotOffloadAccordingToDurationHeuristic() throws Throwable {
                when(resourcesLocator.getUsedCpuPercentage())
                                .thenReturn(new ResourcesLocatorResponse(new BigDecimal("80"), null));
                when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                                .thenReturn(13);
                when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                                .thenReturn(17);

                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(13))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByRanking.shouldOffloadVitalSigns(17))
                                .thenThrow(new CouldNotDetermineException());
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns("body-temperature-monitor"))
                                .thenReturn(false);
                when(offloadingHeuristicByDuration.shouldOffloadVitalSigns("bar-function"))
                                .thenReturn(false);

                ingestVitalSign();

                verify(runningServicesProvider, times(1))
                                .executionStarted(ID, "body-temperature-monitor", 13);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("body-temperature-monitor", VITAL_SIGN);

                verify(runningServicesProvider, times(1))
                                .executionStarted(ID, "bar-function", 17);
                verify(serverlessFunctionClient, times(1))
                                .runFunction("bar-function", VITAL_SIGN);

                verify(runningServicesProvider, times(2))
                                .executionFinished(any());
        }

        @Test
        void shouldOffloadWhenMemoryExceedsCriticalThreshold() {
            when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                        .thenReturn(13);
            when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                        .thenReturn(14);
            when(resourcesLocator.getUsedCpuPercentage())
                        .thenReturn(new ResourcesLocatorResponse(new BigDecimal("10.01"), null, new BigDecimal("95.01"), null));

            ingestVitalSign();

            verify(serviceExecutorClient, times(1))
                        .runServiceExecutor(new ServiceExecutorInputDto("body-temperature-monitor", VITAL_SIGN, USER_PRIORITY, ID));
            verify(serviceExecutorClient, times(1))
                        .runServiceExecutor(new ServiceExecutorInputDto("bar-function", VITAL_SIGN, USER_PRIORITY, ID));
            verify(resourcesLocator, times(2))
                        .getUsedCpuPercentage();
        }

        @Test
        void shouldNotOffloadWhenMemoryIsEqualToCriticalThreshold() {
            when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                            .thenReturn(13);
            when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                            .thenReturn(17);
            when(resourcesLocator.getUsedCpuPercentage())
                            .thenReturn(new ResourcesLocatorResponse(new BigDecimal("74"), null, new BigDecimal("95"), null));

            ingestVitalSign();

            verify(resourcesLocator, times(2))
                            .getUsedCpuPercentage();
            verify(rankingCalculator, times(1))
                            .calculate(USER_PRIORITY, "body-temperature-monitor");
            verify(rankingCalculator, times(1))
                            .calculate(USER_PRIORITY, "bar-function");

            InOrder orderVerifier = inOrder(runningServicesProvider, serverlessFunctionClient);

            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionStarted(ID, "body-temperature-monitor", 13);
            orderVerifier.verify(serverlessFunctionClient, times(1))
                            .runFunction("body-temperature-monitor", VITAL_SIGN);
            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionFinished(any());

            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionStarted(ID, "bar-function", 17);
            orderVerifier.verify(serverlessFunctionClient, times(1))
                            .runFunction("bar-function", VITAL_SIGN);
            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionFinished(any());
        }

        @Test
        void shouldNotOffloadWhenMemoryIsLowerThanCriticalThreshold() {
            when(rankingCalculator.calculate(USER_PRIORITY, "body-temperature-monitor"))
                            .thenReturn(13);
            when(rankingCalculator.calculate(USER_PRIORITY, "bar-function"))
                            .thenReturn(17);
            when(resourcesLocator.getUsedCpuPercentage())
                            .thenReturn(new ResourcesLocatorResponse(new BigDecimal("74"), null, new BigDecimal("10"), null));

            ingestVitalSign();

            verify(resourcesLocator, times(2))
                            .getUsedCpuPercentage();
            verify(rankingCalculator, times(1))
                            .calculate(USER_PRIORITY, "body-temperature-monitor");
            verify(rankingCalculator, times(1))
                            .calculate(USER_PRIORITY, "bar-function");

            InOrder orderVerifier = inOrder(runningServicesProvider, serverlessFunctionClient);

            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionStarted(ID, "body-temperature-monitor", 13);
            orderVerifier.verify(serverlessFunctionClient, times(1))
                            .runFunction("body-temperature-monitor", VITAL_SIGN);
            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionFinished(any());

            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionStarted(ID, "bar-function", 17);
            orderVerifier.verify(serverlessFunctionClient, times(1))
                            .runFunction("bar-function", VITAL_SIGN);
            orderVerifier.verify(runningServicesProvider, times(1))
                            .executionFinished(any());
        }        

        private void ingestVitalSign() {
            vitalSignService.ingestVitalSignRunningAllServices(ID, VITAL_SIGN, USER_PRIORITY);
        }

}

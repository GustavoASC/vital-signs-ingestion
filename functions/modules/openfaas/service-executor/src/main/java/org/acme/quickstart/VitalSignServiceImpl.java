package org.acme.quickstart;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class VitalSignServiceImpl implements VitalSignService {

    private static final List<String> FUNCTIONS = List.of("body-temperature-monitor", "bar-function");

    private final int criticalMemUsage;
    private final int criticalCpuUsage;
    private final int warningCpuUsage;
    private final ServerlessFunctionClient serverlessFunctionClient;
    private final MetricsClient metricsClient;
    private final ServiceExecutorClient serviceExecutorClient;
    private final ResourcesLocator resourcesLocator;
    private final OffloadingHeuristicByRanking offloadingHeuristicByRanking;
    private final OffloadingHeuristicByDuration offloadingHeuristicByDuration;
    private final RankingCalculator rankingCalculator;
    private final RunningServicesProvider runningServicesProvider;
    private final Clock clock;

    public VitalSignServiceImpl(
            @ConfigProperty(name = "offloading.threshold.critical-mem-usage") int criticalMemUsage,
            @ConfigProperty(name = "offloading.threshold.critical-cpu-usage") int criticalCpuUsage,
            @ConfigProperty(name = "offloading.threshold.warning-cpu-usage") int warningCpuUsage,
            @RestClient ServerlessFunctionClient serverlessFunctionClient,
            @RestClient MetricsClient metricsClient,
            ServiceExecutorClient serviceExecutorClient,
            ResourcesLocator resourcesLocator,
            OffloadingHeuristicByRanking offloadingHeuristicByRanking,
            OffloadingHeuristicByDuration offloadingHeuristicByDuration,
            RankingCalculator rankingCalculator,
            RunningServicesProvider runningServicesProvider,
            Clock clock) {
        this.criticalMemUsage = criticalMemUsage;
        this.criticalCpuUsage = criticalCpuUsage;
        this.warningCpuUsage = warningCpuUsage;
        this.serverlessFunctionClient = serverlessFunctionClient;
        this.metricsClient = metricsClient;
        this.serviceExecutorClient = serviceExecutorClient;
        this.resourcesLocator = resourcesLocator;
        this.offloadingHeuristicByRanking = offloadingHeuristicByRanking;
        this.offloadingHeuristicByDuration = offloadingHeuristicByDuration;
        this.rankingCalculator = rankingCalculator;
        this.runningServicesProvider = runningServicesProvider;
        this.clock = clock;
    }

    @Override
    public void ingestVitalSignRunningAllServices(UUID id, String vitalSign, int userPriority) {
        ingestVitalSign(id, FUNCTIONS, vitalSign, userPriority);
    }

    @Override
    public void ingestVitalSign(UUID id, List<String> services, String vitalSign, int userPriority) {
        services.stream()
                .forEach(fn -> {

                    Metrics metrics = new Metrics();
                    metrics.userPriority = userPriority;
                    metrics.function = fn;

                    metrics.ranking = rankingCalculator.calculate(userPriority, fn);
                    if (shouldOffloadToParent(metrics, metrics.ranking, fn)) {

                        // Vertical offloading to process vital signs on the parent node within the hierarchy
                        metrics.offloading = true;
                        ServiceExecutorInputDto input = new ServiceExecutorInputDto(fn, vitalSign, userPriority, id);
                        serviceExecutorClient.runServiceExecutor(input);

                    } else {

                        // Runs on the local machine
                        metrics.runningLocally = true;
                        runningServicesProvider.executionStarted(id, fn, metrics.ranking);
                        serverlessFunctionClient.runFunction(fn, vitalSign);
                        runningServicesProvider.executionFinished(id);
                    }

                    // Sends metrics for further analysis
                    metricsClient.sendMetrics(metrics);
                    
                });
    }

    private boolean shouldOffloadToParent(Metrics metrics, int ranking, String fn) {
        ResourcesLocatorResponse response = resourcesLocator.getUsedCpuPercentage();
        metrics.cpuCollectionTimestamp = clock.instant().toEpochMilli();
        metrics.usedCpu = response.getUsedCpu();
        metrics.lastCpuObservation = response.getLastCpuObservation();
        
        if (metrics.usedCpu.doubleValue() > criticalCpuUsage) {
            metrics.exceededCriticalThreshold = true;
            return true;
        }

        var memUsage = response.getUsedMemory();
        if (memUsage != null && memUsage.doubleValue() > criticalMemUsage) {
            return true;
        }

        if (metrics.usedCpu.doubleValue() > warningCpuUsage) {
            try {
                metrics.triggeredHeuristicByRanking = true;
                metrics.resultForHeuristicByRanking = offloadingHeuristicByRanking.shouldOffloadVitalSigns(ranking);
                return metrics.resultForHeuristicByRanking;
            } catch (CouldNotDetermineException e) {
                try {
                    metrics.triggeredHeuristicByDuration = true;
                    metrics.resultForHeuristicByDuration = offloadingHeuristicByDuration.shouldOffloadVitalSigns(ranking, fn);
                    return metrics.resultForHeuristicByDuration;
                } catch (CouldNotDetermineException e1) {
                    // Run locally because it is a match and we could not detect which request is
                    // more important. Therefore, we assume that running locally is the best approach
                    // because it does not incur the overhead of performing an offloading operation.
                    metrics.assumingFallbackForHeuristics = true;
                    return false;
                }
            }
        }

        return false;
    }

}

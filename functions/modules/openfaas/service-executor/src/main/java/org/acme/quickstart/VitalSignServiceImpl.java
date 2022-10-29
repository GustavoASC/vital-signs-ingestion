package org.acme.quickstart;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.acme.quickstart.calculator.RankingCalculator;
import org.acme.quickstart.input.ServiceExecutorInputDto;
import org.acme.quickstart.offloading.duration.OffloadingHeuristicByDuration;
import org.acme.quickstart.offloading.ranking.OffloadingHeuristicByRanking;
import org.acme.quickstart.offloading.shared.CouldNotDetermineException;
import org.acme.quickstart.resources.ResourcesLocator;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.acme.quickstart.serverless.ServiceExecutorClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VitalSignServiceImpl implements VitalSignService {
    
    private final Logger logger = LoggerFactory.getLogger(VitalSignServiceImpl.class);
    
    private static final List<String> FUNCTIONS = List.of("body-temperature-monitor", "bar-function");

    private final int criticalCpuUsage;
    private final int warningCpuUsage;
    private final ServerlessFunctionClient serverlessFunctionClient;
    private final ServiceExecutorClient serviceExecutorClient;
    private final ResourcesLocator resourcesLocator;
    private final OffloadingHeuristicByRanking offloadingHeuristicByRanking;
    private final OffloadingHeuristicByDuration offloadingHeuristicByDuration;
    private final RankingCalculator rankingCalculator;
    private final RunningServicesProvider runningServicesProvider;

    public VitalSignServiceImpl(
            @ConfigProperty(name = "offloading.threshold.critical-cpu-usage") int criticalCpuUsage,
            @ConfigProperty(name = "offloading.threshold.warning-cpu-usage") int warningCpuUsage,
            @RestClient ServerlessFunctionClient serverlessFunctionClient,
            ServiceExecutorClient serviceExecutorClient,
            ResourcesLocator resourcesLocator,
            OffloadingHeuristicByRanking offloadingHeuristicByRanking,
            OffloadingHeuristicByDuration offloadingHeuristicByDuration,
            RankingCalculator rankingCalculator,
            RunningServicesProvider runningServicesProvider) {
        this.criticalCpuUsage = criticalCpuUsage;
        this.warningCpuUsage = warningCpuUsage;
        this.serverlessFunctionClient = serverlessFunctionClient;
        this.serviceExecutorClient = serviceExecutorClient;
        this.resourcesLocator = resourcesLocator;
        this.offloadingHeuristicByRanking = offloadingHeuristicByRanking;
        this.offloadingHeuristicByDuration = offloadingHeuristicByDuration;
        this.rankingCalculator = rankingCalculator;
        this.runningServicesProvider = runningServicesProvider;
    }

    @Override
    public void ingestVitalSignRunningAllServices(String vitalSign, int userPriority) {
        ingestVitalSign(FUNCTIONS, vitalSign, userPriority);
    }

    @Override
    public void ingestVitalSign(List<String> services, String vitalSign, int userPriority) {
        services.stream()
                .forEach(fn -> {

                    int ranking = rankingCalculator.calculate(userPriority, fn);
                    if (shouldOffloadToParent(ranking, fn)) {

                        // Vertical offloading to process vital signs on the parent node within the hierarchy
                        logger.info("Making vertical offloading...");
                        ServiceExecutorInputDto input = new ServiceExecutorInputDto(fn, vitalSign, userPriority);
                        serviceExecutorClient.runServiceExecutor(input);
                        
                    } else {

                        // Runs on the local machine
                        logger.info("Running health service locally...");
                        UUID id = runningServicesProvider.executionStarted(fn, ranking);
                        serverlessFunctionClient.runFunction(fn, vitalSign);
                        runningServicesProvider.executionFinished(id);
                    }
                });
    }

    private boolean shouldOffloadToParent(int ranking, String fn) {
        int usedCpu = resourcesLocator.getUsedCpuPercentage();
        if (usedCpu >= criticalCpuUsage) {
            return true;
        }

        if (usedCpu >= warningCpuUsage) {
            try {
                logger.info("Triggering offloading heuristic by ranking...");
                return offloadingHeuristicByRanking.shouldOffloadVitalSigns(ranking);
            } catch (CouldNotDetermineException e) {
                try {
                    logger.info("Triggering offloading heuristic by duration...");
                    return offloadingHeuristicByDuration.shouldOffloadVitalSigns(ranking, fn);
                } catch (CouldNotDetermineException e1) {
                    // Run locally because it is a match and we could not detect which request is
                    // more important. Therefore, we assume that running locally is the best approach
                    // because it does not incur the overhead of performing an offloading operation.
                    logger.info("Assuming fallback to execute health service locally...");
                    return false;
                }
            }
        }

        return false;
    }

}

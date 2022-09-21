package org.acme;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class VitalSignServiceImpl implements VitalSignService, ResourceService {
    private static final List<String> FUNCTIONS = List.of("body-temperature-monitor", "bar-function");
    private static final int CRITICAL_CPU_USAGE = 90;
    private static final int WARNING_CPU_USAGE = 75;

    private final ServerlessFunctionClient serverlessFunctionClient;
    private final VitalSignIngestionClient vitalSignIngestionClient;
    private final ResourcesLocator resourcesLocator;
    private final OffloadingHeuristicByRanking offloadingHeuristicByRanking;
    private final RankingCalculator rankingCalculator;
    private final RunningServicesProvider runningServicesProvider;

    public VitalSignServiceImpl(
            @RestClient ServerlessFunctionClient serverlessFunctionClient,
            @RestClient VitalSignIngestionClient vitalSignIngestionClient,
            ResourcesLocator resourcesLocator,
            OffloadingHeuristicByRanking offloadingHeuristicByRanking,
            RankingCalculator rankingCalculator,
            RunningServicesProvider runningServicesProvider) {
        this.serverlessFunctionClient = serverlessFunctionClient;
        this.vitalSignIngestionClient = vitalSignIngestionClient;
        this.resourcesLocator = resourcesLocator;
        this.offloadingHeuristicByRanking = offloadingHeuristicByRanking;
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

                    if (shouldOffloadToParent(fn, userPriority)) {

                        // Executes on a remote machine
                        var input = new VigalSignIngestionClientInputDto(fn, vitalSign, userPriority);
                        vitalSignIngestionClient.ingestVitalSigns(input);
                        
                    } else {

                        int ranking = rankingCalculator.calculate(userPriority, fn);
                        UUID id = runningServicesProvider.executionStarted(fn, ranking);

                        // Runs on the local machine
                        serverlessFunctionClient.runFunction(fn, vitalSign);

                        runningServicesProvider.executionFinished(id);
                    }
                });
    }

    private boolean shouldOffloadToParent(String service, int userPriority) {
        int usedCpu = resourcesLocator.getUsedCpuPercentage();
        if (usedCpu >= CRITICAL_CPU_USAGE) {
            return true;
        }

        if (usedCpu >= WARNING_CPU_USAGE) {
            try {
                return offloadingHeuristicByRanking.shouldOffloadVitalSigns(userPriority, service);
            } catch (CouldNotDetermineException e) {

                // Run locally because it is a match and we could not detect which one is more
                // important. Therefore, we assume that running locally is the best approach
                // because it does not incur the overhead of performing an offloading operation.
                return false;
            }
        }

        return false;
    }

    @Override
    public void updateUsedCpuPercentage(int usedCpu) {
        resourcesLocator.updateUsedCpuPercentage(usedCpu);
    }

}

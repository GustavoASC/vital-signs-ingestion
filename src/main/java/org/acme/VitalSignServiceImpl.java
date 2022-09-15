package org.acme;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class VitalSignServiceImpl implements VitalSignService {
    private static final List<String> FUNCTIONS = List.of("foo-function", "bar-function");
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
                        runningServicesProvider.addRunningService(fn, ranking);

                        // Runs on the local machine
                        serverlessFunctionClient.runFunction(fn, vitalSign);

                        runningServicesProvider.removeRunningService(fn, ranking);
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
                return true;
            }
        }

        return false;
    }

}

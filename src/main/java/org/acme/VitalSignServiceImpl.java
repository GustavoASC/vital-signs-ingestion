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

    public VitalSignServiceImpl(
            @RestClient ServerlessFunctionClient serverlessFunctionClient,
            @RestClient VitalSignIngestionClient vitalSignIngestionClient,
            ResourcesLocator resourcesLocator,
            OffloadingHeuristicByRanking offloadingHeuristicByRanking) {
        this.serverlessFunctionClient = serverlessFunctionClient;
        this.vitalSignIngestionClient = vitalSignIngestionClient;
        this.resourcesLocator = resourcesLocator;
        this.offloadingHeuristicByRanking = offloadingHeuristicByRanking;
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

                        // Runs on the local machine
                        serverlessFunctionClient.runAsyncHealthService(fn, vitalSign);
                    }
                });
    }

    private boolean shouldOffloadToParent(String service, int userPriority) {
        int usedCpu = resourcesLocator.usedCpuPercentage();
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

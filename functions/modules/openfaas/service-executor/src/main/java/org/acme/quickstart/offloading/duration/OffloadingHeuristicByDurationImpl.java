package org.acme.quickstart.offloading.duration;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.acme.quickstart.RunningServicesProvider;
import org.acme.quickstart.RunningServicesProvider.ServiceExecution;
import org.acme.quickstart.offloading.shared.CouldNotDetermineException;
import org.acme.quickstart.offloading.shared.OffloadingDecision;
import org.acme.quickstart.serverless.ServerlessFunctionClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OffloadingHeuristicByDurationImpl implements OffloadingHeuristicByDuration {

    private static final String FUNCTION_NAME = "duration-offloading";

    private final RunningServicesProvider servicesProvider;
    private final ServerlessFunctionClient serverlessFunctionClient;

    public OffloadingHeuristicByDurationImpl(
            RunningServicesProvider servicesProvider,
            @RestClient ServerlessFunctionClient serverlessFunctionClient) {
        this.servicesProvider = servicesProvider;
        this.serverlessFunctionClient = serverlessFunctionClient;
    }

    @Override
    public boolean shouldOffloadVitalSigns(String service) throws CouldNotDetermineException {

        List<List<Long>> previousDurations = servicesProvider.getRunningServices()
                .stream()
                .filter(execution -> !execution.serviceName().equals(service))
                .map(ServiceExecution::serviceName)
                .distinct()
                .map(this::getPreviousDurations)
                .collect(Collectors.toList());

        OffloadDurationOutputDto output = serverlessFunctionClient.runOffloadingDuration(
                FUNCTION_NAME,
                new OffloadDurationInputDto(
                        previousDurations,
                        getPreviousDurations(service)
        ));

        if (output.getOffloadingDecision() == OffloadingDecision.UNKNOWN) {
            throw new CouldNotDetermineException();
        }
        return output.getOffloadingDecision() == OffloadingDecision.OFFLOAD;
    }

    private List<Long> getPreviousDurations(String service) {
        return servicesProvider.getDurationsForService(service)
                .stream()
                .map(Duration::toMillis)
                .collect(Collectors.toList());
    }

}

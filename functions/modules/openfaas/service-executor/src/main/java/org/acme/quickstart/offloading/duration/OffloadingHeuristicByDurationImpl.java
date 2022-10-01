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
    public boolean shouldOffloadVitalSigns(int filterRanking, String service) throws CouldNotDetermineException {

        List<PreviousDurationInputDto> previousDurations = servicesProvider.getRunningServices()
                .stream()
                .filter(execution -> execution.ranking() == filterRanking)
                .map(ServiceExecution::serviceName)
                .map(this::getPreviousDurationForService)
                .collect(Collectors.toList());

        OffloadDurationOutputDto output = serverlessFunctionClient.runOffloadingDuration(
                FUNCTION_NAME,
                new OffloadDurationInputDto(
                        previousDurations,
                        service));

        if (output.getOffloadingDecision() == OffloadingDecision.UNKNOWN) {
            throw new CouldNotDetermineException();
        }
        return output.getOffloadingDecision() == OffloadingDecision.OFFLOAD;
    }

    private PreviousDurationInputDto getPreviousDurationForService(String service) {
        return new PreviousDurationInputDto(service, getDurations(service));
    }

    private List<Long> getDurations(String service) {
        return servicesProvider.getDurationsForService(service)
                .stream()
                .map(Duration::toMillis)
                .collect(Collectors.toList());
    }

}
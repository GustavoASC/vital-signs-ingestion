package org.acme;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.acme.RunningServicesProvider.ServiceExecution;

@ApplicationScoped
public class OffloadingHeuristicByDurationImpl implements OffloadingHeuristicByDuration {

    private final RunningServicesProvider servicesProvider;
    private final DurationPredictor durationPredictor;

    public OffloadingHeuristicByDurationImpl(
            RunningServicesProvider servicesProvider,
            DurationPredictor durationPredictor) {
        this.servicesProvider = servicesProvider;
        this.durationPredictor = durationPredictor;
    }

    @Override
    public boolean shouldOffloadVitalSigns(int filterRanking, String service) throws CouldNotDetermineException {
        List<Long> executionDurations = servicesProvider.getRunningServices()
                .stream()
                .filter(execution -> execution.ranking() == filterRanking)
                .filter(execution -> !execution.serviceName().equals(service))
                .map(ServiceExecution::serviceName)
                .distinct()
                .map(this::predictDurationIgnoringException)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .distinct()
                .toList();

        if (!executionDurations.isEmpty()) {

            try {

                long predictedDuration = predictDuration(service);
                long intermediateDuration = intermediateDuration(executionDurations);

                if (predictedDuration == intermediateDuration) {
                    throw new CouldNotDetermineException();
                }

                return predictedDuration > intermediateDuration;

            } catch (CouldNotPredictDurationException e) {
                throw new CouldNotDetermineException();
            }

        }
        return false;
    }

    private long predictDuration(String service) throws CouldNotPredictDurationException {
        return durationPredictor.predictDurationInMillis(service);
    }

    private Optional<Long> predictDurationIgnoringException(String service) {
        try {
            return Optional.of(durationPredictor.predictDurationInMillis(service));
        } catch (CouldNotPredictDurationException e) {
            return Optional.empty();
        }
    }

    private long intermediateDuration(List<Long> rankingsForService) {
        int intermediateIndex = rankingsForService.size() / 2;
        return rankingsForService.get(intermediateIndex);
    }

}

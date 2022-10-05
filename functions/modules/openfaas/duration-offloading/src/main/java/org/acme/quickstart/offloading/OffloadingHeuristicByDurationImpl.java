package org.acme.quickstart.offloading;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.acme.quickstart.prediction.CouldNotPredictDurationException;
import org.acme.quickstart.prediction.DurationPredictor;

@ApplicationScoped
public class OffloadingHeuristicByDurationImpl implements OffloadingHeuristicByDuration {

    private final DurationPredictor durationPredictor;

    public OffloadingHeuristicByDurationImpl(DurationPredictor durationPredictor) {
        this.durationPredictor = durationPredictor;
    }

    @Override
    public boolean shouldOffloadVitalSigns(
        List<List<Long>> previousDurationsForOtherServices,
        List<Long> previousDurationsForTargetService
    ) throws CouldNotDetermineException {

        List<Long> executionDurations = previousDurationsForOtherServices
                .stream()
                .map(this::predictDurationIgnoringException)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        if (!executionDurations.isEmpty()) {

            try {

                long predictedDuration = predictDuration(previousDurationsForTargetService);
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

    private long predictDuration(List<Long> durationsForService) throws CouldNotPredictDurationException {
        return durationPredictor.predictDurationInMillis(durationsForService);
    }

    private Optional<Long> predictDurationIgnoringException(List<Long> durationsForService) {
        try {
            return Optional.of(predictDuration(durationsForService));
        } catch (CouldNotPredictDurationException e) {
            return Optional.empty();
        }
    }

    private long intermediateDuration(List<Long> rankingsForService) {
        int intermediateIndex = rankingsForService.size() / 2;
        return rankingsForService.get(intermediateIndex);
    }

}

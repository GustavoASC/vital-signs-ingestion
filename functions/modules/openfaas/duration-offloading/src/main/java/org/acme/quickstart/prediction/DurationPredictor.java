package org.acme.quickstart.prediction;

import java.util.List;

public interface DurationPredictor {

    long predictDurationInMillis(List<Long> durationsForService)
            throws CouldNotPredictDurationException;

}

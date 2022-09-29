package org.acme.quickstart;

import java.util.List;

public interface DurationPredictor {

    long predictDurationInMillis(List<Long> durationsForService, String service)
            throws CouldNotPredictDurationException;

}

package org.acme;

public interface DurationPredictor {

    long predictDurationInMillis(String service) throws CouldNotPredictDurationException;

}

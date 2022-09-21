package org.acme;

public interface OffloadingHeuristicByDuration {

    boolean shouldOffloadVitalSigns(int filterRanking, String service) throws CouldNotDetermineException;
}

package org.acme;

public interface OffloadingHeuristicByRanking {

    boolean shouldOffloadVitalSigns(int calculatedRanking) throws CouldNotDetermineException;
}

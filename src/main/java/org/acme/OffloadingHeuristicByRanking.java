package org.acme;

public interface OffloadingHeuristicByRanking {

    boolean shouldOffloadVitalSigns(int userPriority, String service) throws CouldNotDetermineException;
}

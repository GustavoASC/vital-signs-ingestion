package org.acme.quickstart;

import java.util.List;

public interface OffloadingHeuristicByRanking {

    boolean shouldOffloadVitalSigns(List<Integer> rankingsForAllServices, int calculatedRanking) throws CouldNotDetermineException;
}

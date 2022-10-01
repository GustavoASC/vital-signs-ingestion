package org.acme.quickstart.offloading.ranking;

import org.acme.quickstart.offloading.shared.CouldNotDetermineException;

public interface OffloadingHeuristicByRanking {

    boolean shouldOffloadVitalSigns(int calculatedRanking) throws CouldNotDetermineException;
}

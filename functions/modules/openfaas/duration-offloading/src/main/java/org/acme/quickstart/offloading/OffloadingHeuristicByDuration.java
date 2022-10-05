package org.acme.quickstart.offloading;

import java.util.List;

public interface OffloadingHeuristicByDuration {

    boolean shouldOffloadVitalSigns(
        List<List<Long>> previousDurationsForOtherServices,
        List<Long> previousDurationsForTargetService) throws CouldNotDetermineException;
}

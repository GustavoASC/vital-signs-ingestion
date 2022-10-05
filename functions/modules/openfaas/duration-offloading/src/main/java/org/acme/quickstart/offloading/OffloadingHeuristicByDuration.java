package org.acme.quickstart.offloading;

import java.util.List;

import org.acme.quickstart.input.PreviousServiceDuration;

public interface OffloadingHeuristicByDuration {

    boolean shouldOffloadVitalSigns(
        List<PreviousServiceDuration> previousDurationsForOtherServices,
        PreviousServiceDuration previousDurationsForTargetService) throws CouldNotDetermineException;
}

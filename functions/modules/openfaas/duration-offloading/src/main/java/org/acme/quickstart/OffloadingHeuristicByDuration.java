package org.acme.quickstart;

import java.util.List;

import org.acme.quickstart.input.PreviousServiceDuration;

public interface OffloadingHeuristicByDuration {

    boolean shouldOffloadVitalSigns(List<PreviousServiceDuration> previousDurations, String targetService)
            throws CouldNotDetermineException;
}

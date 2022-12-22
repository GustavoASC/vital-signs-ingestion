package org.acme.quickstart.offloading.duration;

import org.acme.quickstart.offloading.shared.CouldNotDetermineException;

public interface OffloadingHeuristicByDuration {

    boolean shouldOffloadVitalSigns(String service) throws CouldNotDetermineException;
}

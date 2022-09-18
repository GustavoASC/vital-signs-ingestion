package org.acme;

import java.util.List;

public interface DurationStore {

    List<Long> getDurations(String service);
    void storeDuration(String service, long duration);
    
}

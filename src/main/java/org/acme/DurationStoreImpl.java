package org.acme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DurationStoreImpl implements DurationStore {

    private final Map<String, List<Long>> data = new HashMap<>();

    @Override
    public synchronized List<Long> getDurations(String service) {
        var result = data.get(service);
        if (result == null) {
            result = new ArrayList<>();
            data.put(service, result);
        }
        return result;
    }

    @Override
    public synchronized void storeDuration(String service, long duration) {
        var durations = getDurations(service);
        durations.add(duration);
    }

}

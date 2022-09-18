package org.acme;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface RunningServicesProvider {

    UUID executionStarted(String service, int ranking);
    void executionFinished(UUID id);
    List<Integer> getRankingsForRunningServices();
    List<Duration> getDurationsForService(String service);

}

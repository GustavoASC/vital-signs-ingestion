package org.acme;

import java.util.List;
import java.util.UUID;

public interface RunningServicesProvider {

    UUID executionStarted(String service, int ranking);
    void executionFinished(UUID id);
    List<Integer> getRankingsForRunningSerices();
    List<Long> getDurationsForService(String service);

}

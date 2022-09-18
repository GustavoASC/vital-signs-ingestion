package org.acme;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface RunningServicesProvider {

    UUID executionStarted(String service, int ranking);
    void executionFinished(UUID id);
    List<ServiceExecution> getRunningServices();
    List<Duration> getDurationsForService(String service);

    default List<Integer> getRankingsForRunningServices() {
        return getRunningServices()
                .stream()
                .map(ServiceExecution::ranking)
                .toList();
    }

    public static record ServiceExecution(String serviceName, int ranking) {}

}

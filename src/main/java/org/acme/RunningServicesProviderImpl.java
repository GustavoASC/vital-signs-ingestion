package org.acme;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
public class RunningServicesProviderImpl implements RunningServicesProvider {

    private final Map<UUID, ServiceExecution> services = new LinkedHashMap<>();

    @Override
    public void executionStarted(String service, int ranking) {
        UUID id = UUID.randomUUID();
        services.put(id, new ServiceExecution(service, ranking));
    }

    @Override
    public void executionFinished(String service, int ranking) {
        services.entrySet()
                .removeIf(entry -> {
                    var execution = entry.getValue();
                    return execution.serviceName.equals(service) && execution.ranking == ranking;
                });
    }

    @Override
    public List<Integer> getRankingsForRunningSerices() {
        return services.values()
                .stream()
                .map(ServiceExecution::ranking)
                .toList();
    }

    private record ServiceExecution(String serviceName, int ranking) {
    }

}

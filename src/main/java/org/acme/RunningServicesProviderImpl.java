package org.acme;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
public class RunningServicesProviderImpl implements RunningServicesProvider {

    private final Map<UUID, ServiceExecution> services = new LinkedHashMap<>();
    private final Map<String, List<Long>> durations = new LinkedHashMap<>();
    private final Clock clock;

    public RunningServicesProviderImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public UUID executionStarted(String service, int ranking) {
        UUID id = UUID.randomUUID();
        long executionStart = clock.millis();
        services.put(id, new ServiceExecution(service, ranking, executionStart));
        return id;
    }

    @Override
    public void executionFinished(UUID id) {

        Optional.ofNullable(services.get(id))
                .ifPresent(execution -> {

                    getDurationsForService(execution.serviceName())
                            .add(clock.millis() - execution.executionStartInMillis());

                });

        services.remove(id);

    }

    @Override
    public List<Integer> getRankingsForRunningSerices() {
        return services.values()
                .stream()
                .map(ServiceExecution::ranking)
                .toList();
    }

    @Override
    public List<Long> getDurationsForService(String service) {
        List<Long> durationsForService = durations.get(service);
        if (durationsForService == null) {
            durationsForService = new ArrayList<>();
            durations.put(service, durationsForService);
        }
        return durationsForService;
    }

    private record ServiceExecution(String serviceName, int ranking, long executionStartInMillis) {
    }

}

package org.acme.quickstart;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
public class RunningServicesProviderImpl implements RunningServicesProvider {

    private final Map<UUID, ExecutionWithDuration> services = new LinkedHashMap<>();
    private final Map<String, List<Duration>> durations = new LinkedHashMap<>();
    private final Clock clock;

    public RunningServicesProviderImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public UUID executionStarted(String service, int ranking) {
        var id = UUID.randomUUID();
        var executionStart = clock.instant();
        services.put(id, new ExecutionWithDuration(new ServiceExecution(service, ranking), executionStart));
        return id;
    }

    @Override
    public void executionFinished(UUID id) {

        Optional.ofNullable(services.get(id))
                .ifPresent(service -> {

                    var now = clock.instant();
                    getDurationsForService(service.execution().serviceName())
                            .add(Duration.between(service.executionStart(), now));

                });

        services.remove(id);

    }

    @Override
    public List<ServiceExecution> getRunningServices() {
        return services.values()
                       .stream()
                       .map(ExecutionWithDuration::execution)
                       .collect(Collectors.toList());
    }

    @Override
    public List<Duration> getDurationsForService(String service) {
        List<Duration> durationsForService = durations.get(service);
        if (durationsForService == null) {
            durationsForService = new ArrayList<>();
            durations.put(service, durationsForService);
        }
        return durationsForService;
    }

    private class ExecutionWithDuration {

        private final ServiceExecution execution;
        private final Instant executionStart;

        public ExecutionWithDuration(ServiceExecution execution, Instant executionStart) {
            this.execution = execution;
            this.executionStart = executionStart;
        }

        public ServiceExecution execution() {
            return this.execution;
        }

        public Instant executionStart() {
            return this.executionStart;
        }
    }

}

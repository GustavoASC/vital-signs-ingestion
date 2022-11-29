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

import org.acme.quickstart.results.Results;
import org.acme.quickstart.results.ResultsClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
public class RunningServicesProviderImpl implements RunningServicesProvider {

    private final Map<UUID, ExecutionWithDuration> services = new LinkedHashMap<>();
    private final Map<String, List<Duration>> durations = new LinkedHashMap<>();
    private final Clock clock;
    private final ResultsClient resultsClient;

    public RunningServicesProviderImpl(Clock clock, @RestClient ResultsClient resultsClient) {
        this.clock = clock;
        this.resultsClient = resultsClient;
    }

    @Override
    public void executionStarted(UUID id, String service, int ranking) {
        var executionStart = clock.instant();
        services.put(id, new ExecutionWithDuration(new ServiceExecution(service, ranking), executionStart));
    }

    @Override
    public void executionFinished(UUID id) {

        Optional.ofNullable(services.get(id))
                .ifPresent(service -> {

                    var now = clock.instant();
                    getDurationsWithoutCloning(service.execution().serviceName())
                            .add(Duration.between(service.executionStart(), now));

                    Results results = new Results();
                    results.initialServiceTimestamp = service.executionStart.toEpochMilli();
                    results.endTimestamp = now.toEpochMilli();
                    resultsClient.sendResults(id.toString(), results);

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
        return new ArrayList<>(getDurationsWithoutCloning(service));
    }

    private List<Duration> getDurationsWithoutCloning(String service) {
        List<Duration> durationsForService = durations.get(service);
        if (durationsForService == null) {
            durationsForService = new ArrayList<>();
            durations.put(service, durationsForService);
        }
        return durationsForService;
    }
    
    @Override
    public void clearDataForTests() {
        services.clear();
        durations.clear();
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

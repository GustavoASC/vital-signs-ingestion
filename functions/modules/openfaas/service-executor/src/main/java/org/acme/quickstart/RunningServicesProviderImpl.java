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
import javax.ws.rs.WebApplicationException;

import org.acme.quickstart.results.Results;
import org.acme.quickstart.results.ResultsClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
public class RunningServicesProviderImpl implements RunningServicesProvider {

    private static final int MAX_HISTORICAL_DURATIONS = 6;
    private static final int MAX_RETRIES = 5;
    
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
                .ifPresent(sendResults -> {

                    var now = clock.instant();
                    var durationsForService = getDurationsWithoutCloning(sendResults.execution().serviceName());
                    
                    durationsForService
                            .add(Duration.between(sendResults.executionStart(), now));

                    // Ensures that only the most recent values will be available
                    // This is to avoid huge memory usage and also because prediction
                    // only cares about the most recent values
                    if (durationsForService.size() > MAX_HISTORICAL_DURATIONS) {
                        durationsForService.remove(0);
                    }

                    sendResults(id, sendResults, now);

                });

        services.remove(id);

    }

    private void sendResults(UUID id, ExecutionWithDuration service, Instant now) {
        
        boolean keepTrying = true;
        int retries = 0;

        while (keepTrying) {
            try {
                Results results = new Results();
                results.initialServiceTimestamp = service.executionStart.toEpochMilli();
                results.endTimestamp = now.toEpochMilli();
                resultsClient.sendResults(id.toString(), results);
                keepTrying = false;
            } catch (WebApplicationException e) {
                e.printStackTrace();
                if (retries++ > MAX_RETRIES) {
                    System.out.println("Exceeded maximum limit of " + MAX_RETRIES + " retries...");
                    throw e;
                } else {
                    System.out.println("Retrying...");
                }
                
            }
        }
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

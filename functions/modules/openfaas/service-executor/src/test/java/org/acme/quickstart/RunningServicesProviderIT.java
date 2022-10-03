package org.acme.quickstart;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.acme.quickstart.RunningServicesProvider.ServiceExecution;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RunningServicesProviderIT {

    @Inject
    RunningServicesProviderImpl runningServicesProviderImpl;

    @Test
    public void shouldStoreAndRetrieveRankingsInMultiThreadScenarios() {

        IntStream.range(1, 100000)
                .boxed()
                .parallel()
                .forEach(i -> {
                    var id = runningServicesProviderImpl.executionStarted("body-temperature-monitor", 7);

                    runningServicesProviderImpl.getDurationsForService("body-temperature-monitor")
                            .stream()
                            .map(Duration::toMillis)
                            .collect(Collectors.toList());

                    runningServicesProviderImpl.getRunningServices()
                            .stream()
                            .map(ServiceExecution::ranking)
                            .collect(Collectors.toList());

                    runningServicesProviderImpl.getRankingsForRunningServices()
                            .stream()
                            .map(value -> value++)
                            .collect(Collectors.toList());

                    runningServicesProviderImpl.executionFinished(id);
                });

        assertThat(runningServicesProviderImpl.getRankingsForRunningServices())
                .isEmpty();
    }

}
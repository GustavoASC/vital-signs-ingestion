package org.acme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import javax.inject.Inject;

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
                                        runningServicesProviderImpl.addRunningService("body-temperature-monitor", 7);
                                        runningServicesProviderImpl.removeRunningService("body-temperature-monitor", 7);
                                });

                assertThat(runningServicesProviderImpl.provideAllRankings())
                                .isEmpty();
        }

}

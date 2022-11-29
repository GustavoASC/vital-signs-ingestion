package org.acme.quickstart;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.acme.quickstart.RunningServicesProvider.ServiceExecution;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RunningServicesProviderIT {

    private static final int STUBBED_MACHINE_RESOURCES_PORT = 8678;

    @Inject
    RunningServicesProviderImpl runningServicesProviderImpl;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(
            wireMockConfig()
                .port(STUBBED_MACHINE_RESOURCES_PORT)
                .notifier(new Slf4jNotifier(true)))
        .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_MACHINE_RESOURCES_PORT);
    }

    @Test
    @Disabled
    public void shouldStoreAndRetrieveRankingsInMultiThreadScenarios() {

        stubFor(patch(urlMatching("/results/.*")));

        IntStream.range(1, 100000)
                .boxed()
                .parallel()
                .forEach(i -> {
                    var id = UUID.randomUUID();
                    runningServicesProviderImpl.executionStarted(id, "body-temperature-monitor", 7);

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
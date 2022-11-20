package org.acme.quickstart.resources;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MachineResourcesClientIT {

    private static final int STUBBED_MACHINE_RESOURCES_PORT = 8597;

    @Inject
    @RestClient
    MachineResourcesClient machineResourcesClient;

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
    public void shouldReturnMachineResourcesWithoutLastObservationField() throws Throwable {
        stubFor(
            get("/machine-resources")
                    .willReturn(okJson(jsonFromResource("output-machine-resources-with-cpu.json"))));

        assertThat(machineResourcesClient.getMachineResources())
            .isEqualTo(new MachineResourcesOutputDto(new BigDecimal("27.8"), null));
    }

    @Test
    public void shouldReturnMachineResourcesWithLastObservationField() throws Throwable {
        stubFor(
            get("/machine-resources")
                    .willReturn(okJson(jsonFromResource("output-machine-resources-with-cpu-and-last-observation.json"))));

        assertThat(machineResourcesClient.getMachineResources())
            .isEqualTo(new MachineResourcesOutputDto(new BigDecimal("18.090625000000003"), new BigDecimal("19.3")));
    }

    private String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/machine-resources", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}

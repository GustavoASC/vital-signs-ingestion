package org.acme.quickstart.restclient;

import javax.inject.Inject;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MappingProviderClientIT {

    private static final int STUBBED_MAPPING_PROVIDER = 9234;

    @Inject
    @RestClient
    MappingProviderClient mappingProviderClient;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(
                    wireMockConfig()
                            .port(STUBBED_MAPPING_PROVIDER)
                            .notifier(new Slf4jNotifier(true)))
            .build();

    @BeforeAll
    static void beforeAll() {
        configureFor(STUBBED_MAPPING_PROVIDER);
    }

    @Test
    public void shouldReturnMappingPropertiesContent() throws Throwable {

        stubFor(
                get("/nodes-mappings.properties")
                        .withHost(equalTo("localhost"))
                        .willReturn(okForContentType("text/plain", textFromResource("nodes-mappings.properties"))));

        assertThat(mappingProviderClient.getMappingProperties())
                .isEqualTo(textFromResource("nodes-mappings.properties"));
    }

    private String textFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/mapping-provider", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

}

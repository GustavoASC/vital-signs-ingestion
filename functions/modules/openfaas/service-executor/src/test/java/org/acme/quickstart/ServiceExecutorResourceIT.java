package org.acme.quickstart;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class ServiceExecutorResourceIT {

    private static final int SERVERLESS_PLATFORM_PORT = 8586;
    private static final int MACHINE_RESOURCES_PORT = 8597;

    private static final long DELAY_NONE = 0;
    private static final long DELAY_TWO_SECONDS = 2;

    MockServer wmServerless;
    MockServer wmMachineResources;

    @Inject
    RunningServicesProvider runningServicesProvider;

    @BeforeEach
    public void setUp() {
        
        wmServerless = new MockServer(WireMockConfiguration.wireMockConfig()
            .port(SERVERLESS_PLATFORM_PORT)
            .notifier(new Slf4jNotifier(true))
        );

        wmMachineResources = new MockServer(WireMockConfiguration.wireMockConfig()
            .port(MACHINE_RESOURCES_PORT)
            .notifier(new Slf4jNotifier(true))
        );

        wmServerless.start();
        wmMachineResources.start();
    }
    
    @AfterEach
    void tearDown() {
        wmServerless.stop();
        wmMachineResources.stop();
    }

    @Test
    public void testSingleVitalSignIngestionOnLocalMachineWithoutSpecificService() throws Throwable {


        String[] functions = {"body-temperature-monitor", "bar-function"};
        stubHealthServerlessFunctions(DELAY_NONE, functions);

        configureFor(wmMachineResources.getClient());
        stubUsedCpuPercentage(0);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-user-priority.json"))
        .when()
            .post("/")
        .then()
            .statusCode(202)
            .body(is(""));

        verifyFunctionsWereInvokedOnlyOnce(functions);
    }

    @Test
    public void testSingleVitalSignIngestionOnLocalMachineDependingOnHeuristicWithoutSpecificService() throws Throwable {
        
        String[] functions = {"body-temperature-monitor", "bar-function"};

        configureFor(wmServerless.getClient());
        stubHealthServerlessFunctions(DELAY_NONE, functions);
        stubRankingOffloadingFunctionToRunLocally();

        configureFor(wmMachineResources.getClient());
        stubUsedCpuPercentage(85);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-user-priority.json"))
        .when()
            .post("/")
        .then()
            .statusCode(202)
            .body(is(""));

        verifyFunctionsWereInvokedOnlyOnce(functions);
    }

    @Test
    public void testSingleVitalSignIngestionOnLocalMachineWithSpecificService() throws Throwable {
        
        String[] functions = {"body-temperature-monitor"};

        configureFor(wmServerless.getClient());
        stubHealthServerlessFunctions(DELAY_NONE, functions);

        configureFor(wmMachineResources.getClient());
        stubUsedCpuPercentage(0);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-foo-service-and-user-priority.json"))
        .when()
            .post("/")
        .then()
            .statusCode(202)
            .body(is(""));

        verifyFunctionsWereInvokedOnlyOnce(functions);
    }

    @Test
    public void shouldRunLocallyAfterBothRankingAndDurationHeuristics() throws Throwable{
        
        String[] functions = {"different-health-service"};

        configureFor(wmServerless.getClient());
        stubHealthServerlessFunctions(DELAY_NONE, functions);
        stubRankingOffloadingFunctionToUnknown();
        stubDurationOffloadingFunctionToRunLocally();

        configureFor(wmMachineResources.getClient());
        stubUsedCpuPercentage(85);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-different-health-service-and-user-priority.json"))
        .when()
            .post("/")
        .then()
            .statusCode(202)
            .body(is(""));

        verifyFunctionsWereInvokedOnlyOnce(functions);
    }

    @Test
    public void testSingleVitalSignIngestionOnLocalMachineWithDurationVerification() throws Throwable {
        
        String[] functions = {"specific-health-service"};

        configureFor(wmServerless.getClient());
        stubHealthServerlessFunctions(DELAY_TWO_SECONDS, functions);

        configureFor(wmMachineResources.getClient());
        stubUsedCpuPercentage(0);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-specific-health-service-and-user-priority.json"))
        .when()
            .post("/")
        .then()
            .statusCode(202)
            .body(is(""));

        verifyFunctionsWereInvokedOnlyOnce(functions);
        assertThat(getDurationsForServiceInSeconds("specific-health-service"))
            .isEqualTo(List.of(DELAY_TWO_SECONDS));
    }

    private List<Long> getDurationsForServiceInSeconds(String service) {
        return runningServicesProvider.getDurationsForService(service)
                .stream()
                .map(Duration::toSeconds)
                .collect(Collectors.toList());
    }

    @Test
    public void testSingleVitalSignIngestionOnRemoteMachineWithoutSpecificService() throws Throwable {
        
        configureFor(wmServerless.getClient());
        stubServiceExecutor();

        configureFor(wmMachineResources.getClient());
        stubUsedCpuPercentage(99);
        
        given()
            .contentType(ContentType.JSON)
            .body(jsonFromResource("vital-sign-with-user-priority.json"))
        .when()
            .post("/")
        .then()
            .statusCode(202)
            .body(is(""));

        configureFor(wmServerless.getClient());
        verify(1,
            postRequestedFor(urlEqualTo("/function/service-executor"))
                .withRequestBody(equalToJson(jsonFromResource("vital-sign-with-foo-service-and-user-priority.json")))
        );
        verify(1,
            postRequestedFor(urlEqualTo("/function/service-executor"))
                .withRequestBody(equalToJson(jsonFromResource("vital-sign-with-bar-service-and-user-priority.json")))
        );
        verify(2, postRequestedFor(urlEqualTo("/function/service-executor")));
    }

    private void stubHealthServerlessFunctions(long delay, String... functions) {
        configureFor(wmServerless.getClient());
        for (var function : functions) {
            stubFor(
                post("/function/" + function)
                    .withRequestBody(equalToJson("{\"heartbeat\": 100}"))
                    .willReturn(ResponseDefinitionBuilder.responseDefinition().withFixedDelay((int) delay * 1000))
            );
        }
    }

    private void stubRankingOffloadingFunctionToRunLocally() throws IOException {
        configureFor(wmServerless.getClient());
        stubFor(
            post("/function/ranking-offloading")
                .withRequestBody(equalToJson(jsonFromResource("input-ranking-heuristic-without-any-ranking-and-calculated-7.json")))
                .willReturn(okJson(jsonFromResource("output-run-locally.json")))
        );
        stubFor(
            post("/function/ranking-offloading")
                .withRequestBody(equalToJson(jsonFromResource("input-ranking-heuristic-without-any-ranking-and-calculated-9.json")))
                .willReturn(okJson(jsonFromResource("output-run-locally.json")))
        );
    }

    private void stubRankingOffloadingFunctionToUnknown() throws IOException {
        configureFor(wmServerless.getClient());
        stubFor(
            post("/function/ranking-offloading")
                .withRequestBody(equalToJson(jsonFromResource("input-ranking-heuristic-without-any-ranking-and-calculated-7.json")))
                .willReturn(okJson(jsonFromResource("output-unknown.json")))
        );
        stubFor(
            post("/function/ranking-offloading")
                .withRequestBody(equalToJson(jsonFromResource("input-ranking-heuristic-without-any-ranking-and-calculated-9.json")))
                .willReturn(okJson(jsonFromResource("output-unknown.json")))
        );
    }

    private void stubDurationOffloadingFunctionToRunLocally() throws IOException {
        configureFor(wmServerless.getClient());
        stubFor(
            post("/function/duration-offloading")
                .withRequestBody(equalToJson(jsonFromResource("input-duration-heuristic-without-any-duration.json")))
                .willReturn(okJson(jsonFromResource("output-run-locally.json")))
        );
    }

    private void stubServiceExecutor() throws IOException {
        configureFor(wmServerless.getClient());
        stubFor(
            post("/function/service-executor")
            .withRequestBody(equalToJson(jsonFromResource("input-vertical-offloading-body-temperature-monitor.json")))
        );
        stubFor(
            post("/function/service-executor")
            .withRequestBody(equalToJson(jsonFromResource("input-vertical-offloading-bar-function.json")))
        );
    }

    private void stubUsedCpuPercentage(int cpuPercentage) {
        configureFor(wmMachineResources.getClient());
        stubFor(
            get("/machine-resources")
            .willReturn(okJson("{\"cpu\": "  + cpuPercentage + "}"))
        );            
    }

    private void verifyFunctionsWereInvokedOnlyOnce(String[] functions) {
        configureFor(wmServerless.getClient());
        for (var function : functions) {
            verify(1, postRequestedFor(urlEqualTo("/function/" + function)));
        }
    }

    public String jsonFromResource(String resourcePath) throws IOException {
        String fullResourcePath = Path.of("/service-executor", resourcePath).toString();
        InputStream resourceStream = Objects.requireNonNull(this.getClass().getResourceAsStream(fullResourcePath));
        return new String(resourceStream.readAllBytes(), Charset.defaultCharset());
    }

    public static class MockServer extends WireMockServer {
        public MockServer(Options options) {
            super(options);
        }
    
        public WireMock getClient() {
            return client;
        }
    }

}
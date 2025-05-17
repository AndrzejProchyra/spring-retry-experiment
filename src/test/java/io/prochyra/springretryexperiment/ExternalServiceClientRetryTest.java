package io.prochyra.springretryexperiment;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.retry.annotation.EnableRetry;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "external-service-client.base-url"))
public class ExternalServiceClientRetryTest {

  @Autowired private ExternalServiceClient externalServiceClient;

  @Test
  void should_retry_twice() {
    stubFor(
        get(urlEqualTo("/"))
            .inScenario("retry-scenario")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("failure-1"));

    stubFor(
        get(urlEqualTo("/"))
            .inScenario("retry-scenario")
            .whenScenarioStateIs("failure-1")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("success"));

    stubFor(
        get(urlEqualTo("/"))
            .inScenario("retry-scenario")
            .whenScenarioStateIs("success")
            .willReturn(aResponse().withStatus(200)));

    externalServiceClient.get();

    verify(exactly(3), getRequestedFor(urlEqualTo("/")));
  }

  @TestConfiguration
  @EnableRetry
  public static class RetryTestConfiguration {}
}

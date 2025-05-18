package io.prochyra.springretryexperiment;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(
    classes = {
      ExternalServiceClientRetryTest.RetryTestConfiguration.class,
      ExternalServiceClient.class
    })
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

  @Test
  void should_throw_ExternalServiceClientException_when_retries_run_out() {
    stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(500)));

    var throwable = catchThrowable(() -> externalServiceClient.get());

    assertAll(
        () -> verify(exactly(3), getRequestedFor(urlEqualTo("/"))),
        () -> then(throwable).isInstanceOf(ExternalServiceClientException.class));
  }

  @Configuration
  @EnableRetry
  @ConfigurationPropertiesScan
  public static class RetryTestConfiguration {
    @Bean
    public RestClient.Builder restClientBuilder() {
      return RestClient.builder();
    }
  }
}

package io.prochyra.springretryexperiment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExternalServiceClient {

  private final RestClient restClient;

  public ExternalServiceClient(
      RestClient.Builder restClientBuilder,
      @Value("${external-service-client.base-url}") String baseUrl) {
    this.restClient = restClientBuilder.baseUrl(baseUrl).build();
  }

  @Retryable(
      maxAttemptsExpression =
          "#{@'external-service-client-io.prochyra.springretryexperiment.ExternalServiceClientProperties'.maxAttempts}",
      backoff =
          @Backoff(
              delayExpression =
                  "#{@'external-service-client-io.prochyra.springretryexperiment.ExternalServiceClientProperties'.backoff.delay}",
              multiplierExpression =
                  "#{@'external-service-client-io.prochyra.springretryexperiment.ExternalServiceClientProperties'.backoff.multiplier}"))
  public void get() {
    restClient
        .get()
        .uri("/")
        .retrieve()
        .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::handle5xx)
        .toBodilessEntity();
  }

  private static void handle5xx(HttpRequest request, ClientHttpResponse response) {
    throw new ExternalServiceClientException("Got a 5xx response");
  }
}

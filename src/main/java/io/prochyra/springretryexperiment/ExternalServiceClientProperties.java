package io.prochyra.springretryexperiment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties("external-service-client")
public record ExternalServiceClientProperties(
    @DefaultValue("3") int maxAttempts, @DefaultValue Backoff backoff) {
  public record Backoff(@DefaultValue("1000") long delay, @DefaultValue("0.0") double multiplier) {}
}

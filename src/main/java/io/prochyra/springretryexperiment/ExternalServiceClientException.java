package io.prochyra.springretryexperiment;

public class ExternalServiceClientException extends RuntimeException {
  public ExternalServiceClientException(String message) {
    super(message);
  }
}

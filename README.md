# An Experiment in Configuring Spring Retry

1. We create an `@ConfigurationProperties` record thus
    ```java
    @ConfigurationProperties("external-service-client")
    public record ExternalServiceClientProperties(String baseUrl, @DefaultValue Retry retry) {
      public record Retry(@DefaultValue("3") int maxAttempts, @DefaultValue Backoff backoff) {}
    
      public record Backoff(@DefaultValue("1000") long delay, @DefaultValue("0.0") double multiplier) {}
    }
    ```
    - We put all the client properties here, including `base-url`
    - We give it the prefix `external-service-client`. The Spring Boot convention is to use snake case for property
      names. See the manual appendix: https://docs.spring.io/spring-boot/appendix/application-properties/index.html.
    - We use `@DefaultValue` to specify that a reference type is not null by default and to supply default values for
      primitives.
    - In this way we never have an invalid configuration and supplying overrides in `properties.yml` has IntelliJ
      autocompletion and hints.
2. In order for Spring Boot to pick up such configuration property records we **either** add
   `@ConfigurationPropertiesScan` to our context configuration **or** we add `@EnableConfigurationProperties` and
   register them as beans in one of the usual ways. If we rely on `@ConfigurationPropertiesScan` the resultant beans are
   given names like `<prefix>-<fully-qualified-name-of-configuration-property-record>`. I have not been able to work out
   how to get beans configured in another way to work.
3. We can reference these properties in SpEL expressions thus
    ```SpEL
    "#{@'external-service-client-io.prochyra.springretryexperiment.ExternalServiceClientProperties'.retry.backoff.multiplier}"
    ```
    - Note that beans are referenced with the `@` prefix and that bean names containing hyphens must be escaped with
      single quotes.

## ❓Open Questions

### Using WireMock with `@Nested` classes

If I annotate the main test class with `@EnableWireMock`, WireMock starts another instance when running the tests
in the nested class. The `external-service-client.base-url` in the context still refers to the **original instance
**, but `stubFor` works on the **new** instance!
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  metrics:
    tags:
      application: ${artifactId}
    distribution:
      percentiles-histogram:
        http.server.requests: true
      slo:
        http.server.requests: 50ms, 100ms, 200ms, 500ms, 1s
  tracing:
    sampling:
      probability: ${'$'}{TRACING_SAMPLE_RATE:0.1}
    propagation:
      type: w3c
  otlp:
    tracing:
      endpoint: ${'$'}{OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/traces}
    metrics:
      endpoint: ${'$'}{OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/metrics}

logging:
  pattern:
    correlation: "[%X{traceId}/%X{spanId}] "

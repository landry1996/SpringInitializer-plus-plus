spring:
  kafka:
    bootstrap-servers: ${'$'}{KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: ${artifactId}-group
      auto-offset-reset: earliest
      enable-auto-commit: false
    producer:
      acks: all
      retries: 3
    listener:
      ack-mode: manual_immediate

spring:
  datasource:
    url: jdbc:mysql://${r"${MYSQL_HOST:localhost}"}:${r"${MYSQL_PORT:3306}"}/${r"${MYSQL_DB:${artifactId}}"}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${r"${MYSQL_USER:root}"}
    password: ${r"${MYSQL_PASSWORD:root}"}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

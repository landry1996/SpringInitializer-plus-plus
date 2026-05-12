<#if securityType == "OAUTH2" || securityType == "OIDC">
spring:
  security:
    oauth2:
      client:
        registration:
          ${provider!"keycloak"}:
            client-id: ${'$'}{OAUTH2_CLIENT_ID:${artifactId}}
            client-secret: ${'$'}{OAUTH2_CLIENT_SECRET:change-me}
            scope: openid,profile,email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          ${provider!"keycloak"}:
            issuer-uri: ${'$'}{OAUTH2_ISSUER_URI:http://localhost:8080/realms/${artifactId}}
      resourceserver:
        jwt:
          issuer-uri: ${'$'}{OAUTH2_ISSUER_URI:http://localhost:8080/realms/${artifactId}}
<#else>
app:
  security:
    jwt:
      secret: ${'$'}{JWT_SECRET:change-this-in-production-must-be-at-least-64-characters-long-for-hs512}
      access-token-expiration: 900000
      refresh-token-expiration: 604800000
</#if>

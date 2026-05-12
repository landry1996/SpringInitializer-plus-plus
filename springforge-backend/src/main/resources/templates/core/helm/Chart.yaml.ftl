apiVersion: v2
name: ${artifactId}
description: ${projectDescription!"A Spring Boot application"}
type: application
version: 0.1.0
appVersion: "1.0.0"
keywords:
  - spring-boot
  - java
<#if architecture??>
  - ${architecture?lower_case}
</#if>
maintainers:
  - name: ${groupId}

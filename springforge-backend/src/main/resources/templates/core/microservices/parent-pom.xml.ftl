<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>${springBootVersion}</version>
        <relativePath/>
    </parent>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>${projectName}</name>
    <description>${projectDescription}</description>

    <properties>
        <java.version>${javaVersion}</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
    </properties>

    <modules>
        <module>service-registry</module>
        <module>api-gateway</module>
<#if configServer?? && configServer>
        <module>config-server</module>
</#if>
<#list services as service>
        <module>${service.name}</module>
</#list>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${r"${spring-cloud.version}"}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>

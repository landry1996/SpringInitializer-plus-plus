replicaCount: 2

image:
  repository: ${groupId?replace(".", "/")}/${artifactId}
  tag: "latest"
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: ${artifactId}.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: ${artifactId}-tls
      hosts:
        - ${artifactId}.example.com

resources:
  requests:
    cpu: 250m
    memory: 512Mi
  limits:
    cpu: "1"
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

env:
  SPRING_PROFILES_ACTIVE: prod
  JAVA_OPTS: "-XX:+UseZGC -XX:MaxRAMPercentage=75"

secrets:
  DATABASE_URL: ""
  DATABASE_USERNAME: ""
  DATABASE_PASSWORD: ""
<#if messaging?? && messaging == "KAFKA">
  KAFKA_BOOTSTRAP_SERVERS: ""
</#if>

probes:
  liveness:
    path: /actuator/health/liveness
    initialDelaySeconds: 30
    periodSeconds: 10
  readiness:
    path: /actuator/health/readiness
    initialDelaySeconds: 15
    periodSeconds: 5

postgresql:
  enabled: true
  auth:
    database: ${artifactId}
    username: ${artifactId}
    existingSecret: ${artifactId}-db-secret

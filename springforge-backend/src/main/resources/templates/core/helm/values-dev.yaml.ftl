replicaCount: 1

image:
  tag: "dev-latest"

resources:
  requests:
    cpu: 100m
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi

autoscaling:
  enabled: false

env:
  SPRING_PROFILES_ACTIVE: dev
  JAVA_OPTS: "-XX:+UseZGC"

ingress:
  enabled: true
  hosts:
    - host: ${artifactId}-dev.example.com
      paths:
        - path: /
          pathType: Prefix
  tls: []

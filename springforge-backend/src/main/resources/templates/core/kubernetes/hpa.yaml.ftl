apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ${artifactId}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ${artifactId}
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80

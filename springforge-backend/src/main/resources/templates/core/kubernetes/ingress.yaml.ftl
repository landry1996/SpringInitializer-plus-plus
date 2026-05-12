apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${artifactId}
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - ${artifactId}.example.com
      secretName: ${artifactId}-tls
  rules:
    - host: ${artifactId}.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: ${artifactId}
                port:
                  number: 80

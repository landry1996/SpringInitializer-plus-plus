spring:
  data:
    mongodb:
      uri: mongodb://${artifactId}:${artifactId}@localhost:27017/${artifactId}?authSource=admin
      auto-index-creation: true

mongock:
  migration-scan-package: ${packageName}.migration
  enabled: true

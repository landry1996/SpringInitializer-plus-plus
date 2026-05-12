repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.6.0
    hooks:
      - id: trailing-whitespace
      - id: end-of-file-fixer
      - id: check-yaml
      - id: check-json
      - id: check-merge-conflict
      - id: check-added-large-files
        args: ['--maxkb=1024']

  - repo: https://github.com/gitleaks/gitleaks
    rev: v8.18.4
    hooks:
      - id: gitleaks

  - repo: local
    hooks:
      - id: checkstyle
        name: Checkstyle
        language: system
<#if buildTool?? && buildTool?contains("GRADLE")>
        entry: ./gradlew checkstyleMain --daemon
<#else>
        entry: ./mvnw checkstyle:check -q
</#if>
        types: [java]
        pass_filenames: false

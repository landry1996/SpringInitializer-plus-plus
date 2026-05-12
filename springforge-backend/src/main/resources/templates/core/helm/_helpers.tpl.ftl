{{- define "${artifactId}.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "${artifactId}.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "${artifactId}.labels" -}}
helm.sh/chart: {{ include "${artifactId}.name" . }}-{{ .Chart.Version }}
{{ include "${artifactId}.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}

{{- define "${artifactId}.selectorLabels" -}}
app.kubernetes.io/name: {{ include "${artifactId}.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

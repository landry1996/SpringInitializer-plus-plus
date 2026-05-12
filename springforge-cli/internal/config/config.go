package config

import (
	"encoding/json"
	"fmt"
	"os"
)

type Metadata struct {
	GroupID            string `json:"groupId"`
	ArtifactID         string `json:"artifactId"`
	Name               string `json:"name"`
	Description        string `json:"description"`
	PackageName        string `json:"packageName"`
	JavaVersion        string `json:"javaVersion"`
	SpringBootVersion  string `json:"springBootVersion"`
	BuildTool          string `json:"buildTool"`
}

type Architecture struct {
	Type    string   `json:"type"`
	Modules []string `json:"modules,omitempty"`
}

type Dependency struct {
	GroupID    string `json:"groupId"`
	ArtifactID string `json:"artifactId"`
	Version    string `json:"version,omitempty"`
	Scope      string `json:"scope,omitempty"`
}

type MessagingConfig struct {
	Type   string   `json:"type"`
	Topics []string `json:"topics,omitempty"`
}

type ObservabilityConfig struct {
	Enabled           bool `json:"enabled"`
	Metrics           bool `json:"metrics"`
	Tracing           bool `json:"tracing"`
	StructuredLogging bool `json:"structuredLogging"`
}

type TestingConfig struct {
	Enabled         bool `json:"enabled"`
	Testcontainers  bool `json:"testcontainers"`
	Archunit        bool `json:"archunit"`
	ContractTesting bool `json:"contractTesting"`
}

type MultiTenantConfig struct {
	Enabled  bool   `json:"enabled"`
	Strategy string `json:"strategy,omitempty"`
}

type SecurityConfig struct {
	Enabled  bool   `json:"enabled"`
	Type     string `json:"type,omitempty"`
	Provider string `json:"provider,omitempty"`
}

type CacheConfig struct {
	Enabled  bool   `json:"enabled"`
	Provider string `json:"provider,omitempty"`
}

type InfrastructureConfig struct {
	Docker bool `json:"docker"`
	Helm   bool `json:"helm"`
	CI     bool `json:"ci"`
}

type ProjectConfiguration struct {
	Metadata       Metadata             `json:"metadata"`
	Architecture   Architecture         `json:"architecture"`
	Dependencies   []Dependency         `json:"dependencies,omitempty"`
	Messaging      *MessagingConfig     `json:"messaging,omitempty"`
	Observability  *ObservabilityConfig `json:"observability,omitempty"`
	Testing        *TestingConfig       `json:"testing,omitempty"`
	MultiTenant    *MultiTenantConfig   `json:"multiTenant,omitempty"`
	Security       *SecurityConfig      `json:"security,omitempty"`
	Cache          *CacheConfig         `json:"cache,omitempty"`
	Infrastructure *InfrastructureConfig `json:"infrastructure,omitempty"`
}

func LoadFromFile(path string) (*ProjectConfiguration, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("cannot read file %s: %w", path, err)
	}

	var cfg ProjectConfiguration
	if err := json.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("invalid JSON: %w", err)
	}

	return &cfg, nil
}

func (c *ProjectConfiguration) SaveToFile(path string) error {
	data, err := json.MarshalIndent(c, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal config: %w", err)
	}

	if err := os.WriteFile(path, data, 0644); err != nil {
		return fmt.Errorf("failed to write file: %w", err)
	}

	return nil
}

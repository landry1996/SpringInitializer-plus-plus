package cmd

import (
	"fmt"
	"os"

	"github.com/fatih/color"
	"github.com/landry1996/springforge-cli/internal/api"
	"github.com/landry1996/springforge-cli/internal/config"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	validateConfig string
	strict         bool
)

var validateCmd = &cobra.Command{
	Use:   "validate",
	Short: "Validate a configuration file without generating",
	Long:  `Checks a SpringForge configuration JSON file for errors and incompatibilities.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		if validateConfig == "" {
			return fmt.Errorf("--config flag is required")
		}

		if _, err := os.Stat(validateConfig); os.IsNotExist(err) {
			return fmt.Errorf("config file not found: %s", validateConfig)
		}

		cfg, err := config.LoadFromFile(validateConfig)
		if err != nil {
			return fmt.Errorf("failed to parse config: %w", err)
		}

		serverURL := viper.GetString("server")
		client := api.NewClient(serverURL, viper.GetString("token"))

		result, err := client.Validate(cfg)
		if err != nil {
			return fmt.Errorf("validation request failed: %w", err)
		}

		if result.Valid {
			color.Green("✓ Configuration is valid")
			fmt.Printf("  Project: %s:%s\n", cfg.Metadata.GroupID, cfg.Metadata.ArtifactID)
			fmt.Printf("  Java %s / Spring Boot %s / %s\n", cfg.Metadata.JavaVersion, cfg.Metadata.SpringBootVersion, cfg.Metadata.BuildTool)
			return nil
		}

		color.Red("✗ Configuration has errors:")
		for _, e := range result.Errors {
			fmt.Printf("  • %s\n", e)
		}

		if strict {
			os.Exit(1)
		}
		return fmt.Errorf("%d validation error(s)", len(result.Errors))
	},
}

func init() {
	rootCmd.AddCommand(validateCmd)

	validateCmd.Flags().StringVar(&validateConfig, "config", "", "path to configuration file (required)")
	validateCmd.Flags().BoolVar(&strict, "strict", false, "exit with code 1 on any error")
	validateCmd.MarkFlagRequired("config")
}

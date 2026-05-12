package cmd

import (
	"fmt"
	"os"

	"github.com/fatih/color"
	"github.com/landry1996/springforge-cli/internal/wizard"
	"github.com/spf13/cobra"
)

var (
	preset   string
	fromFile string
	output   string
)

var initCmd = &cobra.Command{
	Use:   "init",
	Short: "Interactive wizard to create a new Spring Boot project",
	Long:  `Launches an interactive wizard that guides you through project configuration step by step.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		color.Cyan("\n🚀 SpringForge — Project Initialization Wizard\n")
		fmt.Println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

		config, err := wizard.RunWizard()
		if err != nil {
			return fmt.Errorf("wizard cancelled: %w", err)
		}

		outputDir := output
		if outputDir == "" {
			outputDir = "."
		}

		color.Green("\n✓ Configuration complete!")
		fmt.Printf("  Project: %s:%s\n", config.Metadata.GroupID, config.Metadata.ArtifactID)
		fmt.Printf("  Architecture: %s\n", config.Architecture.Type)
		fmt.Printf("  Java: %s / Spring Boot: %s\n", config.Metadata.JavaVersion, config.Metadata.SpringBootVersion)

		configPath := outputDir + "/springforge-config.json"
		if err := config.SaveToFile(configPath); err != nil {
			return fmt.Errorf("failed to save config: %w", err)
		}

		color.Yellow("\n→ Config saved to: %s", configPath)
		fmt.Println("  Run 'springforge generate --config", configPath, "' to generate the project")

		return nil
	},
}

func init() {
	rootCmd.AddCommand(initCmd)

	initCmd.Flags().StringVar(&preset, "preset", "", "start from a preset configuration")
	initCmd.Flags().StringVar(&fromFile, "from-file", "", "start from an existing config file")
	initCmd.Flags().StringVarP(&output, "output", "o", "", "output directory (default: current)")

	if fromFile != "" {
		if _, err := os.Stat(fromFile); os.IsNotExist(err) {
			fmt.Fprintf(os.Stderr, "Error: file %s does not exist\n", fromFile)
			os.Exit(1)
		}
	}
}

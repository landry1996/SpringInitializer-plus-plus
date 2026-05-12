package cmd

import (
	"fmt"
	"os"
	"time"

	"github.com/fatih/color"
	"github.com/landry1996/springforge-cli/internal/api"
	"github.com/landry1996/springforge-cli/internal/config"
	"github.com/schollz/progressbar/v3"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	configFile string
	dryRun     bool
	outputDir  string
)

var generateCmd = &cobra.Command{
	Use:   "generate",
	Short: "Generate a Spring Boot project from a configuration file",
	Long:  `Reads a SpringForge configuration JSON file and generates the complete project.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		if configFile == "" {
			return fmt.Errorf("--config flag is required. Use 'springforge init' to create one")
		}

		if _, err := os.Stat(configFile); os.IsNotExist(err) {
			return fmt.Errorf("config file not found: %s", configFile)
		}

		cfg, err := config.LoadFromFile(configFile)
		if err != nil {
			return fmt.Errorf("failed to parse config: %w", err)
		}

		if dryRun {
			color.Yellow("DRY RUN — no project will be generated")
			fmt.Printf("\nProject: %s:%s\n", cfg.Metadata.GroupID, cfg.Metadata.ArtifactID)
			fmt.Printf("Architecture: %s\n", cfg.Architecture.Type)
			fmt.Printf("Java: %s / Spring Boot: %s\n", cfg.Metadata.JavaVersion, cfg.Metadata.SpringBootVersion)
			fmt.Printf("Build Tool: %s\n", cfg.Metadata.BuildTool)
			if len(cfg.Architecture.Modules) > 0 {
				fmt.Printf("Modules: %v\n", cfg.Architecture.Modules)
			}
			return nil
		}

		serverURL := viper.GetString("server")
		client := api.NewClient(serverURL, viper.GetString("token"))

		color.Cyan("\n⚙ Validating configuration...")
		validationResult, err := client.Validate(cfg)
		if err != nil {
			return fmt.Errorf("validation failed: %w", err)
		}
		if !validationResult.Valid {
			color.Red("✗ Configuration invalid:")
			for _, e := range validationResult.Errors {
				fmt.Printf("  • %s\n", e)
			}
			return fmt.Errorf("fix the errors above and retry")
		}
		color.Green("✓ Configuration valid")

		color.Cyan("\n⚙ Generating project...")
		genResponse, err := client.Generate(cfg)
		if err != nil {
			return fmt.Errorf("generation request failed: %w", err)
		}

		bar := progressbar.NewOptions(100,
			progressbar.OptionSetDescription("Generating"),
			progressbar.OptionSetWidth(40),
			progressbar.OptionShowCount(),
			progressbar.OptionSetTheme(progressbar.Theme{
				Saucer:        "█",
				SaucerPadding: "░",
				BarStart:      "[",
				BarEnd:        "]",
			}),
		)

		for {
			status, err := client.GetStatus(genResponse.GenerationID)
			if err != nil {
				return fmt.Errorf("failed to check status: %w", err)
			}

			switch status.Status {
			case "COMPLETED":
				bar.Set(100)
				fmt.Println()
				color.Green("✓ Generation completed!")

				dest := outputDir
				if dest == "" {
					dest = "."
				}

				zipPath, err := client.Download(genResponse.GenerationID, dest, cfg.Metadata.ArtifactID)
				if err != nil {
					return fmt.Errorf("download failed: %w", err)
				}
				color.Green("✓ Project downloaded: %s", zipPath)
				return nil

			case "FAILED":
				fmt.Println()
				color.Red("✗ Generation failed: %s", status.ErrorMessage)
				return fmt.Errorf("generation failed")

			default:
				bar.Set(50)
				time.Sleep(1 * time.Second)
			}
		}
	},
}

func init() {
	rootCmd.AddCommand(generateCmd)

	generateCmd.Flags().StringVar(&configFile, "config", "", "path to springforge-config.json (required)")
	generateCmd.Flags().BoolVar(&dryRun, "dry-run", false, "validate and show config without generating")
	generateCmd.Flags().StringVarP(&outputDir, "output", "o", "", "output directory for generated project")
	generateCmd.MarkFlagRequired("config")
}

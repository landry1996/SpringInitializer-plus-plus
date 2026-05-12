package cmd

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	setServer string
	setToken  string
	setOrg    string
)

var configCmd = &cobra.Command{
	Use:   "config",
	Short: "Manage CLI configuration",
}

var configSetCmd = &cobra.Command{
	Use:   "set",
	Short: "Set configuration values",
	RunE: func(cmd *cobra.Command, args []string) error {
		if setServer != "" {
			viper.Set("server", setServer)
		}
		if setToken != "" {
			viper.Set("token", setToken)
		}
		if setOrg != "" {
			viper.Set("organization", setOrg)
		}

		home, _ := os.UserHomeDir()
		configPath := filepath.Join(home, ".springforge.yaml")
		if err := viper.WriteConfigAs(configPath); err != nil {
			return fmt.Errorf("failed to write config: %w", err)
		}

		color.Green("✓ Configuration saved to %s", configPath)
		return nil
	},
}

var configShowCmd = &cobra.Command{
	Use:   "show",
	Short: "Show current configuration",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("Current configuration:")
		fmt.Printf("  server:       %s\n", viper.GetString("server"))
		token := viper.GetString("token")
		if token != "" {
			fmt.Printf("  token:        %s...%s\n", token[:4], token[len(token)-4:])
		} else {
			fmt.Printf("  token:        (not set)\n")
		}
		fmt.Printf("  organization: %s\n", viper.GetString("organization"))
		fmt.Printf("  output:       %s\n", viper.GetString("output"))
	},
}

func init() {
	rootCmd.AddCommand(configCmd)
	configCmd.AddCommand(configSetCmd)
	configCmd.AddCommand(configShowCmd)

	configSetCmd.Flags().StringVar(&setServer, "server", "", "SpringForge server URL")
	configSetCmd.Flags().StringVar(&setToken, "token", "", "authentication token (JWT)")
	configSetCmd.Flags().StringVar(&setOrg, "org", "", "organization ID")
}

package cmd

import (
	"fmt"
	"net/http"
	"time"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var doctorCmd = &cobra.Command{
	Use:   "doctor",
	Short: "Check CLI configuration and server connectivity",
	Long:  `Diagnoses the SpringForge CLI configuration and checks connectivity to the server.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		color.Cyan("SpringForge Doctor\n")
		fmt.Println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

		serverURL := viper.GetString("server")
		token := viper.GetString("token")

		fmt.Printf("Server URL: %s\n", serverURL)
		if token != "" {
			fmt.Printf("Auth Token: %s...%s\n", token[:4], token[len(token)-4:])
		} else {
			color.Yellow("Auth Token: not configured")
		}
		fmt.Println()

		// Check server connectivity
		fmt.Print("Checking server connectivity... ")
		client := &http.Client{Timeout: 5 * time.Second}
		resp, err := client.Get(serverURL + "/actuator/health")
		if err != nil {
			color.Red("FAILED")
			fmt.Printf("  Error: %s\n", err.Error())
			fmt.Println("\n  Fix: Check server URL with 'springforge config set --server <url>'")
			return nil
		}
		defer resp.Body.Close()

		if resp.StatusCode == 200 {
			color.Green("OK")
		} else {
			color.Yellow("WARNING (status %d)", resp.StatusCode)
		}

		// Check API access
		fmt.Print("Checking API access...          ")
		resp2, err := client.Get(serverURL + "/api/v1/blueprints")
		if err != nil {
			color.Red("FAILED")
			return nil
		}
		defer resp2.Body.Close()

		if resp2.StatusCode == 200 {
			color.Green("OK")
		} else if resp2.StatusCode == 401 {
			color.Yellow("UNAUTHORIZED — configure token with 'springforge config set --token <jwt>'")
		} else {
			color.Yellow("WARNING (status %d)", resp2.StatusCode)
		}

		fmt.Println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
		color.Green("Doctor check complete.")
		return nil
	},
}

func init() {
	rootCmd.AddCommand(doctorCmd)
}

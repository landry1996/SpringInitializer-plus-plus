package cmd

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	cfgFile string
	verbose bool
)

var rootCmd = &cobra.Command{
	Use:   "springforge",
	Short: "SpringForge — Intelligent Spring Boot Project Generator",
	Long: `SpringForge CLI generates production-ready Spring Boot projects
with architecture patterns, security, Docker, Kubernetes, CI/CD and more.

Usage:
  springforge init           Interactive wizard to create a project
  springforge generate       Generate project from a config file
  springforge validate       Validate a configuration file
  springforge doctor         Check CLI configuration and connectivity`,
}

func Execute() error {
	return rootCmd.Execute()
}

func init() {
	cobra.OnInitialize(initConfig)

	rootCmd.PersistentFlags().StringVar(&cfgFile, "config", "", "config file (default $HOME/.springforge.yaml)")
	rootCmd.PersistentFlags().BoolVarP(&verbose, "verbose", "v", false, "verbose output")
}

func initConfig() {
	if cfgFile != "" {
		viper.SetConfigFile(cfgFile)
	} else {
		home, err := os.UserHomeDir()
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			os.Exit(1)
		}
		viper.AddConfigPath(home)
		viper.SetConfigName(".springforge")
		viper.SetConfigType("yaml")
	}

	viper.SetEnvPrefix("SPRINGFORGE")
	viper.AutomaticEnv()

	viper.SetDefault("server", "http://localhost:8080")
	viper.SetDefault("output", ".")

	_ = viper.ReadInConfig()
}

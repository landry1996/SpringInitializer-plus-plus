package main

import (
	"os"

	"github.com/landry1996/springforge-cli/cmd"
)

func main() {
	if err := cmd.Execute(); err != nil {
		os.Exit(1)
	}
}

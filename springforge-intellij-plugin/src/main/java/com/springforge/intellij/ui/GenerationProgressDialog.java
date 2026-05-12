package com.springforge.intellij.ui;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.springforge.intellij.api.ApiModels;
import com.springforge.intellij.api.SpringForgeApiClient;
import com.springforge.intellij.settings.SpringForgeSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GenerationProgressDialog extends DialogWrapper {

    private final Project project;
    private final SpringForgeApiClient client;
    private final ApiModels.ProjectConfiguration config;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public GenerationProgressDialog(@Nullable Project project, SpringForgeApiClient client, ApiModels.ProjectConfiguration config) {
        super(project);
        this.project = project;
        this.client = client;
        this.config = config;
        setTitle("Generating Project...");
        init();
        startGeneration();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("Starting generation...");
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        return panel;
    }

    private void startGeneration() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "SpringForge Generation", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("Sending generation request...");
                    ApiModels.GenerateResponse response = client.generate(config);

                    String generationId = response.generationId;
                    indicator.setText("Generation in progress...");

                    while (!indicator.isCanceled()) {
                        ApiModels.StatusResponse status = client.getStatus(generationId);

                        int progress = status.progress;
                        ApplicationManager.getApplication().invokeLater(() -> {
                            progressBar.setValue(progress);
                            statusLabel.setText("Status: " + status.status);
                        });

                        if ("COMPLETED".equals(status.status)) {
                            indicator.setText("Downloading project...");
                            byte[] zipData = client.download(generationId);

                            String outputDir = SpringForgeSettings.getInstance().getDefaultOutputDir();
                            if (outputDir.isEmpty()) {
                                outputDir = System.getProperty("user.home");
                            }

                            String filePath = outputDir + File.separator + config.metadata.artifactId + ".zip";
                            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                                fos.write(zipData);
                            }

                            ApplicationManager.getApplication().invokeLater(() -> {
                                progressBar.setValue(100);
                                statusLabel.setText("Completed!");
                                notify("Project downloaded: " + filePath, NotificationType.INFORMATION);
                            });
                            break;
                        }

                        if ("FAILED".equals(status.status)) {
                            ApplicationManager.getApplication().invokeLater(() -> {
                                statusLabel.setText("Failed: " + status.errorMessage);
                                notify("Generation failed: " + status.errorMessage, NotificationType.ERROR);
                            });
                            break;
                        }

                        Thread.sleep(1000);
                    }
                } catch (IOException | InterruptedException e) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        notify("Error: " + e.getMessage(), NotificationType.ERROR);
                    });
                }
            }
        });
    }

    private void notify(String content, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SpringForge")
            .createNotification(content, type)
            .notify(project);
    }
}

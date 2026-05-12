package com.springforge.intellij.actions;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.google.gson.Gson;
import com.springforge.intellij.api.ApiModels;
import com.springforge.intellij.api.SpringForgeApiClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ValidateConfigAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        VirtualFile baseDir = project.getBaseDir();
        VirtualFile configFile = baseDir.findChild("springforge-config.json");

        if (configFile == null) {
            notify(project, "No springforge-config.json found in project root", NotificationType.WARNING);
            return;
        }

        try {
            String content = new String(configFile.contentsToByteArray(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            ApiModels.ProjectConfiguration config = gson.fromJson(content, ApiModels.ProjectConfiguration.class);

            SpringForgeApiClient client = new SpringForgeApiClient();
            ApiModels.ValidationResult result = client.validate(config);

            if (result.valid) {
                notify(project, "Configuration is valid!", NotificationType.INFORMATION);
            } else {
                String errors = String.join("\n", result.errors);
                notify(project, "Validation errors:\n" + errors, NotificationType.ERROR);
            }
        } catch (IOException ex) {
            notify(project, "Failed to validate: " + ex.getMessage(), NotificationType.ERROR);
        }
    }

    private void notify(Project project, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("SpringForge")
            .createNotification(content, type)
            .notify(project);
    }
}

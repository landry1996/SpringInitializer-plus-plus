package com.springforge.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.springforge.intellij.ui.ProjectConfigDialog;
import org.jetbrains.annotations.NotNull;

public class NewProjectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ProjectConfigDialog dialog = new ProjectConfigDialog(project);
        dialog.show();
    }
}

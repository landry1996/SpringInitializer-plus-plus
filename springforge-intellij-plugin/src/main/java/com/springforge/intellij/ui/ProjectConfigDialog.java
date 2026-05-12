package com.springforge.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.springforge.intellij.api.ApiModels;
import com.springforge.intellij.api.SpringForgeApiClient;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ProjectConfigDialog extends DialogWrapper {

    private JTextField groupIdField;
    private JTextField artifactIdField;
    private JTextField nameField;
    private JTextField descriptionField;
    private JComboBox<String> javaVersionCombo;
    private JComboBox<String> springBootVersionCombo;
    private JComboBox<String> buildToolCombo;
    private JComboBox<String> architectureCombo;
    private JTextField modulesField;
    private JCheckBox observabilityCheck;
    private JCheckBox securityCheck;
    private JCheckBox messagingCheck;
    private JCheckBox helmCheck;

    private final Project project;

    public ProjectConfigDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        setTitle("SpringForge — New Project");
        setSize(500, 600);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        groupIdField = addTextField(panel, gbc, row++, "Group ID:", "com.example");
        artifactIdField = addTextField(panel, gbc, row++, "Artifact ID:", "my-app");
        nameField = addTextField(panel, gbc, row++, "Name:", "my-app");
        descriptionField = addTextField(panel, gbc, row++, "Description:", "Spring Boot application");

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Java Version:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        javaVersionCombo = new JComboBox<>(new String[]{"21", "17", "11"});
        panel.add(javaVersionCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Spring Boot:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        springBootVersionCombo = new JComboBox<>(new String[]{"3.3.5", "3.2.12", "3.1.12"});
        panel.add(springBootVersionCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Build Tool:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        buildToolCombo = new JComboBox<>(new String[]{"MAVEN", "GRADLE"});
        panel.add(buildToolCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Architecture:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        architectureCombo = new JComboBox<>(new String[]{
            "MONOLITHIC", "LAYERED", "HEXAGONAL", "DDD", "CQRS", "EVENT_DRIVEN", "MICROSERVICES", "MODULITH"
        });
        panel.add(architectureCombo, gbc);
        row++;

        modulesField = addTextField(panel, gbc, row++, "Modules:", "user,order,payment");

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        row++;

        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = row++;
        observabilityCheck = new JCheckBox("Enable Observability (metrics, tracing, logging)");
        panel.add(observabilityCheck, gbc);

        gbc.gridy = row++;
        securityCheck = new JCheckBox("Enable Security (OAuth2/OIDC)");
        panel.add(securityCheck, gbc);

        gbc.gridy = row++;
        messagingCheck = new JCheckBox("Enable Kafka Messaging");
        panel.add(messagingCheck, gbc);

        gbc.gridy = row++;
        helmCheck = new JCheckBox("Generate Docker & Helm charts");
        panel.add(helmCheck, gbc);

        return panel;
    }

    private JTextField addTextField(JPanel panel, GridBagConstraints gbc, int row, String label, String defaultValue) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField field = new JTextField(defaultValue, 25);
        panel.add(field, gbc);
        return field;
    }

    @Override
    protected void doOKAction() {
        ApiModels.ProjectConfiguration config = buildConfig();

        try {
            SpringForgeApiClient client = new SpringForgeApiClient();
            ApiModels.ValidationResult validation = client.validate(config);

            if (!validation.valid) {
                String errors = String.join("\n", validation.errors);
                Messages.showErrorDialog(project, "Validation errors:\n" + errors, "SpringForge");
                return;
            }

            GenerationProgressDialog progressDialog = new GenerationProgressDialog(project, client, config);
            progressDialog.show();

        } catch (Exception e) {
            Messages.showErrorDialog(project, "Error: " + e.getMessage(), "SpringForge");
        }

        super.doOKAction();
    }

    private ApiModels.ProjectConfiguration buildConfig() {
        ApiModels.ProjectConfiguration config = new ApiModels.ProjectConfiguration();
        config.metadata.groupId = groupIdField.getText();
        config.metadata.artifactId = artifactIdField.getText();
        config.metadata.name = nameField.getText();
        config.metadata.description = descriptionField.getText();
        config.metadata.packageName = groupIdField.getText() + "." + artifactIdField.getText().replace("-", "");
        config.metadata.javaVersion = (String) javaVersionCombo.getSelectedItem();
        config.metadata.springBootVersion = (String) springBootVersionCombo.getSelectedItem();
        config.metadata.buildTool = (String) buildToolCombo.getSelectedItem();
        config.architecture.type = (String) architectureCombo.getSelectedItem();

        String modulesText = modulesField.getText().trim();
        if (!modulesText.isEmpty()) {
            config.architecture.modules = Arrays.asList(modulesText.split("\\s*,\\s*"));
        }

        if (observabilityCheck.isSelected()) {
            config.observability = new ApiModels.ObservabilityConfig();
            config.observability.enabled = true;
            config.observability.metrics = true;
            config.observability.tracing = true;
            config.observability.structuredLogging = true;
        }

        if (securityCheck.isSelected()) {
            config.security = new ApiModels.SecurityConfig();
            config.security.enabled = true;
            config.security.type = "OAUTH2";
            config.security.provider = "keycloak";
        }

        if (messagingCheck.isSelected()) {
            config.messaging = new ApiModels.MessagingConfig();
            config.messaging.type = "KAFKA";
            config.messaging.topics = List.of("events", "commands");
        }

        if (helmCheck.isSelected()) {
            config.infrastructure = new ApiModels.InfrastructureConfig();
            config.infrastructure.docker = true;
            config.infrastructure.helm = true;
            config.infrastructure.ci = true;
        }

        return config;
    }
}

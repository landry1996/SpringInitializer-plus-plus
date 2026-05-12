package com.springforge.intellij.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SpringForgeSettingsConfigurable implements Configurable {

    private JTextField serverUrlField;
    private JPasswordField tokenField;
    private JTextField organizationField;
    private JTextField outputDirField;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SpringForge";
    }

    @Override
    public @Nullable JComponent createComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        SpringForgeSettings settings = SpringForgeSettings.getInstance();

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Server URL:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        serverUrlField = new JTextField(settings.getServerUrl(), 30);
        panel.add(serverUrlField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Auth Token:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tokenField = new JPasswordField(settings.getToken(), 30);
        panel.add(tokenField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Organization:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        organizationField = new JTextField(settings.getOrganization(), 30);
        panel.add(organizationField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Default Output:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        outputDirField = new JTextField(settings.getDefaultOutputDir(), 30);
        panel.add(outputDirField, gbc);

        return panel;
    }

    @Override
    public boolean isModified() {
        SpringForgeSettings settings = SpringForgeSettings.getInstance();
        return !serverUrlField.getText().equals(settings.getServerUrl()) ||
               !new String(tokenField.getPassword()).equals(settings.getToken()) ||
               !organizationField.getText().equals(settings.getOrganization()) ||
               !outputDirField.getText().equals(settings.getDefaultOutputDir());
    }

    @Override
    public void apply() {
        SpringForgeSettings settings = SpringForgeSettings.getInstance();
        settings.setServerUrl(serverUrlField.getText());
        settings.setToken(new String(tokenField.getPassword()));
        settings.setOrganization(organizationField.getText());
        settings.setDefaultOutputDir(outputDirField.getText());
    }

    @Override
    public void reset() {
        SpringForgeSettings settings = SpringForgeSettings.getInstance();
        serverUrlField.setText(settings.getServerUrl());
        tokenField.setText(settings.getToken());
        organizationField.setText(settings.getOrganization());
        outputDirField.setText(settings.getDefaultOutputDir());
    }
}

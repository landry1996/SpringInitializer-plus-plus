package com.springforge.intellij.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "SpringForgeSettings",
    storages = @Storage("springforge.xml")
)
public class SpringForgeSettings implements PersistentStateComponent<SpringForgeSettings.State> {

    private State state = new State();

    public static SpringForgeSettings getInstance() {
        return ApplicationManager.getApplication().getService(SpringForgeSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public String getServerUrl() {
        return state.serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        state.serverUrl = serverUrl;
    }

    public String getToken() {
        return state.token;
    }

    public void setToken(String token) {
        state.token = token;
    }

    public String getOrganization() {
        return state.organization;
    }

    public void setOrganization(String organization) {
        state.organization = organization;
    }

    public String getDefaultOutputDir() {
        return state.defaultOutputDir;
    }

    public void setDefaultOutputDir(String dir) {
        state.defaultOutputDir = dir;
    }

    public static class State {
        public String serverUrl = "http://localhost:8080";
        public String token = "";
        public String organization = "";
        public String defaultOutputDir = "";
    }
}

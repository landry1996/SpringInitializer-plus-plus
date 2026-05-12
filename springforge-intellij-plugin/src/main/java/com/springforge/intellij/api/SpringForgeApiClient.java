package com.springforge.intellij.api;

import com.google.gson.Gson;
import com.springforge.intellij.settings.SpringForgeSettings;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SpringForgeApiClient {

    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client;
    private final Gson gson = new Gson();

    public SpringForgeApiClient() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    private String getBaseUrl() {
        return SpringForgeSettings.getInstance().getServerUrl();
    }

    private String getToken() {
        return SpringForgeSettings.getInstance().getToken();
    }

    private Request.Builder buildRequest(String path) {
        Request.Builder builder = new Request.Builder().url(getBaseUrl() + path);
        String token = getToken();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder;
    }

    public ApiModels.ValidationResult validate(ApiModels.ProjectConfiguration config) throws IOException {
        String json = gson.toJson(config);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = buildRequest("/api/v1/projects/validate")
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Validation failed: " + response.code());
            }
            return gson.fromJson(response.body().string(), ApiModels.ValidationResult.class);
        }
    }

    public ApiModels.GenerateResponse generate(ApiModels.ProjectConfiguration config) throws IOException {
        String json = gson.toJson(config);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = buildRequest("/api/v1/projects/generate")
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Generation failed: " + response.code());
            }
            return gson.fromJson(response.body().string(), ApiModels.GenerateResponse.class);
        }
    }

    public ApiModels.StatusResponse getStatus(String generationId) throws IOException {
        Request request = buildRequest("/api/v1/projects/" + generationId + "/status")
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Status check failed: " + response.code());
            }
            return gson.fromJson(response.body().string(), ApiModels.StatusResponse.class);
        }
    }

    public byte[] download(String generationId) throws IOException {
        Request request = buildRequest("/api/v1/projects/" + generationId + "/download")
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Download failed: " + response.code());
            }
            return response.body().bytes();
        }
    }

    public boolean checkHealth() {
        try {
            Request request = buildRequest("/actuator/health").get().build();
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }
}

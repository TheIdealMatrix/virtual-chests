package de.kevin_stefan.virtualChests.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class UpdateNotifier {

    private final static String MODRINTH_API = "https://api.modrinth.com/v2/project/{id}/version?include_changelog=false";

    private final String projectId;
    private @Nullable Logger logger;
    private @Nullable String projectVersion;
    private @Nullable String minecraftVersion;

    private @Nullable Consumer<Exception> onError = null;
    private @Nullable Consumer<String> onLatestVersion = null;
    private @Nullable Consumer<String> onNewVersion = null;

    public UpdateNotifier(String projectId) {
        this.projectId = projectId;
    }

    public UpdateNotifier logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public UpdateNotifier projectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
        return this;
    }

    public UpdateNotifier minecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
        return this;
    }

    public UpdateNotifier onError(Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public UpdateNotifier onLatestVersion(Consumer<String> onLatestVersion) {
        this.onLatestVersion = onLatestVersion;
        return this;
    }

    public UpdateNotifier onNewVersion(Consumer<String> onNewVersion) {
        this.onNewVersion = onNewVersion;
        return this;
    }

    public void run() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(getURI()).GET().build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAcceptAsync(this::handleResponse);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private URI getURI() {
        String url = MODRINTH_API.replace("{id}", this.projectId);
        if (this.minecraftVersion != null) {
            url = url + "&game_versions=" + URLEncoder.encode("[\"" + this.minecraftVersion + "\"]", StandardCharsets.UTF_8);
        }
        return URI.create(url);
    }

    private void handleResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            handleError(new RuntimeException("Wrong status code: " + response.statusCode()));
            return;
        }

        try {
            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
            JsonObject latestVersionObject = jsonArray.asList().getFirst().getAsJsonObject();
            String latestVersion = latestVersionObject.get("version_number").getAsString();
            if (this.onLatestVersion != null) {
                this.onLatestVersion.accept(latestVersion);
            }
            if (this.projectVersion != null && !this.projectVersion.equalsIgnoreCase(latestVersion)) {
                if (this.onNewVersion != null) {
                    this.onNewVersion.accept(latestVersion);
                }
                if (logger != null) {
                    logger.warning("There is a newer version available: " + latestVersion + ", you're on: " + this.projectVersion);
                }
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        if (this.onError != null) {
            this.onError.accept(e);
        }
    }

}

package com.objectt.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class SkinRepository {
    private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final UUID uuid;

    public SkinRepository(UUID uuid) {
        this.uuid = uuid;
    }

    public Skin fetch() throws IOException, InterruptedException {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + uuid.toString())).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            String name = jsonObject.get("name").getAsString();
            String texture = jsonObject.get("properties").getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).filter(o -> o.get("value") != null).findFirst().get().get("value").getAsString();
            return new Skin(this.uuid, name, texture);
        }
    }

    public record Skin(UUID uuid, String name, String texture) {}
}

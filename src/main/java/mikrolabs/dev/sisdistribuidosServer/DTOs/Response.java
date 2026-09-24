package mikrolabs.dev.sisdistribuidosServer.DTOs;

import com.google.gson.JsonElement;

public record Response(
        int statusCode,
        String message,
        JsonElement data
) {
    public static Response success(String result, JsonElement data) {
        return new Response(200, result, data);
    }
    public static Response success(String result) {
        return new Response(200, result, null);
    }
    public static Response error(int status, String message) {
        return new Response(status, message, null);
    }
}

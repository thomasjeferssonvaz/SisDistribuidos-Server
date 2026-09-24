package mikrolabs.dev.sisdistribuidosServer.DTOs;

import com.google.gson.JsonElement;

public record Request(
        String method,
        JsonElement data
) {}

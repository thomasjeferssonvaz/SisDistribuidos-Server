package mikrolabs.dev.sisdistribuidosServer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import mikrolabs.dev.sisdistribuidosServer.DTOs.LoginDTO;
import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;
import mikrolabs.dev.sisdistribuidosServer.exceptions.EmptyAction;

import java.util.Map;
import java.util.UUID;

public class UserController {
    private static final Gson gson = new Gson();

    public Response login(String inputData) {
        if (inputData == null || inputData.isBlank()) return Response.error(400, "Dados de login ausentes");

        try {
            LoginDTO loginDTO = gson.fromJson(inputData, LoginDTO.class);

            if (loginDTO == null || loginDTO.username() == null ||  loginDTO.password() == null) {
                return Response.error(400, "Usuário e senha são obrigatórios");
            }

            if ("thomas".equals(loginDTO.username()) && "thomas".equals(loginDTO.password())) {
                String data = gson.toJson(Map.of("username", loginDTO.username(), "token", generateToken()));
                return Response.success("Login realizado com sucesso", data);
            }

            return Response.error(401, "Credenciais inválidas");
        } catch (JsonSyntaxException e) {
            return Response.error(400, "Payload de login inválido");
        }
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}

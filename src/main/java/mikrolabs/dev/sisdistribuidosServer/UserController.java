package mikrolabs.dev.sisdistribuidosServer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;
import mikrolabs.dev.sisdistribuidosServer.DTOs.User;
import mikrolabs.dev.sisdistribuidosServer.exceptions.BaseException;
import mikrolabs.dev.sisdistribuidosServer.exceptions.UserAlreadyExists;
import mikrolabs.dev.sisdistribuidosServer.repositories.SessionRepository;
import mikrolabs.dev.sisdistribuidosServer.repositories.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserController {
    private static final Gson gson = new Gson();
    private static final UserRepository userRepository = new UserRepository();
    public static final SessionRepository sessionRepository = new SessionRepository();
    private final SecurityService securityService = new SecurityService();
    public record LogoutRequest(UUID token) {}

    public Response login(JsonElement inputData) {
        if (inputData == null) return Response.error(400, "Dados de login ausentes");

        try {
            User user = gson.fromJson(inputData, User.class);

            if (user == null || user.username() == null ||  user.password() == null) {
                return Response.error(400, "Usuário e senha são obrigatórios");
            }
            Optional<User> loggedUser = userRepository.getUserByCredencials(user.username(), user.password());

            if (loggedUser.isPresent()) {
                UUID token = UUID.randomUUID();
                JsonElement data = gson.toJsonTree(Map.of("token", token));
                sessionRepository.registerSession(token, loggedUser);

                System.out.println("Sessões: "+ sessionRepository.getAllSessions());
                return Response.success("Login realizado com sucesso", data);
            }

            return Response.error(401, "Credenciais inválidas");
        } catch (JsonSyntaxException e) {
            return Response.error(400, "Payload de login inválido: " + inputData);
        }
    }

    public Response register(JsonElement inputData) throws BaseException {
        try {
            User user = gson.fromJson(inputData, User.class);
            SecurityService.ValidationResultDTO validationResult = securityService.validate(user.password());

            if (user.name() == null || user.username() == null ||  user.password() == null) {
                return Response.error(400, "Nome, usuário e senha são obrigatórios");
            }

            if (user.name().isBlank() || user.username().isBlank() ||  user.password().isBlank()) {
                return Response.error(400, "Nome, usuário e senha são obrigatórios");
            }

            if (!validationResult.isValid()) {
                return Response.error(400, """
                        senha não compatível com os parâmetros necessários:\s
                        Símbolos especiais liberados: #, ., *, &, %, $, @, !, (, ), -, _, =, +, ..\s
                        Min caracteres: 8
                        Máx caracteres: 20
                       \s""");
            }

            if (userRepository.registerUser(user.name(), user.username(), user.password()).isPresent()) {
                return Response.success("Usuário criado com sucesso");
            }

            return new UserAlreadyExists(user.username()).toResponse();
        } catch (JsonSyntaxException e) {
            return Response.error(400, "Payload de login inválido");
        }
    }

    public Response logout(JsonElement inputData) {
        try {
            LogoutRequest request = gson.fromJson(inputData, LogoutRequest.class);

            if (request != null && request.token() != null) {
                if(sessionRepository.deleteSessionByToken(request.token())) {
                    System.out.println("Sessões: "+ sessionRepository.getAllSessions());
                    return Response.success("Logout realizado com sucesso");
                } else {
                    return Response.error(404,"Token não encontrado");
                }

            }
            return Response.error(400, "Token não fornecido");
        } catch (Exception e) {
            return Response.error(400, "Formato do payload inválido");
        }
    }
}

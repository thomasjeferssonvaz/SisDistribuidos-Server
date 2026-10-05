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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserController {
    private static final Gson gson = new Gson();
    private static final UserRepository userRepository = new UserRepository();
    public static final SessionRepository sessionRepository = new SessionRepository();
    private final SecurityService securityService = new SecurityService();
    private final UserValidationService userValidationService =
            new UserValidationService();
    public record Token(UUID token) {}

    public Response login(JsonElement inputData) {
        if (inputData == null) return Response.error(400, "Dados de login ausentes");

        try {
            User user = gson.fromJson(inputData, User.class);

            if (user == null || user.username() == null ||  user.password() == null) {
                return Response.error(400, "Usuário e senha são obrigatórios");
            }
            Optional<User> loggedUser = userRepository.getUserByCredentials(user.username(), user.password());

            if (loggedUser.isPresent()) {
                UUID token = UUID.randomUUID();
                JsonElement data = gson.toJsonTree(Map.of("token", token));
                sessionRepository.registerSession(token, loggedUser.get().username());
                System.out.println("[CLIENTE] Login: " + loggedUser.get().username());
                //System.out.println("User logged: " + loggedUser.get().username());
                //System.out.println("User Logged" + loggedUser);
                //System.out.println("Sessões: "+ sessionRepository.getAllSessions());
                return Response.success("Login realizado com sucesso", data);
            }

            return Response.error(401, "Usuario ou senhas inválidos");
        } catch (JsonSyntaxException e) {
            return Response.error(400, "Payload de login inválido: " + inputData);
        }
    }

    public Response register(JsonElement inputData) throws BaseException {
        try {
            User user = gson.fromJson(inputData, User.class);
            if (user == null) return Response.error(400, "payload incorreta");

            if (!userValidationService.isNameValid(user.name())) {
                return Response.error(
                        400,
                        "O campo name não está no padrão esperado."
                );
            }

            if (!userValidationService.isUsernameValid(user.username())) {
                return Response.error(
                        400,
                        "O campo username não está no padrão esperado."
                );
            }

            if (!userValidationService.hasOnlyAllowedPasswordCharacters(user.password())) {
                return Response.error(
                        400,
                        "O campo password não está no padrão esperado."
                );
            }

            SecurityService.ValidationResultDTO validationResult = securityService.validate(user.password());

            if (!validationResult.isValid()) {
                return Response.error(
                        400,
                        "O campo password não está no padrão esperado."
                );
            }
            Optional<User> createdUser = userRepository.registerUser(user.name(), user.username(), user.password());
            if (createdUser.isPresent()) {
                System.out.println("Created User: " + createdUser);
                return Response.created("Usuário criado com sucesso");
            }


            return new UserAlreadyExists(user.username()).toResponse();
        } catch (JsonSyntaxException e) {
            return Response.error(400, "Payload de cadastro inválido");
        }
    }

    public Response logout(JsonElement token) {
        try {
            Token tokenJson = gson.fromJson(token, Token.class);

            if (tokenJson == null || tokenJson.token() == null) {
                return missingTokenResponse();
            }
            if (isTokenInvalid(tokenJson.token())) {
                return unauthorizedTokenResponse();
            }

            sessionRepository.deleteSessionByToken(tokenJson.token());
            System.out.println("[CLIENTE] Logout: " + tokenJson.token());
            return Response.success("Logout realizado com sucesso");
        } catch (Exception e) {
            return Response.error(400, "Formato do payload inválido");
        }
    }

    public Response getUserByToken(JsonElement inputData) {

        if (inputData == null) return Response.error(400, "nenhum usuário enviado");
        try  {
            Token tokenJson =  gson.fromJson(inputData, Token.class);
            if (tokenJson == null || tokenJson.token() == null) {
                return missingTokenResponse();
            }
            if (isTokenInvalid(tokenJson.token())) {
                return unauthorizedTokenResponse();
            }
            String username = sessionRepository.getUsernameByToken(tokenJson.token());
            Optional<User> user = userRepository.getUserByUsername(username);

            if(user.isPresent()) {
                JsonElement userData = gson.toJsonTree(Map.of(
                        "username", user.get().username(),
                        "name", user.get().name()
                ));
                return new Response(200, "Usuário encontrado com sucesso", userData);
            }
            return Response.error(404, "Usuário não encontrado");
        } catch (JsonSyntaxException e) {
            return Response.error(400, "Formato do payload inválido");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(500, "Erro interno no servidor");
        }

    }

    public boolean isTokenInvalid(UUID token) {
        return sessionRepository.getSessionByToken(token).isEmpty();
    }

    public Response updateUserName(JsonElement inputData) {
        record UpdateUserName(UUID token, String username, String name){}
        try {
            UpdateUserName receivedJson = gson.fromJson(inputData, UpdateUserName.class);
            if (receivedJson == null || receivedJson.token() == null) {
                return missingTokenResponse();
            }

            if (isTokenInvalid(receivedJson.token())) {
                return unauthorizedTokenResponse();
            }

            if (!userValidationService.isUsernameValid(receivedJson.username())) {
                return Response.error(
                        400,
                        "O campo username não está no padrão esperado."
                );
            }

            if (!userValidationService.isNameValid(receivedJson.name())) {
                return Response.error(
                        400,
                        "Nome do usuário fora do padrão"
                );
            }

            String authenticatedUsername =
                    sessionRepository.getUsernameByToken(receivedJson.token());

            if (!Objects.equals(receivedJson.username(), authenticatedUsername)) {
                return Response.error(401, "Operação não autorizada");
            }

            Optional<User> updatedUser = userRepository.updateUserName(
                    authenticatedUsername,
                    receivedJson.name()
            );

            if (updatedUser.isEmpty()) {
                return Response.error(404, "Usuário não encontrado");
            }

            return Response.success("Nome alterado com sucesso");
        } catch (Exception e) {
            System.out.println();
            return Response.error(400, "Formato do payload inválido");
        }


    }

    public Response updatePassword(JsonElement inputData) {
        record UpdatePassword(UUID token, String username, String oldPassword, String newPassword){}
        try {
            UpdatePassword receivedJson = gson.fromJson(inputData, UpdatePassword.class);

            if (receivedJson == null || receivedJson.token() == null) {
                return missingTokenResponse();
            }

            if (isTokenInvalid(receivedJson.token())) {
                return unauthorizedTokenResponse();
            }

            if (receivedJson.newPassword().equals(receivedJson.oldPassword())) return Response.error(401, "Senha atual incorreta");

            if (!userValidationService.hasOnlyAllowedPasswordCharacters(receivedJson.newPassword())) return Response.error(400, """
                        senha não compatível com os parâmetros mínimos necessários:\s
                        Símbolos especiais liberados: #, ., *, &, %, $, @, !, (, ), -, _, =, +, .\s
                        Min caracteres: 8
                        Máx caracteres: 20
                       \s""");

            String authenticatedUsername =
                    sessionRepository.getUsernameByToken(receivedJson.token());

            if (Objects.equals(receivedJson.username(), authenticatedUsername)) {
                Optional<User> updatedUser = userRepository.updateUserPassword(
                        authenticatedUsername,
                        receivedJson.newPassword()
                );

                if (updatedUser.isEmpty()) {
                    return Response.error(404, "Usuário não encontrado");
                }

                System.out.println("Updated User: " + updatedUser.get());
                return Response.success("Senha atualizada com sucesso");

            }
            return Response.error(401, "Função de Admin, ainda não implementada");

        } catch (Exception e) {
            System.out.println();
            return Response.error(400, "Formato do payload inválido");
        }

    }

    public Response deleteUser(JsonElement inputData) {
        record UpdatePassword(UUID token, String username){}
        try {
            UpdatePassword receivedJson = gson.fromJson(inputData, UpdatePassword.class);

            if (receivedJson == null || receivedJson.token() == null) {
                return missingTokenResponse();
            }

            if (isTokenInvalid(receivedJson.token())) {
                return unauthorizedTokenResponse();
            }

            String authenticatedUsername =
                    sessionRepository.getUsernameByToken(receivedJson.token());

            if (Objects.equals(receivedJson.username(), authenticatedUsername)) {
                Optional<User> updatedUser = userRepository.deleteUser(
                        authenticatedUsername
                );

                if (updatedUser.isEmpty()) {
                    return Response.error(404, "Usuário não encontrado");
                }

                System.out.println("Deleted User: " + updatedUser.get());
                return Response.success("Usuário deletado com sucesso");

            }
            return Response.error(401, "Função de Admin, ainda não implementada");

        } catch (Exception e) {
            System.out.println();
            return Response.error(400, "Formato do payload inválido");
        }
    }

    private Response missingTokenResponse() {
        return Response.error(
                400,
                "Token de autenticação não fornecido."
        );
    }

    private Response unauthorizedTokenResponse() {
        return Response.error(
                401,
                "Sessão expirada ou encerrada."
        );
    }
}

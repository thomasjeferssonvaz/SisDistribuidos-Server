# TODO pendente — SisDistribuidos Server

Este arquivo contém somente alterações ainda não aplicadas.

- `[ ]` = alteração pendente e ainda não autorizada.
- Depois de marcar os itens desejados com `[x]`, avise o assistente para aplicar somente os itens assinalados.
- Não serão criados testes automatizados; a validação do protocolo será feita manualmente pelo cliente.
- CRUD de Admin permanece fora do escopo enquanto estiver “a ser especificado”.

## Dependências

- Para disponibilizar a atualização de senha, marcar **2.1** e **2.2** juntos.
- Para disponibilizar a exclusão de cadastro, marcar **3.1**, **3.2**, **3.3** e **3.4** juntos.

## 1. Garantir `data: null` nas respostas JSON

- [ ] **1.1 — Ativar serialização de valores nulos no Gson do transporte.**

  Arquivo: `BasicFunctionsClient.java`

  Adicionar:

  ```java
  import com.google.gson.GsonBuilder;
  ```

  Substituir:

  ```java
  private static final Gson gson = new Gson();
  ```

  por:

  ```java
  private static final Gson gson = new GsonBuilder()
          .serializeNulls()
          .create();
  ```

  Motivo: o Gson padrão omite propriedades nulas. Sem essa alteração, respostas sem dados não seguem obrigatoriamente o formato `{ "statusCode": int, "message": String, "data": null }`.

## 2. Implementar atualização de senha

- [ ] **2.1 — Adicionar a ação ao protocolo.**

  Arquivo: `BasicFunctionsClient.java`, `switch` de `executeAction`

  Adicionar:

  ```java
  case "updateuserpassword" ->
          userController.updateUserPassword(inputData);
  ```

- [ ] **2.2 — Implementar `UserController.updateUserPassword`.**

  Arquivo: `UserController.java`

  Adicionar antes de `isTokenInvalid`:

  ```java
  public Response updateUserPassword(JsonElement inputData) {
      record UpdateUserPassword(
              UUID token,
              String username,
              String oldPassword,
              String newPassword
      ) {}

      try {
          UpdateUserPassword request = gson.fromJson(
                  inputData,
                  UpdateUserPassword.class
          );

          if (request == null || request.token() == null) {
              return missingTokenResponse();
          }
          if (isTokenInvalid(request.token())) {
              return unauthorizedTokenResponse();
          }
          if (!userValidationService.isUsernameValid(request.username())
                  || request.oldPassword() == null
                  || request.newPassword() == null) {
              return Response.error(400, "Formato do payload inválido");
          }

          String authenticatedUsername =
                  sessionRepository.getUsernameByToken(request.token());

          if (!Objects.equals(request.username(), authenticatedUsername)) {
              return Response.error(401, "Operação não autorizada");
          }

          Optional<User> user = userRepository.getUserByCredentials(
                  request.username(),
                  request.oldPassword()
          );

          if (user.isEmpty()) {
              return Response.error(401, "Operação não autorizada");
          }

          SecurityService.ValidationResultDTO passwordResult =
                  securityService.validate(request.newPassword());

          if (!userValidationService.hasOnlyAllowedPasswordCharacters(
                  request.newPassword()
          ) || !passwordResult.isValid()) {
              return Response.error(400, "Senha fora do padrão");
          }

          Optional<User> updatedUser = userRepository.updateUserPassword(
                  request.username(),
                  request.newPassword()
          );

          if (updatedUser.isEmpty()) {
              return Response.error(404, "Usuário não encontrado");
          }

          return Response.success("Senha alterada com sucesso");
      } catch (JsonSyntaxException e) {
          return Response.error(400, "Formato do payload inválido");
      } catch (Exception e) {
          e.printStackTrace();
          return Response.error(500, "Erro interno no servidor");
      }
  }
  ```

## 3. Implementar exclusão de cadastro

- [ ] **3.1 — Adicionar remoção de usuário ao repositório.**

  Arquivo: `repositories/UserRepository.java`

  Adicionar:

  ```java
  public boolean deleteUserByUsername(String username) {
      if (username == null) return false;

      synchronized (users) {
          return users.removeIf(
                  user -> Objects.equals(user.username(), username)
          );
      }
  }
  ```

- [ ] **3.2 — Adicionar remoção das sessões de um usuário.**

  Arquivo: `repositories/SessionRepository.java`

  Adicionar:

  ```java
  public void deleteSessionsByUsername(String username) {
      if (username == null) return;

      sessions.removeIf(
              session -> Objects.equals(session.username(), username)
      );
  }
  ```

- [ ] **3.3 — Implementar `UserController.deleteUser`.**

  Arquivo: `UserController.java`

  Adicionar antes de `isTokenInvalid`:

  ```java
  public Response deleteUser(JsonElement inputData) {
      record DeleteUser(UUID token, String username) {}

      try {
          DeleteUser request = gson.fromJson(inputData, DeleteUser.class);

          if (request == null || request.token() == null) {
              return missingTokenResponse();
          }
          if (isTokenInvalid(request.token())) {
              return unauthorizedTokenResponse();
          }
          if (!userValidationService.isUsernameValid(request.username())) {
              return Response.error(400, "Formato do payload inválido");
          }

          String authenticatedUsername =
                  sessionRepository.getUsernameByToken(request.token());

          if (!Objects.equals(request.username(), authenticatedUsername)) {
              return Response.error(401, "Operação não autorizada");
          }

          if (!userRepository.deleteUserByUsername(request.username())) {
              return Response.error(404, "Usuário não encontrado");
          }

          sessionRepository.deleteSessionsByUsername(request.username());
          return Response.success("Usuário excluído com sucesso");
      } catch (JsonSyntaxException e) {
          return Response.error(400, "Formato do payload inválido");
      } catch (Exception e) {
          e.printStackTrace();
          return Response.error(500, "Erro interno no servidor");
      }
  }
  ```

- [ ] **3.4 — Adicionar a ação de exclusão ao protocolo.**

  Arquivo: `BasicFunctionsClient.java`, `switch` de `executeAction`

  Adicionar:

  ```java
  case "deleteuser" -> userController.deleteUser(inputData);
  ```

  O cliente deverá usar o mesmo nome `deleteuser`.

## 4. Correções encontradas na revalidação

- [ ] **4.1 — Tratar campos vazios no login como requisição inválida.**

  Arquivo: `UserController.java`, método `login`

  Substituir:

  ```java
  if (user == null || user.username() == null || user.password() == null) {
  ```

  por:

  ```java
  if (user == null
          || user.username() == null
          || user.username().isBlank()
          || user.password() == null
          || user.password().isBlank()) {
  ```

- [ ] **4.2 — Separar payload inválido de erro interno em `updateUserName`.**

  Arquivo: `UserController.java`, método `updateUserName`

  Substituir o `catch (Exception e)` atual por:

  ```java
  } catch (JsonSyntaxException e) {
      return Response.error(400, "Formato do payload inválido");
  } catch (Exception e) {
      e.printStackTrace();
      return Response.error(500, "Erro interno no servidor");
  }
  ```

- [ ] **4.3 — Eliminar os avisos de serialização das exceções.**

  Arquivos: `BaseException.java`, `ActionNotFound.java`, `EmptyAction.java`, `EmptyData.java` e `UserAlreadyExists.java`.

  Adicionar em cada classe:

  ```java
  import java.io.Serial;

  @Serial
  private static final long serialVersionUID = 1L;
  ```

  Em `ActionNotFound`, remover também o campo não utilizado:

  ```java
  Request request;
  ```

  e a atribuição:

  ```java
  this.request = request;
  ```

## 5. Validação manual pelo cliente

- [ ] Toda resposta contém `statusCode`, `message` e `data`, inclusive `data: null`.
- [ ] Login: sucesso, username inexistente, senha incorreta, campos ausentes e campos vazios.
- [ ] Cadastro: limites e caracteres inválidos de `name`, `username` e `password`; username duplicado.
- [ ] Logout: token válido, ausente, UUID malformado e sessão inexistente.
- [ ] Consulta: token válido, inválido e usuário inexistente.
- [ ] Atualização de nome: nome válido/inválido, username divergente e token inválido.
- [ ] Atualização de senha: senha antiga correta/incorreta e senha nova válida/inválida.
- [ ] Exclusão: próprio usuário, username divergente e reutilização do token após exclusão.
- [ ] Cada requisição abre uma nova conexão TCP e a conexão é encerrada após a resposta.
- [ ] Chaves em inglês/camelCase e métodos em lower case.
- [ ] Compilar o servidor e executar novamente o fluxo completo pelo cliente.

package mikrolabs.dev.sisdistribuidosServer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import mikrolabs.dev.sisdistribuidosServer.DTOs.Request;
import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;
import mikrolabs.dev.sisdistribuidosServer.exceptions.ActionNotFound;
import mikrolabs.dev.sisdistribuidosServer.exceptions.BaseException;
import mikrolabs.dev.sisdistribuidosServer.exceptions.EmptyAction;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BasicFunctionsClient extends Thread {
    private final Socket client;
    private static final Gson gson = new Gson();
    private final UserController userController;


    public BasicFunctionsClient(Socket client, UserController userController) {
        this.client = client;
        this.userController = userController;
    }


    @Override
    public void run() {
        String clientAddress = client.getInetAddress().getHostAddress();

        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),
                        true
                )
        ) {
            String jsonLine;
            while((jsonLine = in.readLine()) != null) {
                jsonLine = jsonLine.trim();
                if (jsonLine.isEmpty()) continue;
                if("sair".equalsIgnoreCase(jsonLine)) break;

                Response response;
                try {
                    Request request = gson.fromJson(jsonLine, Request.class);
                    System.out.println(jsonLine);
                    System.out.println(request);
                    response = request != null ? executeAction(request) : Response.error(400, "request nulo");
                } catch (BaseException e) {
                    response = e.toResponse();
                } catch (JsonSyntaxException e) {
                    response = Response.error(400, "JSON malformado");
                }
                out.println(gson.toJson(response));

            }
        } catch (IOException e) {
            System.err.println("Erro de I/O com " + clientAddress + ": " + e.getMessage());
        } finally {
            try {
                if (!client.isClosed()) client.close();
                System.out.println("Cliente desconectado: " + clientAddress);
            } catch (IOException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
    private Response executeAction(Request request) throws BaseException {
        if (request.action() == null || request.action().isBlank()) throw new EmptyAction();

        String inputData = request.data() != null ? request.data() : "";

        return switch (request.action()) {
            case "textToUpperCase" -> Response.success(inputData.toUpperCase());
            case "login" -> userController.login(inputData);
            default -> throw new ActionNotFound(inputData, request);
        };
    }

}

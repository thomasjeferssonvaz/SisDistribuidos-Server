package mikrolabs.dev.sisdistribuidosServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 34345;
        System.out.println("Inicializando o servidor");
        UserController userController = new UserController();

        try (ServerSocket server = new ServerSocket(port)){
            System.out.println("Servidor iniciado com sucesso na porta: " + port);

            while(!server.isClosed()) {
                try {
                    Socket client =  server.accept();
                    System.out.println("IP: " + client.getInetAddress().getHostAddress() + "abriu uma requisição ao servidor");
                    new BasicFunctionsClient(client, userController).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());

                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao tentar abrir o servidor\n" +  e.getMessage());
        }

    }
}

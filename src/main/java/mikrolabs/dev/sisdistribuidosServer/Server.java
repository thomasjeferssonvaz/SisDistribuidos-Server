package mikrolabs.dev.sisdistribuidosServer;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 34345;

            System.out.println("Inicializando o servidor");

            ServerSocket server = new ServerSocket(port);
            System.out.println("Servidor iniciado com sucesso na porta: " + port);

            UserController userController = new UserController();

            while(true) {
                Socket client =  server.accept();
                System.out.println("Acessando o servidor: " + client.getInetAddress().getHostAddress());
                new BasicFunctionsClient(client, userController).start();
            }
        } catch (Exception e) {
            System.out.println("Erro ao tentar abrir o servidor\n" +  e.getMessage());
        }

    }
}

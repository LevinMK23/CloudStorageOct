package HW1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
   // public static final int PORT = 1313;

    public Server() {
        ExecutorService run = Executors.newFixedThreadPool(4);
        try (ServerSocket serverSocket = new ServerSocket(1313)) {
            while (true){
                System.out.println("Waiting for connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client accepted - " + socket);
                run.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

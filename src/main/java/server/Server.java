package server;

import handler.ClientHandler;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public Server() {
        ExecutorService runner = Executors.newFixedThreadPool(2);
        try(ServerSocket  server = new ServerSocket(8189)){
            while (true) {
                runner.execute(new ClientHandler(server.accept()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}

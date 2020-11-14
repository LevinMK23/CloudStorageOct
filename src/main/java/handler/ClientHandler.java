package handler;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            while (true) {
                String command = dis.readUTF();
                String dir = null;
                if (command.equals("upload")) {
                    dir = "server/";
                } else if (command.equals("download")) {
                    dir = "client/";
                }
                try {
                    File file = new File(dir + dis.readUTF());
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    long size = dis.readLong();
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[256];
                    for (int i = 0; i < (size + 255) / 256; i++) {
                        int read = dis.read(buffer);
                        fos.write(buffer, 0, read);
                    }
                    fos.close();
                    dos.writeUTF("OK");
                } catch (Exception e) {
                    dos.writeUTF(command + " WRONG");
                }
                System.out.println(command);
            }
        } catch (SocketException socketException) {
            System.out.println("Client disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

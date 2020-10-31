package HW1;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable{

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (true) {
                String cmd = in.readUTF();
                System.out.println(cmd);

                if (cmd.equals("exit")) {
                    System.out.println("Client disconnected.");
                    out.writeUTF("BYE");
                    break;
                }

                if (cmd.equals("upload")) {
                    try {
                        String nameFie = in.readUTF();
                        File file = new File("server/" + nameFie);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        long size = in.readLong();
                        FileOutputStream fileByte = new FileOutputStream(file);
                        byte[] buf = new byte[256];
                        for (int i = 0; i < (size + 255) / 256; i++) { // не понял, как здесь высчитали размер - ДО какого i считать
                            int read = in.read(buf);
                            fileByte.write(buf, 0, read);
                        }
                        fileByte.close();
                        out.writeUTF("OK");
                    } catch (Exception e) {
                        out.writeUTF("ERROR");
                    }

                }

                if (cmd.equals("download")) {
                    //TODO 27102020
                    try {
                        String nameFile = in.readUTF();
                        File file = new File("server/" + nameFile);
                        long size = file.length();
                        out.writeLong(size);
                        FileInputStream fileByte = new FileInputStream(file);
                        int read = 0;
                        byte[] buf = new byte[256];
                        while ((read = fileByte.read(buf)) != -1) {
                            out.write(buf, 0, read);
                        }
                        out.flush();
                        //String statusOperation = in.readUTF();
                        //System.out.println("Status operation - " + statusOperation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        } catch (SocketException  socketException) {
            System.out.println("Client disconnected");
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }


}

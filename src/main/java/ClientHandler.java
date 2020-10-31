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
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            System.out.println("Client connected");
            while (true) {
                String command = in.readUTF();
                System.out.println("received command: " + command);

                if (command.equals("upload")) {
                    upload(out, in);
                }
                if (command.equals("download")) {
                    download(out, in);
                }
                if (command.equals("exit")) {
                    System.out.println("Client disconnected correctly");
                    out.writeUTF("OK");
                    break;
                }

            }

        } catch (SocketException socketException) {
            System.out.println("Client disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upload(DataOutputStream out, DataInputStream in) throws IOException {
        String filename = in.readUTF();
        System.out.println("upload " + filename);
        FileTransferUtils.receiveFileFromStream("server/" + filename, in);
        System.out.println("done!");
        out.writeUTF("OK");
        out.flush();
    }


    private void download(DataOutputStream out, DataInputStream in) throws IOException {
        String filename = in.readUTF();
        System.out.println("download " + filename);
        FileTransferUtils.sendFileToStream("server/" + filename, out);
        System.out.println("done!");
        out.writeUTF("OK");
        out.flush();

    }

}

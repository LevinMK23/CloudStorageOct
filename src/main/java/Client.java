import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
/**
 * Client command: upload fileName | download fileName
 *
 * @author user
 * */
public class Client extends JFrame {

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public Client() throws HeadlessException, IOException {
        socket = new Socket("localhost", 8000);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        setBounds(screenWidth / 2 - 100,screenHeight / 2 - 50,200,100);
        JPanel panel = new JPanel(new GridLayout(2, 1));
        JButton send = new JButton("SEND");
        JTextField text = new JTextField();


        send.addActionListener(a -> {
            String[] cmd = text.getText().split(" ");
            if (cmd[0].equalsIgnoreCase("upload") || cmd[0].equalsIgnoreCase("насервер")) {
                try {
                    sendFile(cmd[1]);
                } catch (IOException ioException) {
                    throw new RuntimeException("Ошибка отправки файла на сервер", ioException);
                }
            }
            if (cmd[0].equalsIgnoreCase("download") || cmd[0].equalsIgnoreCase("наклиент")) {
                try {
                    getFile(cmd[1]);
                } catch (IOException ioException) {
                    throw new RuntimeException("Ошибка загрузки файла с сервера", ioException);
                }
            }
        });

        panel.add(text);
        panel.add(send);
        add(panel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                sendMessage("exit");
            }
        });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

    }

    private void getFile(String fileName) throws IOException {
        out.writeUTF("download");
        out.writeUTF(fileName);
        FileTransferUtils.receiveFileFromStream("client/" + fileName, in);
        String status = this.in.readUTF();
        System.out.println(status);
    }

    private void sendFile(String fileName) throws IOException {
        out.writeUTF("upload");
        out.writeUTF(fileName);
        FileTransferUtils.sendFileToStream("client/" + fileName, out);
        String status = this.in.readUTF();
        System.out.println(status);
    }

    private void sendMessage(String text) {
        try {
            out.writeUTF(text);
            System.out.println(in.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}

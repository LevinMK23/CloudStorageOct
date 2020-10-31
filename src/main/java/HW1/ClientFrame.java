package HW1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class ClientFrame extends JFrame {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ClientFrame() throws HeadlessException, IOException {
        this.socket = new Socket("localhost", 1313);
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());

        setBounds(300, 200, 300,250);
        setTitle("Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(2,1));
        JButton btnSend = new JButton("SEND");
        JTextField textField = new JTextField();
        panel.add(textField);
        panel.add(btnSend);
        add(panel);


        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String [] cmd = textField.getText().split(" ");

                if (cmd[0].equals("upload")) {
                    sendFile(cmd[1]);
                }
                if (cmd[0].equals("download")) {
                    takeFile(cmd[1]);
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendMessage("exit");
            }
        });
        setVisible(true);
    }

    private void sendFile(String nameFile) {
        try {
            out.writeUTF("upload");
            out.writeUTF(nameFile);
            File file = new File("client/" + nameFile);
            long size = file.length();
            out.writeLong(size);
            FileInputStream fileByte = new FileInputStream(file);
            int read = 0;
            byte[] buf = new byte[256];
            while ((read = fileByte.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
                out.flush();
                String statusOperation = in.readUTF();
                System.out.println("Status operation - " + statusOperation);
            } catch (IOException e) {
                e.printStackTrace();
        }
    }

    private void takeFile (String nameFile) {
        try {
            out.writeUTF("download");
            out.writeUTF(nameFile);
            File file = new File("client/" + nameFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            long size = in.readLong();
            FileOutputStream fileByte = new FileOutputStream(file);
            byte[] buf = new byte[256];
            for (int i = 0; i < (size + 255)/256; i++) {
                int read = in.read(buf);
                fileByte.write(buf, 0, read);
            }
            fileByte.close();
            //out.writeUTF("OK");
            System.out.println("Status operation - OK");
        } catch (IOException e) {
            try {
                out.writeUTF("ERROR");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    private void sendMessage (String message) {
        try {
            out.writeUTF(message);
            System.out.println(in.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

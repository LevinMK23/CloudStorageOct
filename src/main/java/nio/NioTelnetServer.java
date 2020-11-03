package nio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class NioTelnetServer {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final String rootPath = "server";
    private HashMap<SocketAddress, Path> usrPath;

    public NioTelnetServer() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        usrPath = new HashMap<>();
        System.out.println("Server started!");
        while (server.isOpen()) {
            selector.select();
            var selectionKeys = selector.selectedKeys();
            var iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                }
                if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    // TODO: 30.10.2020
    //  ls - список файлов (сделано на уроке),
    //  cd (name) - перейти в папку (+)
    //  touch (name) создать текстовый файл с именем (+)
    //  mkdir (name) создать директорию (+)
    //  rm (name) удалить файл по имени (+)
    //  copy (src, target) скопировать файл из одного пути в другой (не получилось)
    //  cat (name) - вывести в консоль содержимое файла (+)

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buffer);
        if (read == -1) {
            usrPath.remove(channel.getRemoteAddress());
            channel.close();
            return;
        }
        if (read == 0) {
            return;
        }
        buffer.flip();
        byte[] buf = new byte[read];
        int pos = 0;
        while (buffer.hasRemaining()) {
            buf[pos++] = buffer.get();
        }
        buffer.clear();
        String command = new String(buf, StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "");
        if (command.equals("--help")) {
            channel.write(ByteBuffer.wrap(("1.input ls for show file list\n\r" +
                    "2.input cd for change the directory\n\r" +
                    "3.input touch for create file\n\r" +
                    "4.input mkdir for create directory\n\r" +
                    "5.input rm for delete file\n\r" +
                    "6.input cat for show the contents of the file\n\r").getBytes()));
        } else if (command.equals("ls")) {
            channel.write(ByteBuffer.wrap(getFilesList(channel.getRemoteAddress()).getBytes()));
        } else if (command.substring(0, 2).equals("cd") && command.length() >= 4) {
            channel.write(ByteBuffer.wrap(changeDirectory(channel.getRemoteAddress(), command.substring(3)).getBytes()));
        } else if (command.substring(0, 5).equals("touch") && command.length() > 6) {
            channel.write(ByteBuffer.wrap(createFile(channel.getRemoteAddress(), command.substring(6)).getBytes()));
        } else if (command.substring(0, 5).equals("mkdir") && command.length() > 6) {
            channel.write(ByteBuffer.wrap(createDir(channel.getRemoteAddress(), command.substring(6)).getBytes()));
        } else if (command.substring(0, 2).equals("rm") && command.length() >= 4) {
            channel.write(ByteBuffer.wrap(deleteFile(channel.getRemoteAddress(), command.substring(3)).getBytes()));
        }
//        else if (command.substring(0, 4).equals("copy") && command.length() > 5) {
//            channel.write(ByteBuffer.wrap(copyFile(command.substring(5)).getBytes()));
//        }
        else if (command.substring(0, 3).equals("cat") && command.length() > 4) {
            channel.write(ByteBuffer.wrap(showFile(channel.getRemoteAddress(), command.substring(4)).getBytes()));
        }


    }

    private String showFile(SocketAddress remoteAddress, String name) {
        Path p = Path.of(usrPath.get(remoteAddress).toString(), name);
        String textFile = "";
        if (Files.exists(p)) {
            try {
                BufferedReader br = Files.newBufferedReader(p);
                String line;
                while ((line = br.readLine()) != null) {
                    textFile += line + "\n\r";
                }
                return textFile + "\n\r";
            } catch (IOException e) {
                e.printStackTrace();
                return "error: " + e.getMessage() + "\n\r";
            }
        } else {
            return "file not found\n\r";
        }

    }

    private String copyFile(String command) {
        String path[] = command.split(" ");
        Path src = Path.of(path[0]);
        Path target = Path.of(path[1]);
        if (!Files.exists(src)) {
            return "file not found\n\r";
        } else {

//            if (!Files.exists(target)) {
//
//            }
        }
        return "";
    }

    private String deleteFile(SocketAddress remoteAddress, String name) {
        Path p = Path.of(usrPath.get(remoteAddress).toString(), name);
        if (!Files.exists(p)) {
            return "file not found\n\r";
        } else {
            try {
                Files.delete(p);
                return "file deleted\n\r";
            } catch (IOException e) {
                e.printStackTrace();
                return "error: " + e.getMessage() + "\n\r";
            }
        }
    }

    private String createDir(SocketAddress remoteAddress, String name) {
        Path p = Path.of(usrPath.get(remoteAddress).toString(), name);
        if (!Files.exists(p)) {
            try {
                Files.createDirectory(p);
                return "directory is created\n\r";
            } catch (IOException e) {
                e.printStackTrace();
                return "error: " + e.getMessage() + "\n\r";
            }
        } else return "directory already exists\n\r";
    }

    private String createFile(SocketAddress remoteAddress, String name) {
        Path p = Path.of(usrPath.get(remoteAddress).toString(), name);
        if (!Files.exists(p)) {
            try {
                Files.createFile(p);
                return "file is created\n\r";
            } catch (IOException e) {
                e.printStackTrace();
                return "error: " + e.getMessage() + "\n\r";
            }
        } else {
            return "file already exists\n\r";
        }
    }

    private void sendMessage(String message, Selector selector) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                ((SocketChannel) key.channel())
                        .write(ByteBuffer.wrap(message.getBytes()));
            }
        }
    }

    private String getFilesList(SocketAddress address) {
        return String.join(" ", new File(usrPath.get(address).toString()).list())+"\n\r";
    }

    private String changeDirectory(SocketAddress address, String command) {
        Path x = Path.of(usrPath.get(address).toString(), command);
        if (Files.exists(x)) {
            usrPath.put(address, x);
            return x.toString() + " ";
        } else {
            return "Path not found\n\r";
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client accepted. IP: " + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "LOL");
        if (!usrPath.containsKey(channel.getRemoteAddress())) {
            usrPath.put(channel.getRemoteAddress(), Path.of(rootPath));
        }
        channel.write(ByteBuffer.wrap("Enter --help\n\r".getBytes()));
    }

    public static void main(String[] args) throws IOException {
        new NioTelnetServer();
    }
}

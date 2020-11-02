package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class NioTelnetServer {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final String rootPath = "server";
    private HashMap<SelectableChannel, Path> usrPath = new HashMap<>();

    public NioTelnetServer() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started!");
        usrPath = new HashMap();
        while (server.isOpen()) {
            selector.select();
            var selectionKeys = selector.selectedKeys();
            var iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                    if(!usrPath.containsKey(key.channel())) {
                        usrPath.put(key.channel(), Path.of(rootPath));
                    }
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
    //  cd (name) - перейти в папку
    //  touch (name) создать текстовый файл с именем
    //  mkdir (name) создать директорию
    //  rm (name) удалить файл по имени
    //  copy (src, target) скопировать файл из одного пути в другой
    //  cat (name) - вывести в консоль содержимое файла

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buffer);
        if (read == -1) {
            usrPath.remove(key.channel());
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
        System.out.println(command);
        if (command.equals("--help")) {
            channel.write(ByteBuffer.wrap("input ls for show file list".getBytes()));
        }
        else if (command.equals("ls")) {
            System.out.println(ByteBuffer.wrap(getFilesList().getBytes()));
        }
        else if (command.length()>=2 && command.substring(0,2).equals("cd ")){
            System.out.println();
            System.out.println(changeDirectory(command.substring(3), key.channel()));
        }
        else if (command.length()>=6 && command.substring(0,5).equals("touch ")){
            System.out.println(ByteBuffer.wrap(createFile(command.substring(6), key.channel()).getBytes()));
        }
        else if (command.length()>=6 && command.substring(0,5).equals("mkdir ")) {
            System.out.println(ByteBuffer.wrap(createDir(command.substring(6), key.channel()).getBytes()));
        }
        else if (command.length()>=3 && command.substring(0,2).equals("rm ")) {
            System.out.println(ByteBuffer.wrap(deleteFile(command.substring(3), key.channel()).getBytes()));
        }
        else{
            System.out.println(ByteBuffer.wrap("command not found".getBytes()));
        }
    }

    private void sendMessage(String message, Selector selector) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                ((SocketChannel)key.channel())
                        .write(ByteBuffer.wrap(message.getBytes()));
            }
        }
    }

    private String getFilesList() {
        return String.join(" ", new File(rootPath).list());
    }

    private String changeDirectory(String userMsg, SelectableChannel key) {
        Path x = Path.of(usrPath.get(key).toString(), userMsg);
        if (Files.exists(x)){
            usrPath.put(key, x);
            return x.toString();
        } else {
            return "Path not found";
        }
    }

    private String createFile(String fileName, SelectableChannel key){
        Path x = Path.of(usrPath.get(key).toString(), fileName);
        if (!Files.exists(x)){
            try {
                Files.createFile(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "file is created";
        } else {
            return "file already created";
        }
    }

    private String createDir(String userMsg, SelectableChannel key){
        Path x = Path.of(usrPath.get(key).toString(), userMsg);
        if (!Files.exists(x)){
            try {
                Files.createDirectory(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "directory is created";
        } else {
            return "directory already created";
        }
    }

    private String deleteFile(String fileName, SelectableChannel key){
        Path x = Path.of(usrPath.get(key).toString(), fileName);
        if (!Files.exists(x)){
            try {
                Files.delete(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "file is deleted";
        } else {
            return "file already deleted";
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client accepted. IP: " + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "LOL");
        channel.write(ByteBuffer.wrap("Enter --help".getBytes()));
    }

    public static void main(String[] args) throws IOException {
        new NioTelnetServer();
    }
}

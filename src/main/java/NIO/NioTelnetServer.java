package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioTelnetServer {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final String rootPath = "server";

    public NioTelnetServer() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
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

    
    
    // TODO: 30.10.2020 list (ls) - возвращает список файлов
    private String getFilesList() {
        return String.join(" ", new File(rootPath).list());
    }

    //  TODO: 30.10.2020 copy (src, target) скопировать файл из одного в другой
    static void copy(Path src, Path target) throws IOException {
       Files.copy((Paths.get(path.getParent().toString(), src)),
               (Paths.get(path.getParent().toString(), target),
               StandardCopyOption.REPLACE_EXISTING);
    }

    // дописать в конец фаула
    static void appendText(Path src, String s) throws IOException {
    Path src = Path.of(usrPath.get(key).toString(), s);
    Files.write(Paths.get(src), s.getBytes(), StandardOpenOption.APPEND);
    }

    // TODO: 30.10.2020 создать файл
    static void createFile(String fileName) throws IOException {
        Path path = Path.of(usrPath.get(key).toString(), fileName);
        if (!Files.exists(path)) {
            try {
            Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: 30.10.2020 rm (name) удалить файл по имени
    private void deleteFile(String fileName, SelectableChannel key){
        Path path = Path.of(usrPath.get(key).toString(), fileName);
        if (!Files.exists(path)){
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
    }


    //  TODO: 30.10.2020 mkdir (name) создать директорию
    private void createDirectory(String directory, SelectableChannel key){
        Path path = Path.of(usrPath.get(key).toString(), directory);
        if (!Files.exists(path)){
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buffer);
        if (read == -1) {
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
        if (command.equals("ls")) {
            channel.write(ByteBuffer.wrap(getFilesList().getBytes()));
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

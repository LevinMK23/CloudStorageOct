package nio;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;


public class NioTelnetServer {
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private Path rootPath = Path.of("C:/");
    private SocketChannel channel;

    public NioTelnetServer() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server start");
        while (server.isOpen()) {
            selector.select();
            var selectionKeys = selector.selectedKeys();
            var iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        channel = (SocketChannel) key.channel();
        int read = channel.read(buffer);
        if (read == -1) {
            channel.close();
            return;
        }
        if (read == 0) {
            return;
        }
        buffer.flip();
        int pos = 0;
        byte[] buf = new byte[read];
        while (buffer.hasRemaining()) {
            buf[pos++] = buffer.get();
        }
        buffer.clear();

        String command = new String(buf, StandardCharsets.UTF_8).
                replace("\n", "").replace("\r", "");
        System.out.println(command);
        if (command.equals("--help")) {
            channel.write(ByteBuffer.wrap(("input --ls for show file list; \n\r" +
                    "--cd (name) going to the folder; \n\r" +
                    "--touch (name) creat txt file; \n\r" +
                    "--mkdir (name) creat directory; \n\r" +
                    "--rm (name) remove file; \n\r" +
                    "--copy (name , directory) copy file from directory to another; \n\r" +
                    "--cat (name) print the contents of a file to the console.\n\r").getBytes()));
        }
        if (command.equals("--ls")) {
            String files = getFilesList();
            channel.write(ByteBuffer.wrap(getFilesList().getBytes()));
            channel.write(ByteBuffer.wrap(("\n\r" + rootPath.toAbsolutePath().toString() + ">").getBytes()));
        }
        if (command.startsWith("--cd")) {
            String[] str = command.split(" ");
            if (validComand(str)) {
                moveFolder(str[1]);
            } else {
                invalidCommand();
            }
        }
        if (command.startsWith("--touch")) {
            String[] str = command.split(" ");
            if (validComand(str)) {
                if (str[1].contains(".txt")) {
                    createFile("/" + str[1]);
                    channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
                } else {
                    createFile("/" + str[1] + ".txt");
                    channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
                }
            } else {
                invalidCommand();
            }
        }
        if (command.startsWith("--mkdir")) {
            String[] str = command.split(" ");
            if (validComand(str)) {
                createDir("/" + str[1]);
                channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
            } else {
                invalidCommand();
            }
        }
        if (command.startsWith("--rm")) {
            String[] str = command.split(" ");
            if (validComand(str)) {
                removeFile("/" + str[1]);
                channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
            } else {
                invalidCommand();
            }
        }
        if (command.startsWith("--copy")) {
            String[] str = command.split(" ");
            if (str.length > 2) {
                try {
                    copyFile(str);
                } catch (InvalidPathException e) {
                    invalidCommand();
                }
            } else {
                invalidCommand();
            }
        }
        if (command.startsWith("--cat")) {
            String[] str = command.split(" ");
            if (validComand(str)) {
                readFile(str);
            } else {
                invalidCommand();
            }
        }
    }

    private void readFile(String[] str) throws IOException {
        if (Files.exists(Path.of(rootPath.toString() + "/" + str[1]))) {
            BufferedReader buf = null;
            try {
                String s;
                buf = new BufferedReader(new FileReader(rootPath.toString() + "/" + str[1]));
                while ((s = buf.readLine()) != null) {
                    channel.write(ByteBuffer.wrap(s.getBytes()));
                }
                channel.write(ByteBuffer.wrap(("\n\r" + rootPath.toAbsolutePath().toString() + ">").getBytes()));
            } finally {
                try {
                    if (buf != null)
                        buf.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            channel.write(ByteBuffer.wrap(("File " + str[1] + " not detected" + "\n\r").getBytes()));
        }
    }

    private void copyFile(String[] str) throws IOException {
        if (Files.isDirectory(Path.of(str[2]))) {
            Path toPath = Path.of(str[2] + "/" + str[1]);
            if (Files.exists(Path.of(rootPath.toString() + "/" + str[1]))) {
                if (!Files.exists(toPath)) {
                    Path fromPath = Path.of(rootPath.toString() + "/" + str[1]);
                    Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
                    channel.write(ByteBuffer.wrap(("File " + str[1] + " copy success" + "\n\r").getBytes()));
                } else {
                    channel.write(ByteBuffer.wrap(("File " + str[1] + " already exist" + "\n\r").getBytes()));
                }
            } else {
                channel.write(ByteBuffer.wrap(("File " + str[1] + " not detected" + "\n\r").getBytes()));
            }
            channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
        } else {
            channel.write(ByteBuffer.wrap(("Directory " + str[2] + " not detected" + "\n\r").getBytes()));
        }
    }

    private void removeFile(String s) throws IOException {
        Path newPath = Path.of(rootPath.toString() + s);
        if (Files.exists(newPath)) {
            Files.delete(newPath);
            channel.write(ByteBuffer.wrap(("File " + s.replace("/", "") + " delete" + "\n\r").getBytes()));
        } else {
            channel.write(ByteBuffer.wrap(("File " + s.replace("/", "") + " not detected" + "\n\r").getBytes()));
        }
    }

    private void invalidCommand() throws IOException {
        channel.write(ByteBuffer.wrap(("Wrong command" + "\n\r").getBytes()));
        channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
    }

    private boolean validComand(String[] s) {
        return s.length > 1;
    }

    private void createDir(String s) throws IOException {
        Path newPath = Path.of(rootPath.toString() + s);
        if (!Files.isDirectory(newPath)) {
            Files.createDirectory(newPath);
            channel.write(ByteBuffer.wrap(("Directory " + s.replace("/", "") + " created" + "\n\r").getBytes()));
        } else {
            channel.write(ByteBuffer.wrap(("Directory " + s.replace("/", "") + " already exist" + "\n\r").getBytes()));
        }
    }

    private void moveFolder(String s) throws IOException {
        Path p;
        if (s.contains(":") && !s.equals("C:")) {
            try {
                if (Files.isDirectory(Path.of(s.replace("\\", "/")))) {
                    rootPath = Path.of(s.replace("\\", "/"));
                } else {
                    channel.write(ByteBuffer.wrap(("The system cannot find a way." + "\n\r").getBytes()));
                }
            } catch (InvalidPathException | IOException pathException) {
                channel.write(ByteBuffer.wrap(("Wrong way. Use '\\'" + "\n\r").getBytes()));
            }
            p = rootPath;
        } else {
            try {
                p = Path.of(rootPath.toString() + "/" + s);
            } catch (InvalidPathException pathException) {
                channel.write(ByteBuffer.wrap(("Wrong path" + "\n\r").getBytes()));
                p = rootPath;
            }
        }
        if (s.equals("..\\")) {
            try {
                rootPath = Path.of(rootPath.getParent().toString());
            } catch (Exception e) {
            }
            channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
        } else if (Files.isDirectory(Path.of(p.toString()))) {
            rootPath = Path.of(p.toString());
            channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
        } else {
            channel.write(ByteBuffer.wrap(("The system cannot find a way." + "\n\r").getBytes()));
            channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
        }
    }

    private void createFile(String s) throws IOException {
        Path newPath = Path.of(rootPath.toString() + s);
        if (!Files.exists(newPath)) {
            Files.createFile(newPath);
            channel.write(ByteBuffer.wrap(("File " + s.replace("/", "") + " created" + "\n\r").getBytes()));
        } else {
            channel.write(ByteBuffer.wrap(("File " + s.replace("/", "") + " already exist" + "\n\r").getBytes()));
        }
    }

    private String getFilesList() {
        return String.join("\n\r", new File(String.valueOf(rootPath)).list());
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client accepted with IP: " + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "user");
        channel.write(ByteBuffer.wrap("Enter --help\n\r".getBytes()));
        channel.write(ByteBuffer.wrap((rootPath.toAbsolutePath().toString() + ">").getBytes()));
    }

    public static void main(String[] args) throws IOException {
        new NioTelnetServer();
    }
}
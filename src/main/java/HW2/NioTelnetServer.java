package HW2;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class  NioTelnetServer {
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private String rootPath = "server";
    private Path path;

    public NioTelnetServer() throws IOException {
        ServerSocketChannel ser = ServerSocketChannel.open();
        ser.bind(new InetSocketAddress(1313));
        ser.configureBlocking(false);

        Selector sel = Selector.open();
        ser.register(sel, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");

        while (ser.isOpen()) {
            sel.select();
            var selKeys = sel.selectedKeys();
            var iterator = selKeys.iterator();

            while (iterator.hasNext()) {
                var key = iterator.next();

                if (key.isAcceptable()) {
                    handleAccept(key, sel);
                }

                if (key.isReadable()) {
                    handleRead(key, sel);
                }
                iterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector sel) throws IOException {
        SocketChannel channel = ((ServerSocketChannel)key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client accepted: " + channel.getRemoteAddress());
        channel.register(sel, SelectionKey.OP_READ, "User");

        channel.write((ByteBuffer.wrap("Welcome!\nEnter --help".getBytes())));
    }

    private void handleRead(SelectionKey key, Selector sel) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        int read = channel.read(buffer);
        if (read == -1) {
            channel.close();
            return;
        }
        if (read == 0) { //??? Что значит что read = 0??? -1 понятно, значит больше ничего нет, а 0?
            return;
        }
        buffer.flip();
        String command = readCMD (buffer, read);
        System.out.println(command);
        buffer.clear();

        if (command.equals("--help")) {
            path = Path.of("server/help.txt");
            channel.write(ByteBuffer.wrap(readFile(path).getBytes()));
        }

        if (command.equals("ls")) {
            /*Files.walkFileTree(Path.of(rootPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    channel.write(ByteBuffer.wrap(String.valueOf(file.getFileName()).getBytes()));
                    return super.visitFile(file, attrs);
                }
            });*/
            channel.write(ByteBuffer.wrap(getFilesList().getBytes()));


        }

        if (command.startsWith("cat ")) {
            path = Path.of(command.substring(4));
            if (Files.exists(path)) {
                channel.write(ByteBuffer.wrap(readFile(path).getBytes()));
            } else {
                channel.write(ByteBuffer.wrap("File does not exist".getBytes()));
            }
        }

        if (command.startsWith("cd ")) {
            String tempPath = command.substring(3);
            if (Files.exists(Path.of(tempPath))) {
                rootPath = tempPath;
            } else {
                channel.write(ByteBuffer.wrap("Directory does not exist".getBytes()));
            }
        }

        if (command.startsWith("touch ")) {
            channel.write(ByteBuffer.wrap(createFile(command).getBytes()));
        }

         if (command.startsWith("mkdir ")) {
             channel.write(ByteBuffer.wrap(createDir(command).getBytes()));
         }

         if (command.startsWith("rm ")) {
             channel.write(ByteBuffer.wrap(deleteFile(command).getBytes()));
         }

         if (command.startsWith("copy ")) {
             // принцип понятен как делать
         }

        channel.write(ByteBuffer.wrap((rootPath + ">").getBytes()));

    }

    private String readCMD(ByteBuffer buffer, int read) {
        byte[] buf = new byte[read];
        int i = 0;
        while (buffer.hasRemaining()) {
            buf[i++] = buffer.get();
        }
        String command = new String(buf, StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r","");
        return command;
    }

    private static String printBuf(ByteBuffer byteBuffer, int size) {
        int i = 0;
        byte [] buf = new byte[size];
        while (byteBuffer.hasRemaining()) {
            buf[i++] =   byteBuffer.get();
        }
        String text = new String(buf, StandardCharsets.UTF_8);
        return text;
    }

    private static String readFile(Path path) throws IOException {
        FileChannel channelR = new RandomAccessFile(String.valueOf(path), "rw").getChannel();
        int size = (int) channelR.size();
        ByteBuffer bufR = ByteBuffer.allocate(size);
        channelR.read(bufR);
        bufR.flip();
        int i = 0;
        byte [] buf = new byte[size];
        while (bufR.hasRemaining()) {
            buf[i++] =   bufR.get();
        }
        String text = new String(buf, StandardCharsets.UTF_8);
        return text;
    }

    private String getFilesList() {
        return String.join(" ", new File(rootPath).list());
    }

    private String createFile(String command) throws IOException {
        Path path = Path.of(rootPath + "/" + command.substring(6));
        if (!Files.exists(path)) {
            Files.createFile(path);
            return "File created ";
        } else {
            return "File already exists ";
        }
    }

    private String createDir(String command) throws IOException {
        Path path = Path.of(rootPath + "/" + command.substring(6));
        if (!Files.exists(path)) {
            Files.createDirectory(path);
            return "Directory created ";
        } else {
            return "Directory  already exists ";
        }
    }

    private String deleteFile(String command) throws IOException {
        Path path = Path.of(rootPath + "/" + command.substring(3));
        if (Files.deleteIfExists(path)) {
            return "File deleted ";
        } else {
            return "File  dose not exists ";
        }
    }


}

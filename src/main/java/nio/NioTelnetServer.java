package nio;

import nio.telnet.*;
import nio.telnet.functions.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class NioTelnetServer {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final String rootPath = "client";

    private ConsoleFunctionExecutor functionExecutor = new ConsoleFunctionExecutor();


    public NioTelnetServer() throws IOException {

        init();

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

    // TODO: 30.10.2020
    //  cat (name) - вывести в консоль содержимое файла

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

        ConsoleFunctionResultValue resultValue = functionExecutor.execute(command);

        sendString(channel, resultValue.result);

    }

    private void sendString(SocketChannel channel, String message) throws IOException {
        channel.write(ByteBuffer.wrap((message + "\n\r").getBytes()));
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
        channel.write(ByteBuffer.wrap("Enter --help\n\r".getBytes()));
    }

    public static void main(String[] args) throws IOException {
        new NioTelnetServer();
    }

    private void init(){
        Path root = Path.of("client");
        functionExecutor.setFunctionExecuteContent(new FunctionExecuteContent(root));

        functionExecutor.addFunction(new ConsoleFunctionLS());
        functionExecutor.addFunction(new ConsoleFunctionCD());
        functionExecutor.addFunction(new ConsoleFunctionMkDir());
        functionExecutor.addFunction(new ConsoleFunctionRm());
        functionExecutor.addFunction(new ConsoleFunctionCopy());
        functionExecutor.addFunction(new ConsoleFunctionTouch());
        functionExecutor.addFunction(new ConsoleFunctionCat());
    }
}

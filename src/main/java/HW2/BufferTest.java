package HW2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class BufferTest {
    public static void main(String[] args) {

        //int size = 10;

        Path path = Path.of("server/13.txt");
        try {
            FileChannel channel = new RandomAccessFile(String.valueOf(path), "rw").getChannel();
            int size = (int) channel.size();
            ByteBuffer buf = ByteBuffer.allocate(size);
            channel.read(buf);
            buf.flip();
            printBuf(buf,size);
            System.out.println(channel.size());

            // учился записывать в файл из буфера
            buf.clear();
            buf.put("Yes".getBytes());
            buf.flip();
            printBuf(buf,size);
            buf.rewind();
            channel.write(buf);
            buf.flip();
            printBuf(buf,size);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printBuf(ByteBuffer byteBuffer, int size) {
        int i = 0;
        byte [] buf = new byte[size];
        while (byteBuffer.hasRemaining()) {
            buf[i++] = byteBuffer.get();
        }
        String text = new String(buf, StandardCharsets.UTF_8);
        System.out.println(text);
    }
}

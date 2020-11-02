import java.io.*;

public class FileTransferUtils {

    static public int BUFFER_SIZE = 65536;


    /**
     * Отправка файла с диска в поток
     * Первый long потока - размер отправляемого файла
     * @param sourceFilePath путь к файлу
     * @param out выходной поток
     * @return размер отправленного файла
     * @throws IOException
     */
    public static long sendFileToStream(String sourceFilePath, DataOutputStream out) throws IOException {
        System.out.println("-->sendFileToStream");
        File file = new File(sourceFilePath);
        out.writeLong(file.length());

        try(
                FileInputStream inFile = new FileInputStream(file);
        ) {
            byte[] byteBuffer = new byte[BUFFER_SIZE];
            int read = 0;

            while ((read = inFile.read(byteBuffer)) != -1) {
                out.write(byteBuffer, 0, read);
            }
            out.flush();
        }
        System.out.println("<--sendFileToStream");
        return file.length();
    }

    /**
     * Загрузка файла из потока на диск
     * Первый long потока - размер принимаемого файла
     * @param destinationFilePath путь к файлу
     * @param in входной поток
     * @return размер принятого файла
     * @throws IOException
     */
    public static long receiveFileFromStream(String destinationFilePath, DataInputStream in) throws IOException {
        System.out.println("-->receiveFileFromStream");
        long fileSize = in.readLong();

        File file = new File(destinationFilePath);
        if (!file.exists()) file.createNewFile();

        try(
                FileOutputStream outFile = new FileOutputStream(file)
        ){
            byte[] byteBuffer = new byte[BUFFER_SIZE];
            int read = 0;
            long readTotal = 0;

            while (readTotal != fileSize){
                read = in.read(byteBuffer);
                outFile.write(byteBuffer, 0, read);
                readTotal += read;
            }
            outFile.flush();
        }

        System.out.println("<--receiveFileFromStream");
        return fileSize;
    }

}

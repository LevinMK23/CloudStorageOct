package HW2;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;

public class nioUtilsTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path path = Path.of("client");
        System.out.println(path);
        System.out.println(path.toAbsolutePath());

        //System.out.println(path.toAbsolutePath().iterator().forEachRemaining());
        // Эта операция требцет какой- аргумент Consumer...

        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        //// не понял как работает watch service -  у меня ничего не происходит

        Files.newBufferedReader(Path.of("client/13.txt"))
                .lines()
                .forEach(System.out ::println);

        // создание файла
        Path p1 = Path.of("client", "dir1", "dir3", "5.txt");
        if (!Files.exists(p1)) {
            Files.createFile(p1);
        }

        //копирование файла
        Files.copy(Path.of("client/13.txt"), p1, StandardCopyOption.REPLACE_EXISTING);

        //запись в файл
        Files.write(p1, "\nSuper".getBytes(),StandardOpenOption.APPEND);

        // поиск файла
        Files.walkFileTree(path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file,
                                   BasicFileAttributes attrs) throws IOException {
                        System.out.println(file.toAbsolutePath());
                        return super.visitFile(file, attrs);
                    }
                });

        String fileToFind = "3.txt";
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file,
                                    BasicFileAttributes attrs) throws IOException {
                        String fileStr = file.toAbsolutePath().toString();
                        if (fileStr.endsWith(fileToFind)) {
                            System.out.println("file found at path: " + fileStr);
                            //return FileVisitResult.TERMINATE;
                        }
                        //return super.visitFile(file, attrs);
                        return  FileVisitResult.CONTINUE;
                    }
                }
        );

        Files.find(path, 100, new BiPredicate<Path, BasicFileAttributes>() {
            @Override
            public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                return path.getFileName().toString().equals("3.txt");
            }
        }).forEach(System.out :: println);

        // Продолжение уотчсервиса - вроде работает теперь
        while (true) {
            var key = watchService.take();
            if (key.isValid()) {
                var events = key.pollEvents();
                for (WatchEvent<?> event: events) {
                    System.out.println(event.kind() + " " + event.context());
                }
                key.reset();
            }
        }
    }
}

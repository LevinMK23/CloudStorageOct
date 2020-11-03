package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConsoleFunctionCopy implements ConsoleFunction{
    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public String getDescription() {
        return "copy file from source to destination. root relative. first argument - file name with path, second - destination folder or ':' to root";
    }

    @Override
    public boolean checkArgs(String... functionAndArguments) {
        if (functionAndArguments.length != 3) return false;
        if (functionAndArguments[1].isBlank()) return false;

        return true;
    }

    @Override
    public ConsoleFunctionResultValue execute(FunctionExecuteContent content, String... functionAndArguments) {

        Path sourcePath = Path.of(content.rootPath.toString(), functionAndArguments[1]);
        Path destinationPath;

        //первый путь - путь к файлу, второй - к директории
        if (functionAndArguments[2].equals(":")){
            destinationPath = content.rootPath;
        } else {
            destinationPath = Path.of(content.rootPath.toString(), functionAndArguments[2]);
        }

        //Проверяем, является ли первый путь - файлом
        if (!Files.isRegularFile(sourcePath)) {
            return new ConsoleFunctionResultValue(false, String.format("file '%s' is not exists", functionAndArguments[1]));
        }

        //Проверяем, является ли второй путь - директорией
        if (!Files.isDirectory(destinationPath)) {
            return new ConsoleFunctionResultValue(false, String.format("directory '%s' is not exists", functionAndArguments[2]));
        }

        System.out.println(sourcePath.toString());
        System.out.println(destinationPath.toString());

        try {
            Files.copy(sourcePath, destinationPath.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            //Прокидываем рантайм выше
            throw new RuntimeException(String.format("Cannot copy file '%s' to directory '%s'",functionAndArguments[1], functionAndArguments[2]), ioException);
        }

        return new ConsoleFunctionResultValue(true, "OK");

    }
}

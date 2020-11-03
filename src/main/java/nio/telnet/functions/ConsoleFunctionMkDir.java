package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleFunctionMkDir implements ConsoleFunction{

    @Override
    public String getName() {
        return "mkdir";
    }

    @Override
    public String getDescription() {
        return "Create new directory. Relative to current path";
    }

    @Override
    public boolean checkArgs(String... functionAndArguments) {
        if (functionAndArguments.length != 2) return false;
        if (functionAndArguments[1].isBlank()) return false;
        if (functionAndArguments[1].contains(".")) return false;
        if (functionAndArguments[1].contains("\\")) return false;
        if (functionAndArguments[1].contains("/")) return false;

        return true;
    }

    @Override
    public ConsoleFunctionResultValue execute(FunctionExecuteContent content, String... functionAndArguments) {

        Path path = Path.of(content.currentPath.toString(), functionAndArguments[1]);

        //Проверяем, существует ли директория
        if (Files.isDirectory(path)) {
            return new ConsoleFunctionResultValue(false, String.format("directory %s is already exists", functionAndArguments[1]));
        }

        if (Files.isRegularFile(path)) {
            return new ConsoleFunctionResultValue(false, String.format("there is the file %s this the same name", functionAndArguments[1]));
        }

        try {
            Files.createDirectory(path);
        } catch (IOException ioException) {
            //Прокидываем рантайм выше
            throw new RuntimeException(String.format("Cannot create directory %s",functionAndArguments[1]), ioException);
        }

        return new ConsoleFunctionResultValue(true, "OK");

    }
}

package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleFunctionTouch implements ConsoleFunction{
    @Override
    public String getName() {
        return "touch";
    }

    @Override
    public String getDescription() {
        return "Create new file. Relative to current path";
    }

    @Override
    public boolean checkArgs(String... functionAndArguments) {
        if (functionAndArguments.length != 2) return false;
        if (functionAndArguments[1].isBlank()) return false;
        if (functionAndArguments[1].contains("\\")) return false;
        if (functionAndArguments[1].contains("/")) return false;

        return true;
    }

    @Override
    public ConsoleFunctionResultValue execute(FunctionExecuteContent content, String... functionAndArguments) {

        Path path = Path.of(content.currentPath.toString(), functionAndArguments[1]);

        //Проверяем, существует ли файл
        if (Files.isRegularFile(path)) {
            return new ConsoleFunctionResultValue(false, String.format("there is the file %s this the same name", functionAndArguments[1]));
        }

        try {
            Files.createFile(path);
        } catch (IOException ioException) {
            //Прокидываем рантайм выше
            throw new RuntimeException(String.format("Cannot create file %s",functionAndArguments[1]), ioException);
        }

        return new ConsoleFunctionResultValue(true, "OK");

    }
}

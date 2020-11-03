package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleFunctionRm implements ConsoleFunction{
    @Override
    public String getName() {
        return "rm";
    }

    @Override
    public String getDescription() {
        return "Delete file or empty directory. Relative to current path";
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

        Path path;
        path = Path.of(content.currentPath.toString(), functionAndArguments[1]);


        //Проверяем, существует ли директория или файл

        if (!(Files.isDirectory(path) || Files.isRegularFile(path))) {
            return new ConsoleFunctionResultValue(false, String.format("there is no the file or directory named '%s'", functionAndArguments[1]));
        }

        try {

            //Проверяем, что файл - директория, и что она пуста
            //Пришлось использовать File вместо Files.list(), т.к. там Stream и он почему-то не закрывался по
            //команде Files.list(path).findFirst() из-за чего не удавалось удалять файл

            if (Files.isDirectory(path) && new File(path.toUri()).list().length != 0) {
                    return new ConsoleFunctionResultValue(false, String.format("directory '%s' is not empty", functionAndArguments[1]));
            }

            Files.delete(path);


        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return new ConsoleFunctionResultValue(true, "OK");

    }


}

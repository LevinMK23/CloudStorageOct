package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleFunctionCD implements ConsoleFunction{
    @Override
    public String getName() {
        return "cd";
    }

    @Override
    public String getDescription() {
        return "change directory. Type '.' to move up, type ':' to move root";
    }

    @Override
    public boolean checkArgs(String... functionAndArguments) {
        if (functionAndArguments.length != 2) return false;
        if (functionAndArguments[1].isBlank()) return false;
        if (functionAndArguments[1].contains(".") && functionAndArguments[1].length() > 1) return false;

        return true;
    }

    @Override
    public ConsoleFunctionResultValue execute(FunctionExecuteContent content, String... functionAndArguments) {

        //Переход в root
        if (functionAndArguments[1].equals(":")){
            content.currentPath = content.rootPath;
            return new ConsoleFunctionResultValue(true,"OK");
        }

        //Переход в директорию выше
        if (functionAndArguments[1].equals(".")){
            if (content.currentPath.equals(content.rootPath)){
                return new ConsoleFunctionResultValue(false, "Can`t go upper than root directory!");
            } else {
                content.currentPath = content.currentPath.getParent();
                return new ConsoleFunctionResultValue(true, content.currentPath.toString());
            }
        }

        //Переход в директорию ниже

        //Проверяем, существует ли директория
        if (Files.isDirectory(Path.of(content.currentPath.toString(), functionAndArguments[1]))) {
            content.currentPath = Path.of(content.currentPath.toString(), functionAndArguments[1]);
            return new ConsoleFunctionResultValue(true, content.currentPath.toString());
        }

        return new ConsoleFunctionResultValue(false, String.format("directory %s is not exists", functionAndArguments[1]));

    }
}

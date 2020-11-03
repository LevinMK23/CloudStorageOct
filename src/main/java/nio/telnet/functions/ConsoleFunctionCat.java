package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleFunctionCat implements ConsoleFunction{
    @Override
    public String getName() {
        return "cat";
    }

    @Override
    public String getDescription() {
        return "Print file content to console. Relative to current path. Type relative filename to open file";
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

        //Проверяем, является ли путь - файлом
        if (!Files.isRegularFile(path)) {
            return new ConsoleFunctionResultValue(false, String.format("file '%s' is not exists", functionAndArguments[1]));
        }

        //todo надо сделать возврат значений из функций не в виде строк, а в виде списка или в виде потока
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(path))
        {
            //result = reader.lines().collect(Collectors.joining("\r\n"));
            reader.lines().forEachOrdered(s-> {
                builder.append(s);
                builder.append("\r\n");
            });

        } catch (IOException ioException) {
            throw new RuntimeException(String.format("Cannot read file %", functionAndArguments[1]), ioException);
        }

        builder.append("OK");

        return new ConsoleFunctionResultValue(true, builder.toString());

    }
}

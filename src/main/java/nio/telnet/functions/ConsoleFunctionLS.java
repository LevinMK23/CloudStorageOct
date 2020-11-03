package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

import java.io.File;

public class ConsoleFunctionLS implements ConsoleFunction{
    @Override
    public String getName() {
        return "ls";
    }

    @Override
    public String getDescription() {
        return "show file and directory list";
    }

    @Override
    public boolean checkArgs(String... functionAndArguments) {
        return true;
    }

    @Override
    public ConsoleFunctionResultValue execute(FunctionExecuteContent content, String... functionAndArguments) {
        return new ConsoleFunctionResultValue(true, String.join("\n\r", new File(content.currentPath.toUri()).list()));
    }
}

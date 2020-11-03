package nio.telnet.functions;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;

public interface ConsoleFunction {
    String getName();
    String getDescription();
    boolean checkArgs(String... functionAndArguments);
    ConsoleFunctionResultValue execute(FunctionExecuteContent content, String... functionAndArguments);
}

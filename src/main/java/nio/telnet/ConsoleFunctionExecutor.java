package nio.telnet;

import nio.telnet.ConsoleFunctionResultValue;
import nio.telnet.FunctionExecuteContent;
import nio.telnet.functions.ConsoleFunction;

import java.util.ArrayList;
import java.util.List;

public class ConsoleFunctionExecutor {

    private final String BLANK_COMMAND = "Command is blank!";
    private final String NO_FUNCTION_FOUND = "There is no function named '%s'";
    private final String INCORRECT_ARGUMENTS = "Incorrect arguments to function '%s'";

    private List<ConsoleFunction> functionList = new ArrayList<>();
    private FunctionExecuteContent functionExecuteContent;

    /**
     * Добавление функции в пул
     */
    public void addFunction(ConsoleFunction function){
        functionList.add(function);
    }

    public void setFunctionExecuteContent(FunctionExecuteContent functionExecuteContent) {
        this.functionExecuteContent = functionExecuteContent;
    }

    /**
     * Запуск функции
     */
    public ConsoleFunctionResultValue execute(String commandToExecute){

        if (commandToExecute.isBlank()) {
            return new ConsoleFunctionResultValue(false, BLANK_COMMAND);
        }

        String[] parsed = commandToExecute.split(" ");

        if (parsed[0].equalsIgnoreCase("--help")){
            return new ConsoleFunctionResultValue(true, help());
        }

        for (ConsoleFunction f: functionList) {

            if (!f.getName().equalsIgnoreCase(parsed[0])) {
                continue;
            }

            if (!f.checkArgs(parsed)) {
                return new ConsoleFunctionResultValue(false, String.format(INCORRECT_ARGUMENTS, parsed[0]));
            }

            return f.execute(functionExecuteContent,
                    parsed);

        }

        return new ConsoleFunctionResultValue(false, String.format(NO_FUNCTION_FOUND, parsed[0]));

    }

    //отдельная команда
    private String help(){
        StringBuilder builder = new StringBuilder();

        for (ConsoleFunction f: functionList ) {
            builder.append(f.getName());
            builder.append("\t\t");
            builder.append(f.getDescription());
            builder.append("\r\n");
        }

        return builder.toString();
    }

}

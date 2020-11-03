package nio.functions;

import java.util.ArrayList;
import java.util.List;

public class ConsoleFunctionExecutor {

    private final String BLANK_COMMAND = "Command is blank!";
    private final String NO_FUNCTION_FOUND = "There is no function named %s";
    private final String INCORRECT_ARGUMENTS = "Incorrect arguments to function %s";
    private final String UNKNOWN_ERROR = "Unknown error while executing %s";

    List<ConsoleFunction> functionList = new ArrayList<>();


    /**
     * Добавление функции в пул
     */
    public void addFunction(ConsoleFunction function){
        functionList.add(function);
    }


    /**
     * Запуск функции
     */
    public ConsoleFunctionResultValue execute(String commandToExecute){

        if (commandToExecute.isBlank()) {
            return new ConsoleFunctionResultValue(false, BLANK_COMMAND);
        }

        String[] parsed = commandToExecute.split(" ");

        for (ConsoleFunction f: functionList) {


            if (!f.getName().equalsIgnoreCase(parsed[0])) {
                return new ConsoleFunctionResultValue(false, String.format(NO_FUNCTION_FOUND, parsed[0]));
            }

            if (!f.checkArgs(parsed)) {
                return new ConsoleFunctionResultValue(false, String.format(INCORRECT_ARGUMENTS, parsed[0]));
            }

            return f.execute(parsed);

        }

        return new ConsoleFunctionResultValue(false, String.format(UNKNOWN_ERROR, parsed[0]));

    }

}

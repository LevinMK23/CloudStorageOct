package nio.functions;

public interface ConsoleFunction {
    String getName();
    String getDescription();
    boolean checkArgs(String... functionAndArguments);
    ConsoleFunctionResultValue execute(String... functionAndArguments);
}

package nio.functions;

public class HelpCF implements ConsoleFunction{
    @Override
    public String getName() {
        return "--help";
    }

    @Override
    public String getDescription() {
        return "Type --help to get help";
    }

    @Override
    public boolean checkArgs(String... functionAndArguments) {
        //no args
        return true;
    }

    @Override
    public ConsoleFunctionResultValue execute(String... functionAndArguments) {
        String resultMessage = "ls to view file list\n\r";
        return new ConsoleFunctionResultValue(true, resultMessage);
    }
}

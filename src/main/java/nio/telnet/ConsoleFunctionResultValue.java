package nio.telnet;

public class ConsoleFunctionResultValue {
    public boolean isDone;
    public String result;

    public ConsoleFunctionResultValue() {
    }

    public ConsoleFunctionResultValue(boolean isDone, String result) {
        this.isDone = isDone;
        this.result = result;
    }

    @Override
    public String toString() {
        return result;
    }
}

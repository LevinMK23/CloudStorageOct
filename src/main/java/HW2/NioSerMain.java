package HW2;

import java.io.IOException;

public class NioSerMain {
    public static void main(String[] args) {
        try {
            new NioTelnetServer();
        } catch (IOException e) {

        }
    }
}

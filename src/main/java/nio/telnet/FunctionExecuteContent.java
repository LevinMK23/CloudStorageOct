package nio.telnet;

import java.nio.file.Path;

public class FunctionExecuteContent {
    public Path rootPath;
    public Path currentPath;


    public FunctionExecuteContent(Path rootPath) {
        this.rootPath = rootPath;
        this.currentPath = rootPath;
    }
}

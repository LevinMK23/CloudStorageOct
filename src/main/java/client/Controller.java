package client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private Socket socket;
    private DataOutputStream dos;
    private final String IP_ADRESS = "localhost";
    private final int PORT = 8189;
    private DataInputStream dis;
    private Path rootPathUser = Path.of("C:/");
    private Path rootPathServer = Path.of("C:/GeekBrainsJava/CloudStorage/server");
    private String s;
    private final byte COUNT = 5;
    @FXML
    private TextField textFieldServer;
    @FXML
    private TextField textFieldUser;
    @FXML
    private TextField textFieldFile;
    @FXML
    private TextArea textAreaServer;
    @FXML
    private TextArea helpArea;
    @FXML
    private TextArea textAreaUser;
    @FXML
    private TextField messageField;
    @FXML
    private TextField messageFieldcloud;
    @FXML
    private Button messageButtoncloud;
    @FXML
    private TextField loginfield;
    @FXML
    private TextField passwordfiled;
    @FXML
    private Button messageButton;
    @FXML
    private Button loginbutton;
    @FXML
    private Button notDeleteButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button regbutton;
    @FXML
    private Button regbutton1;
    @FXML
    private Pane pane;
    @FXML
    private Pane pane1;

    @FXML
    public void mouseClickedTextArea() {
        textAreaUser.editableProperty().setValue(false);
        textAreaServer.editableProperty().setValue(false);
        messageField.editableProperty().setValue(false);
        messageFieldcloud.editableProperty().setValue(false);
        helpArea.editableProperty().setValue(false);
        textFieldServer.editableProperty().setValue(false);
        textFieldUser.editableProperty().setValue(false);
    }

    @FXML
    void disableTextInput() {
        textAreaUser.editableProperty().setValue(false);
        textAreaServer.editableProperty().setValue(false);
        messageField.editableProperty().setValue(false);
        messageFieldcloud.editableProperty().setValue(false);
        helpArea.editableProperty().setValue(false);
        textFieldServer.editableProperty().setValue(false);
        textFieldUser.editableProperty().setValue(false);
    }

    public void tryToAuth() throws IOException {
        connection();
        try {
            if (!loginfield.getText().equals("") && !passwordfiled.getText().equals("")) {
                dos.write((loginfield.getText() + " " + passwordfiled.getText()).getBytes());
                byte[] buffer = new byte[256];
                int cnt = dis.read(buffer);
                s = new String(buffer, 0, cnt);
                if (s.equals(loginfield.getText())) {
                    rootPathServer = Path.of(rootPathServer.toString() + "/" + s);
                    if (Files.exists(rootPathServer)) {
                        start();
                    } else {
                        Files.createDirectory(rootPathServer);
                        start();
                    }
                } else if (s.equals("notok")) {
                    messageToUserAuth("Wrong login or password");
                }
            }
        } catch (IOException e) {
            System.out.println("Somthing wrong");
        }
    }

    public void tryToReg() throws IOException {
        connection();
        try {
            if (!loginfield.getText().equals("") && !passwordfiled.getText().equals("")) {
                dos.write(("/reg" + " " + loginfield.getText() + " " + passwordfiled.getText()).getBytes());
                byte[] buffer = new byte[256];
                int cnt = dis.read(buffer);
                s = new String(buffer, 0, cnt);
                if (s.equals("addOk")) {
                    messageToUserAuth("Registration success");
                    regbutton1.setVisible(false);
                    regbutton.setVisible(true);
                    loginbutton.setVisible(true);
                } else {
                    messageToUserAuth("This nick is already exist");
                    regbutton.setVisible(false);
                }
            }
        } catch (IOException e) {
            System.out.println("Somthing wrong");
        }
    }

    private void connection() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(IP_ADRESS, PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }
    }

    public void start() {
        pane1.setVisible(false);
        pane.setVisible(true);
        updateFileList();
        pathInField(textFieldUser, rootPathUser);
        pathInField(textFieldServer, rootPathServer);
    }

    private void messageToUserAuth(String s) {
        messageField.setVisible(true);
        messageButton.setVisible(true);
        messageField.appendText(s);
    }

    public void closeMessageAuth() {
        messageField.setVisible(false);
        messageButton.setVisible(false);
        messageField.clear();
    }

    public void closeMessagecloud() {
        deleteButton.setVisible(false);
        notDeleteButton.setVisible(false);
        messageFieldcloud.setVisible(false);
        messageButtoncloud.setVisible(false);
        textFieldFile.clear();
        helpArea.setVisible(false);
        helpArea.clear();
        messageFieldcloud.clear();
    }

    private void messageToUserCloud(String s) {
        messageFieldcloud.setVisible(true);
        messageButtoncloud.setVisible(true);
        messageFieldcloud.appendText(s);
    }

    public void regWindow() {
        regbutton.setVisible(false);
        loginbutton.setVisible(false);
        regbutton1.setVisible(true);
    }

    public void helpMessage() {
        helpMessageOut("1. Use textfeald to enter name \n\r" +
                "2. Click on one of the action button: \n\r" +
                "touch (name) creat file; \n\r" +
                "mkdir (name) creat directory; \n\r" +
                "rm (name) remove file; \n\r" +
                "cd (name) going to the folder; \n\r" +
                "cd (..\\) going to the parent root; \n\r" +
                "rename (old name new name) rename file; \n\r" +
                "cat (name) print the contents of txt file\n\r");
    }

    private void helpMessageOut(String s) {
        helpArea.setVisible(true);
        messageButtoncloud.setVisible(true);
        helpArea.appendText(s);
    }

    public void exit() {
        try {
            socket.close();
            dis.close();
            dos.close();
            pane.setVisible(false);
            pane1.setVisible(true);
            loginfield.clear();
            passwordfiled.clear();
            rootPathServer = Path.of("C:/GeekBrainsJava/CloudStorage/server");
            rootPathUser = Path.of("C:/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFileList() {
        getAllFilesList(rootPathUser, textAreaUser);
        getAllFilesList(rootPathServer, textAreaServer);

    }

    public void uploadFile() throws IOException {
        String s = "/" + textFieldFile.getText();
        moveFIle(rootPathUser, rootPathServer, s);
        updateFileList();
    }

    public void downloadFile() throws IOException {
        String s = "/" + textFieldFile.getText();
        moveFIle(rootPathServer, rootPathUser, s);
        updateFileList();
    }

    private void moveFIle(Path rootPathUser, Path rootPathServer, String s) throws IOException {
        if (s.equals("/")) {
            messageToUserCloud("Enter filename");
        } else {
            Path fromPath = Path.of(rootPathUser + s);
            Path toPath = Path.of(rootPathServer + s);
            if (Files.exists(toPath)) {
                messageToUserCloud("File already exist");
            } else if (Files.exists(fromPath)) {
                Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
                messageToUserCloud("Upload success");
            } else {
                messageToUserCloud("File not found");
            }
        }
    }

    public void getAllFilesList(Path p, TextArea t) {
        try {
            t.clear();
            String files = getFilesList(p);
            t.appendText(files);
        } catch (NullPointerException e) {
            t.appendText("пусто");
        }
    }

    private String getFilesList(Path p) {
        if (new File(String.valueOf(p)).list().length > 0) {
            return String.join("\n\r", new File(String.valueOf(p)).list());
        } else {
            return null;
        }
    }

    public void creatServerFile() throws IOException {
        creatFile(rootPathServer);
        updateFileList();
    }

    public void creatUserFile() throws IOException {
        creatFile(rootPathUser);
        updateFileList();
    }

    public void creatFile(Path p) throws IOException {
        s = "/" + textFieldFile.getText();
        Path newPath = Path.of(p.toString() + s);
        if (s.contains(".")) {
            if (!Files.exists(newPath) && s.contains(".")) {
                Files.createFile(newPath);
                messageToUserCloud("Creat file success");
            } else {
                messageToUserCloud("File already exist");
            }
        } else {
            messageToUserCloud("wrong file format");
        }
    }

    public void pathInField(TextField t, Path p) {
        t.clear();
        t.appendText(p.toAbsolutePath().toString());
    }


    public void createUserDir() throws IOException {
        createDir(rootPathUser);
        updateFileList();
    }

    public void createServerDir() throws IOException {
        createDir(rootPathServer);
        updateFileList();
    }

    private void createDir(Path path) throws IOException {
        s = "/" + textFieldFile.getText();
        Path newPath = Path.of(path.toString() + s);
        if (!Files.isDirectory(newPath)) {
            Files.createDirectory(newPath);
            messageToUserCloud("Creat directory success");
        } else {
            messageToUserCloud("Directory already exist");
        }
    }

    public void removeServerFile() throws IOException {
        removeFile(rootPathServer);
        updateFileList();
    }

    public void removeUserFile() throws IOException {
        removeFile(rootPathUser);
        updateFileList();
    }

    private void removeFile(Path p) throws IOException {
        s = "/" + textFieldFile.getText();
        if (s.equals("/")) {
            messageToUserCloud("Enter filename");
        } else {
            Path newPath = Path.of(p.toString() + s);
            if (Files.exists(newPath)) {
                try {
                    Files.delete(newPath);
                    messageToUserCloud("File delete success");
                } catch (DirectoryNotEmptyException e) {
                    getAllFilesList(newPath, helpArea);
                    helpArea.appendText("\n" + "Directory have this file, delete anyway?");
                    helpArea.setVisible(true);
                    deleteButton.setVisible(true);
                    notDeleteButton.setVisible(true);
                    deleteButton.setOnAction(onMouseClicked -> {
                        try {
                            deleteAllFiles(newPath);
                        } catch (IOException ioException) {
                            messageToUserCloud("One of file is open now. " +
                                    "Close please and repeat operation");
                        }
                    });
                }

            } else {
                messageToUserCloud("File not detected");
            }
        }
    }

    public void deleteAllFiles(Path p) throws IOException {
        System.out.println("okey");
        List<String> list =  new ArrayList<>();
        Files.walkFileTree(p, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file.toAbsolutePath());
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                list.add(dir.toString());
                return super.preVisitDirectory(dir, attrs);
            }
        });
        for (int i = list.size()-1; i >=0 ; i--) {
            Files.delete(Path.of(list.get(i)));
        }
        closeMessagecloud();
        updateFileList();
    }

    public void cdClient() {
        moveFolderUser();
    }

    public void cdServer() {
        moveFolderServer();
    }

    private void moveFolderServer() {
        try {
            s = textFieldFile.getText();
            if (s.equals("..\\") && rootPathServer.getNameCount() >= COUNT) {
                rootPathServer = Path.of(String.valueOf(rootPathServer.getParent()));
            } else if (Files.isDirectory(Path.of(rootPathServer + "/" + s)) && !s.contains("server") && !s.equals("..\\")) {
                rootPathServer = Path.of(rootPathServer + "/" + s);
            } else {
                messageToUserCloud("Wrong direct");
            }
            pathInField(textFieldServer, rootPathServer);
            getAllFilesList(rootPathServer, textAreaServer);
        } catch (InvalidPathException e) {
            messageToUserCloud("Incorrect path");
        }
    }

    private void moveFolderUser() {
        try {
            s = textFieldFile.getText();
            if (s.contains(":\\") && !s.contains("server")) {
                rootPathUser = Path.of(s);
            } else if (s.equals("..\\") && rootPathUser.getNameCount() > 0) {
                rootPathUser = Path.of(String.valueOf(rootPathUser.getParent()));
            } else if (Files.isDirectory(Path.of(rootPathUser + "/" + s)) && !s.contains("server") && !s.equals("..\\")) {
                rootPathUser = Path.of(rootPathUser + "/" + s);
            } else {
                messageToUserCloud("Wrong direct");
            }
            pathInField(textFieldUser, rootPathUser);
            getAllFilesList(rootPathUser, textAreaUser);
        } catch (InvalidPathException e) {
            messageToUserCloud("Incorrect path");
        }
    }

    public void renameFileUser() throws IOException {
        String[] s = textFieldFile.getText().split(" ");
        renameFile(rootPathUser, s[0], s[1]);
        updateFileList();
    }

    public void renameFileServer() {
        String[] s = textFieldFile.getText().split(" ");
        try {
            renameFile(rootPathServer, s[0], s[1]);
            updateFileList();
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            messageToUserCloud("Wrong filename");
        }
    }

    private void renameFile(Path p, String s, String s1) throws IOException {
        Path oldFile = Path.of(p.toString() + "/" + s);
        Path newFile = Path.of(p.toString() + "/" + s1);
        if (Files.exists(oldFile)) {
            if (!Files.exists(newFile)) {
                Files.copy(oldFile, newFile, StandardCopyOption.COPY_ATTRIBUTES);
                Files.delete(oldFile);
                messageToUserCloud("Renamed success");
            } else {
                messageToUserCloud("File already exist");
            }
        } else {
            messageToUserCloud("Wrong file name");
        }
    }

    public void readFileUser() {
        s = textFieldFile.getText();
        if (s.contains(".txt"))
            readFileContent(rootPathUser, s);
        else messageToUserCloud("Use .txt format in filename");
    }

    public void readFileServer() {
        s = textFieldFile.getText();
        if (s.contains(".txt"))
            readFileContent(rootPathServer, s);
        else messageToUserCloud("Use .txt format in filename");
    }


    private void readFileContent(Path rootPath, String s) {
        if (Files.exists(Path.of(rootPath.toString() + "/" + s))) {
            BufferedReader buf = null;
            try {
                String str;
                buf = new BufferedReader(new FileReader(rootPath.toString() + "/" + s));
                if (buf.ready()) {
                    while ((str = buf.readLine()) != null) {
                        helpMessageOut(str);
                    }
                } else {
                    messageToUserCloud("File " + s + " empty");
                }
            } catch (IOException e) {
                messageToUserCloud("File " + s + " not detected");
            } finally {
                try {
                    if (buf != null)
                        buf.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            messageToUserCloud("File " + s + " not detected");
        }
    }


}
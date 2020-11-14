package client;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.Socket;


public class Client extends Application {
    private static Scene scene;
    private  Socket socket;

    @Override
    public void start(Stage primaryStage) throws IOException {
        scene = new Scene(loadFXML(), 691, 440);
        primaryStage.setScene(scene);
        primaryStage.setTitle("MyCloud");
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    private static Parent loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("sample.fxml"));
        return fxmlLoader.load();
    }
    public static void main(String[] args) {
        launch(args);
    }
}

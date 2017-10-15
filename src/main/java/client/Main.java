package client;

import client.core.RootController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public Stage primaryStage;


    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        initRootLayout();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("templates/root.fxml"));

            RootController controller = new RootController(primaryStage);
            loader.setController(controller);

            Parent layout = loader.load();

            primaryStage.setScene(new Scene(layout, 600, 500));
            primaryStage.setTitle("Chat");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

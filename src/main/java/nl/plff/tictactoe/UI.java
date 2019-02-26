package nl.plff.tictactoe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UI extends Application implements Runnable {

    private static UI instance;

    @SuppressWarnings("WeakerAccess") // False positive, public constructor required by JavaFX
    public UI() {
        if (instance != null) return;
        instance = this;
    }

    static UI getInstance() {
        if (instance == null) instance = new UI();
        return instance;
    }

    @Override
    public void run() {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("welcome_stage.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("Erik's TicTacToe");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("tictactoelogo.png")));
        stage.show();
    }

    void runLater(Runnable r) {
        Platform.runLater(r);
    }
}
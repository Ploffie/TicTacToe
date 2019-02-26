package nl.plff.tictactoe;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.plff.plffserver.parcel.Parcel;
import nl.plff.plffserver.parcel.core.OkParcel;
import nl.plff.plffserver.parcel.core.UsernameParcel;
import nl.plff.plffserver.parcel.core.WelcomeParcel;
import nl.plff.plffserver.parcel.processor.ParcelUnit;
import nl.plff.tictactoe.conn.ClientConnection;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeStage implements Initializable {

    public TextField inputUsername;
    public Button buttonGo;
    public Label labelError;
    private ClientConnection connection;
    private Node thisNode;
    private String username;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connection = Main.getConnection();
        ParcelUnit.getInstance().registerParcel(OkParcel.class, this::handleOkParcel);
        ParcelUnit.getInstance().registerParcel(WelcomeParcel.class, this::handleWelcomeParcel);
        buttonGo.setOnAction(e -> {
            this.thisNode = (Node) e.getSource();
            String inputText = inputUsername.getText();
            username = inputText;
            // Loading indicator in UI
            if (inputText == null || inputText.equals("")) return;
            UsernameParcel usernameParcel = new UsernameParcel(inputText);
            try {
                connection.sendParcel(usernameParcel);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unused")
    private void handleOkParcel(Parcel ignored) {
        UI.getInstance().runLater(this::launchGameStage);
        DataHolder.getInstance().setUsername(username);
    }

    @SuppressWarnings("unused")
    private void handleWelcomeParcel(Parcel ignored) {
        UI.getInstance().runLater(() ->
                labelError.setText("Username is invalid or already taken."));
    }

    private void launchGameStage() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("main_stage.fxml"));
            Stage s = new Stage();
            s.setTitle("Erik's TicTacToe");
            s.setScene(new Scene(root, 500, 500));
            s.getIcons().add(new Image(getClass().getResourceAsStream("tictactoelogo.png")));
            s.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            s.show();
            ((Stage) thisNode.getScene().getWindow()).close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}

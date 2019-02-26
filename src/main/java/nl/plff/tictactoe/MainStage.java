package nl.plff.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import nl.plff.plffserver.parcel.Parcel;
import nl.plff.plffserver.parcel.UserActionParcel;
import nl.plff.plffserver.parcel.processor.ParcelUnit;
import nl.plff.plffserver.parcel.tictactoe.*;
import nl.plff.plffserver.server.extensions.tictactoe.Side;
import nl.plff.plffserver.server.extensions.tictactoe.Status;
import nl.plff.tictactoe.conn.ClientConnection;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.UUID;

public class MainStage implements Initializable {

    public ListView<String> listviewOnlineUsers;
    public Button buttonMakeChallenge;
    public DialogPane dialogNewChallenge;
    public Label labelNewChallengeDialog;
    public DialogPane dialogChallengeSent;
    public TabPane masterTabPane;
    private ClientConnection connection;

    private HashMap<UUID, Game> games;
    private Game lastChallengedGame;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = Main.getConnection();
        games = new HashMap<>();
        ParcelUnit unit = ParcelUnit.getInstance();
        unit.registerParcel(UserActionParcel.class, this::handleUserActionParcel);
        unit.registerParcel(UserListParcel.class, this::handleUserListParcel);
        unit.registerParcel(NewChallengeParcel.class, this::handleNewChallengeParcel);
        unit.registerParcel(ChallengeConfirmParcel.class, this::handleChallengeConfirmParcel);
        unit.registerParcel(AssignSideParcel.class, this::handleAssignSideParcel);
        unit.registerParcel(ChallengeStatusParcel.class, p -> {
        });
        unit.registerParcel(MoveParcel.class, this::handleMoveParcel);
        unit.registerParcel(GameOverParcel.class, this::handleGameOverParcel);

        listviewOnlineUsers.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.equals(DataHolder.getInstance().getUsername())) {
                        UI.getInstance().runLater(() -> {
                            buttonMakeChallenge.setText("Challenge this player");
                            buttonMakeChallenge.setDisable(true);
                        });
                    } else {
                        UI.getInstance().runLater(() -> {
                            buttonMakeChallenge.setText("Challenge " + newValue);
                            buttonMakeChallenge.setDisable(false);
                        });
                    }
                });

        ((Button) dialogChallengeSent.lookupButton(ButtonType.CLOSE)).setOnAction(e -> dialogChallengeSent.setVisible(false));
        ((Button) dialogNewChallenge.lookupButton(ButtonType.YES)).setOnAction(this::acceptChallenge);
        ((Button) dialogNewChallenge.lookupButton(ButtonType.NO)).setOnAction(this::denyChallenge);

        buttonMakeChallenge.setOnAction(e -> {
            String selectedPlayer = listviewOnlineUsers.getSelectionModel().getSelectedItem();
            if (selectedPlayer.equals(DataHolder.getInstance().getUsername())) return;
            try {
                connection.sendParcel(new NewChallengeParcel(selectedPlayer));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        try {
            connection.sendParcel(new RequestUserListParcel());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUserListParcel(Parcel parcel) {
        UserListParcel p = (UserListParcel) parcel;
        listviewOnlineUsers.getItems().addAll(p.getUsers());
    }

    private void handleUserActionParcel(Parcel parcel) {
        UserActionParcel p = (UserActionParcel) parcel;
        UI.getInstance().runLater(() -> {
            switch (p.getAction()) {
                case USER_JOINED:
                    listviewOnlineUsers.getItems().add(p.getMessage());
                    break;
                case USER_LEFT:
                    listviewOnlineUsers.getItems().remove(p.getMessage());
                    break;
            }
        });
    }

    @SuppressWarnings("unused")
    private void handleChallengeConfirmParcel(Parcel parcel) {
        ChallengeConfirmParcel p = (ChallengeConfirmParcel) parcel;
        games.put(p.getGameId(), new Game(p.getGameId(), p.getOpponent()));
        UI.getInstance().runLater(() -> dialogChallengeSent.setVisible(true));
    }

    private void handleNewChallengeParcel(Parcel parcel) {
        NewChallengeParcel p = (NewChallengeParcel) parcel;
        lastChallengedGame = new Game(p.getChallengeId(), p.getUsername());
        lastChallengedGame.gameStatus = Status.AWAITING_RESPONSE;
        games.put(p.getChallengeId(), lastChallengedGame);
        UI.getInstance().runLater(() -> {
            labelNewChallengeDialog.setText(p.getUsername() + " has challenged you to a game of TicTacToe!");
            dialogNewChallenge.setVisible(true);
        });
    }

    private void acceptChallenge(ActionEvent e) {
        lastChallengedGame.gameStatus = Status.ACCEPTED;
        sendGameStatus();
    }

    private void denyChallenge(ActionEvent e) {
        lastChallengedGame.gameStatus = Status.DENIED;
        sendGameStatus();
    }

    private void sendGameStatus() {
        try {
            connection.sendParcel(new ChallengeStatusParcel(lastChallengedGame.getGameId(), lastChallengedGame.gameStatus));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        UI.getInstance().runLater(() -> dialogNewChallenge.setVisible(false));
    }

    private void handleAssignSideParcel(Parcel parcel) {
        AssignSideParcel p = (AssignSideParcel) parcel;
        Game g = games.get(p.getGameId());
        if (g == null) {
            System.err.println("Game ID mismatch, game does not exist!");
            return;
        }
        g.setMySide(p.getSide());
        g.gameStatus = Status.IN_PROGRESS;
        g.myTurn = p.getSide().equals(Side.X);
        final Tab t = new Tab(g.getOpponent());
        // Final so we can use t in lambda below
        UI.getInstance().runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("game_stage.fxml"));
                Parent tabContent = loader.load();
                GameStageController c = loader.getController();
                c.setGame(g);
                c.setConnection(connection);
                c.updateTurnLabel();
                g.setController(c);
                t.setContent(tabContent);
            } catch (IOException e) {
                e.printStackTrace();
                t.setContent(new Label("Something went wrong! I don't know what, but your game is not working."));
            }
            masterTabPane.getTabs().add(t);
        });
    }

    private void handleMoveParcel(Parcel parcel) {
        MoveParcel p = (MoveParcel) parcel;
        Game g = games.get(p.getGameId());
        g.makeMove(p.getRow(), p.getColumn(), (g.getMySide().equals(Side.X)) ? Side.O : Side.X);
    }

    private void handleGameOverParcel(Parcel parcel) {
        GameOverParcel p = (GameOverParcel) parcel;
        Game g = games.get(p.getGameId());
        g.end(p.getSide());
    }
}

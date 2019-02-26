package nl.plff.tictactoe;

import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.plff.plffserver.parcel.tictactoe.MoveParcel;
import nl.plff.plffserver.server.extensions.tictactoe.Side;
import nl.plff.tictactoe.conn.ClientConnection;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameStageController implements Initializable {

    private static final int BOARD_SIZE = 3;
    public Pane gameRootPane;
    public Label turnTextIndicator;
    private Game game;
    private Tile[][] board;
    private ClientConnection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        board = new Tile[BOARD_SIZE][BOARD_SIZE];
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                Tile t = new Tile();
                t.setTranslateX(x * 116);
                t.setTranslateY(y * 116);
                board[x][y] = t;

                gameRootPane.getChildren().addAll(t);
            }
        }
    }

    void move(int x, int y, Side side) {
        board[x][y].draw(side);
        updateTurnLabel();
    }

    void setGame(Game game) {
        this.game = game;
    }

    void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    private int getX(Tile t) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].equals(t)) return i;
            }
        }
        return -1;
    }

    private int getY(Tile t) {
        for (Tile[] tiles : board) {
            for (int j = 0; j < tiles.length; j++) {
                if (tiles[j].equals(t)) return j;
            }
        }
        return -1;
    }

    void updateTurnLabel() {
        UI.getInstance().runLater(() -> turnTextIndicator.setText(
                game.myTurn ? "Your turn!" :
                        game.getOpponent().endsWith("s") ? game.getOpponent() + "' turn!" : game.getOpponent() + "'s turn!"
        ));
    }

    private class Tile extends StackPane {
        private Text text = new Text();

        Tile() {
            Rectangle border = new Rectangle(116, 116);
            border.setFill(null);
            border.setStroke(Color.BLACK);

            text.setFont(Font.font(52));
            setAlignment(Pos.CENTER);

            setOnMouseClicked(e -> {
                if (!game.myTurn) return;
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (!text.getText().equals("")) {
                        return;
                    }
                    text.setText(game.getMySide().toString());
                    int x = getX(this);
                    int y = getY(this);
                    game.makeMove(x, y, game.getMySide());
                    try {
                        connection.sendParcel(new MoveParcel(game.getGameId(), x, y));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            getChildren().addAll(border, text);
        }

        private void draw(Side s) {
            text.setText(s.toString());
        }
    }
}

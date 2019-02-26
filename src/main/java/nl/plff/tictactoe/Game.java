package nl.plff.tictactoe;

import nl.plff.plffserver.server.extensions.tictactoe.Side;
import nl.plff.plffserver.server.extensions.tictactoe.Status;

import java.util.UUID;

class Game {

    private final UUID gameId;
    private final String opponent;
    Status gameStatus;
    boolean myTurn;
    private GameStageController controller;
    private Side[][] board;
    private Side mySide;

    Game(UUID gameId, String opponent) {
        this.gameId = gameId;
        this.opponent = opponent;
        this.board = new Side[3][3];
    }

    UUID getGameId() {
        return gameId;
    }

    String getOpponent() {
        return opponent;
    }

    Side getMySide() {
        return mySide;
    }

    void setMySide(Side mySide) {
        this.mySide = mySide;
    }

    void makeMove(int x, int y, Side side) throws IllegalStateException {
        if (board[x][y] != null) throw new IllegalStateException("Spot already taken by " + board[x][y]);
        board[x][y] = side;
        myTurn = side != mySide;
        controller.move(x, y, side);
    }

    void end(Side side) {
        gameStatus = Status.FINISHED;
        myTurn = false;
        UI.getInstance().runLater(() -> controller.turnTextIndicator.setText(side == null ? "It's a tie!" : side.equals(mySide) ? "You win!" : opponent + " wins!"));
    }

    void setController(GameStageController controller) {
        this.controller = controller;
    }
}

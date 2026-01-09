package qchess.chess.logic;

import javafx.scene.control.Button;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.HashSet;

/**
 * @author Quentin Smith
 *
 * Custom JavaFX component for a button.
 * Each chess position is a button which can be clicked.
 * Each chess position can hold nothing, or it can hold a chess piece.
 *
 * All ChessPositions must have a coordinate.
 * To view the grid system:
 * {@link <a href="https://github.com/quentinsmith3006-afk/QChess">QChess Github</a>}}
 */
public class ChessPosition extends Button {
    ChessPiece chessPiece;
    Coordinate coordinate;
    boolean isAttacked = false;
    int amountOfAttackers = 0;
    HashSet<ChessPiece> attackers = new HashSet<>();

    /**
     * Establishes a chess position with a specific chess piece, coordinate and button text.
     * @param text String value to display on the button.
     * @param chessPiece Chess piece to be stored in the ChessPosition.
     * @param coordinate Coordinate of the chess position.
     */
    ChessPosition(String text, ChessPiece chessPiece, Coordinate coordinate) {
        super(text);
        this.chessPiece = chessPiece;
        this.coordinate = coordinate;

        if  (chessPiece != null) {
            if (chessPiece.getGraphic() != null) {
                this.setGraphic(chessPiece.getGraphic());
            } else {
                this.setText(chessPiece.getName());
            } // else
        } // if

        this.getStyleClass().add("chess-button");
        this.setPrefSize(300,300);
        this.setMinSize(40,40);
    }


    /**
     * Establishes a chess position with a null chess piece and a specific coordinate and button text.
     * @param text String value to display on the button.
     * @param coordinate Coordinate of the chess position.
     */
    ChessPosition(String text, Coordinate coordinate) {
        this(text, null, coordinate);
    }


    /**
     * Establishes a chess position with a empty text and a null chess piece but with a specific coordinate.
     * @param coordinate Coordinate of the chess position.
     */
    ChessPosition(Coordinate coordinate) {
        this("", coordinate);
    }

    /**
     * @return chess piece held in the chess position or null if none.
     */
    public ChessPiece getChessPiece() {
        return chessPiece;
    }

    /**
     * Setter for {@code chessPiece}.
     * @param chessPiece chess piece which the current chess position will house.
     */
    public void setChessPiece(ChessPiece chessPiece) {
        this.chessPiece = chessPiece;
    }

    /**
     * @return coordinate of the chess position.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * @return true if the chess position is being attacked and false otherwise.
     */
    public boolean  isAttacked() {
        return isAttacked;
    }

    /**
     * Setter for {@code isAttacked}.
     * @param isAttacked state of being attacked
     */
    void setIsAttacked(boolean isAttacked) {
        this.isAttacked = isAttacked;
    }
}

package qchess.chess.logic;

import javafx.scene.control.Button;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.ArrayList;
import java.util.HashSet;

public class ChessPosition extends Button {
    ChessPiece chessPiece;
    Coordinate coordinate;
    boolean isAttacked = false;
    int amountOfAttackers = 0;
    HashSet<ChessPiece> attackers = new HashSet<>();

    ChessPosition(String text, ChessPiece chessPiece, Coordinate coordinate) {
        super(text);
        this.chessPiece = chessPiece;
        this.coordinate = coordinate;

        if  (chessPiece != null) {
            if (chessPiece.getGraphic() != null) {
                this.setGraphic(chessPiece.getGraphic());
            } else {
                this.setText(chessPiece.getName());
            }
        }

        this.getStyleClass().add("chess-button");
        this.setPrefSize(300,300);
    }

    ChessPosition(String text, Coordinate coordinate) {
        this(text, null, coordinate);
    }

    ChessPosition(Coordinate coordinate) {
        this("", coordinate);
    }

    public ChessPiece getChessPiece() {
        return chessPiece;
    }

    public void setChessPiece(ChessPiece chessPiece) {
        this.chessPiece = chessPiece;
    }

    public boolean  isAttacked() {
        return isAttacked;
    }
    public void setIsAttacked(boolean isAttacked) {
        this.isAttacked = isAttacked;
    }
}

package qchess.chess.logic;

import javafx.scene.control.Button;
import qchess.chess.create.ChessPiece;

public class Position extends Button {
    ChessPiece chessPiece;

    Position(String text, ChessPiece chessPiece) {
        super(text);
        this.chessPiece = chessPiece;

        this.getStyleClass().add("chess-button");
        this.setPrefSize(150,150);
    }

    Position(String text) {
        this(text, null);
    }

    Position() {
        this("");
    }

    public ChessPiece getChessPiece() {
        return chessPiece;
    }

    public void setChessPiece(ChessPiece chessPiece) {
        this.chessPiece = chessPiece;
    }
}

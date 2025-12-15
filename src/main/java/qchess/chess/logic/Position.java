package qchess.chess.logic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

public class Position extends Button {
    ChessPiece chessPiece;

    Position(String text, ChessPiece chessPiece) {
        super(text);
        this.chessPiece = chessPiece;

        if  (chessPiece != null && chessPiece.getGraphic() != null) {
            this.setGraphic(chessPiece.getGraphic());
        }

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

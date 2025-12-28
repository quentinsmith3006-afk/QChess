package qchess.chess.logic;

import javafx.scene.control.Button;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

public class ChessPosition extends Button {
    ChessPiece chessPiece;
    Coordinate coordinate;

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
}

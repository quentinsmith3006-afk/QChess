package qchess.chess.logic;

import javafx.scene.layout.GridPane;
import qchess.chess.create.ChessPiece;

import java.util.List;

public class ChessBoard extends GridPane {
    public Position[] chessMap;

    protected ChessBoard() {
        this.getStylesheets().add("chessboard.css");
        this.getStyleClass().add("chessBoard");
    }

    public ChessPiece getChessPiece(int btnID) {
        return chessMap[btnID].getChessPiece();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        ChessBoard chessBoard;
        Position[] chessMap;

        Builder() {
            this.chessBoard = new ChessBoard();
            this.chessMap = new Position[64];
        }

        public Builder emptyChessGrid() {
            int shiftCounter = 0;
            for (int row = 0; row < 8; row++) {
                shiftCounter++;
                for (int col = 0; col < 8; col++) {
                    int btnID = row * 8 + col;

                    Position pos = new Position();
                    chessMap[btnID] = pos;

                    if ((btnID + shiftCounter) % 2 == 0) {
                        pos.setId("oddButton");
                    } else {
                        pos.setId("evenButton");
                    }

                    pos.getStylesheets().add("chessboard.css");
                    chessBoard.add(chessMap[btnID], col, row);
                }
            }

            return this;
        }

        public Builder add(ChessPiece chessPiece) {
            chessMap[chessPiece.getBtnID()].setChessPiece(chessPiece);

            return this;
        }

        public Builder addAll(List<ChessPiece> chessPieces) {
            for (ChessPiece chessPiece : chessPieces) {
                add(chessPiece);
            }

            return this;
        }

        public ChessBoard build() {
            chessBoard.chessMap = this.chessMap;
            return chessBoard;
        }
    }
}

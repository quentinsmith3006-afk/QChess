package qchess.chess.logic;

import javafx.scene.layout.GridPane;
import qchess.chess.chessmen.*;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

public class ChessBoard extends GridPane {
    public Position[] chessPositions;

    protected ChessBoard(String cssClass, String cssFile) {
        this.getStylesheets().add(cssFile);
        this.getStyleClass().add(cssClass);
    }

    protected ChessBoard() {
        this("chessBoard",  "chessBoard.css");
    }

    public ChessPiece getChessPiece(int btnID) {
        return chessPositions[btnID].getChessPiece();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        ChessBoard chessBoard;
        Position[] chessPositions;
        BoardType boardType;

        private enum BoardType {
            NORMAL,
            EMPTY
        }

        Builder() {
            this.chessBoard = new ChessBoard();
            this.chessPositions = new Position[64];
        }

        private void addWhiteTeam(int row, int col) {
            Coordinate pos = new Coordinate(row, col);

            // white pawn
            if (row == 1) {
                Pawn pawn = new Pawn(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(pawn);
            }

            // white rook
            if (row == 0 && (col == 0 || col == 7)) {
                Rook rook = new Rook(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(rook);
            }

            // white knight
            if (row == 0 && (col == 1 || col == 6)) {
                Knight knight = new Knight(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(knight);
            }

            // white bishop
            if (row == 0 && (col == 2 || col == 5)) {
                Bishop bishop = new Bishop(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(bishop);
            }

            // white queen
            if (row == 0 && col == 3) {
                Queen queen = new Queen(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(queen);
            }

            // white king
            if (row == 0 && col == 4) {
                King king = new King(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(king);
            }
        }

        private void addBlackTeam(int row, int col) {
            Coordinate pos = new Coordinate(row, col);

            // black pawn
            if (row == 6) {
                Pawn pawn = new Pawn(pos, Team.BLACK);
                chessPositions[pos.getBtnID()].setChessPiece(pawn);
            }

            // black rook
            if (row == 7 && (col == 0 || col == 7)) {
                Rook rook = new Rook(pos, Team.BLACK);
                chessPositions[pos.getBtnID()].setChessPiece(rook);
            }

            // black knight
            if (row == 7 && (col == 1 || col == 6)) {
                Knight knight = new Knight(pos, Team.BLACK);
                chessPositions[pos.getBtnID()].setChessPiece(knight);
            }

            // black bishop
            if (row == 7 && (col == 2 || col == 5)) {
                Bishop bishop = new Bishop(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(bishop);
            }

            // black queen
            if (row == 7 && col == 3) {
                Queen queen = new Queen(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(queen);
            }

            // black king
            if (row == 7 && col == 4) {
                King king = new King(pos, Team.WHITE);
                chessPositions[pos.getBtnID()].setChessPiece(king);
            }
        }

        private void exclusivityCheck() {
            if (boardType != null) {
                throw new IllegalStateException("Board types are mutually exclusive");
            }
        }

        private void nullBoardCheck(BoardType boardType) {
            if (boardType == null) {
                throw new IllegalStateException("Board not built");
            }
        }

        public Builder emptyChessBoard() {
            exclusivityCheck();
            boardType = BoardType.EMPTY;

            int shiftCounter = 0;
            for (int row = 0; row < 8; row++) {
                shiftCounter++;
                for (int col = 0; col < 8; col++) {
                    int btnID = row * 8 + col;

                    Position pos = new Position(row + " " + col);
                    chessPositions[btnID] = pos;

                    if ((btnID + shiftCounter) % 2 == 0) {
                        pos.setId("oddButton");
                    } else {
                        pos.setId("evenButton");
                    }

                    pos.getStylesheets().add("chessboard.css");
                    chessBoard.add(chessPositions[btnID], col, row);
                }
            }

            return this;
        }

        public Builder normalChessBoard() {
            exclusivityCheck();
            emptyChessBoard();
            boardType = BoardType.NORMAL;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {

                    addWhiteTeam(row, col);
                    addBlackTeam(row, col);

                }
            }

            return this;
        }

        public Builder stylizeChessPositions(String oddSquaresID, String evenSquaresID, String cssFile) {
            nullBoardCheck(boardType);

            int shiftCounter = 0;
            for (int row = 0; row < 8; row++) {
                shiftCounter++;
                for (int col = 0; col < 8; col++) {
                    int btnID = row * 8 + col;
                    Position pos = chessPositions[btnID];

                    if ((btnID + shiftCounter) % 2 == 0) {
                        pos.setId(oddSquaresID);
                    } else {
                        pos.setId(evenSquaresID);
                    }

                    pos.getStylesheets().remove("chessboard.css");
                    pos.getStylesheets().add(cssFile);
                    chessBoard.add(chessPositions[btnID], col, row);
                }
            }

            return this;
        }

        public Builder stylizeChessBoard (String cssClass, String cssFile) {
            chessBoard.getStylesheets().add(cssFile);
            chessBoard.getStyleClass().add(cssClass);

            return this;
        }

        public Builder add(ChessPiece chessPiece) {
            nullBoardCheck(boardType);

            chessPositions[chessPiece.getBtnID()].setChessPiece(chessPiece);

            return this;
        }

        public Builder addAll(ChessPiece... chessPieces) {
            nullBoardCheck(boardType);

            for (ChessPiece chessPiece : chessPieces) {
                add(chessPiece);
            }

            return this;
        }

        public ChessBoard build() {
            nullBoardCheck(boardType);

            chessBoard.chessPositions = this.chessPositions;
            return chessBoard;
        }
    }
}

package qchess.chess.logic;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.GridPane;
import qchess.chess.chessmen.*;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.logic.event.CheckEvent;
import qchess.chess.logic.event.CheckMateEvent;
import qchess.chess.logic.event.ChessEvent;
import qchess.chess.logic.event.MovementEvent;

import java.util.ArrayList;

public class ChessBoard extends GridPane {
    public ChessPosition[] chessPositions;
    public ArrayList<ChessPiece> chessPieces;
    protected Team playerTeam = Team.WHITE;


    protected ChessBoard(String cssClass, String cssFile) {
        this.getStylesheets().add(cssFile);
        this.getStyleClass().add(cssClass);
        ChessAnnotation.chessAnnotationsInit();
    }

    public void launchGame() {
        chessPieces = new ArrayList<>();

        for (ChessPosition position : chessPositions) {
            EventHandler<ActionEvent> movement = (e) -> Move.positionClick(position);

            position.setOnAction(movement);
            if (position.chessPiece != null) {
                chessPieces.add(position.chessPiece);
            }
        }

        initChessPieces();
    }

    private void initChessPieces() {
        for (ChessPiece chessPiece : chessPieces) {
            chessPiece.setPosition(chessPositions[chessPiece.getBtnID()]);
            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece.getTeam() == playerTeam) {
                chessPosition.setDisable(false);
            }

            //graphics init
            if (chessPiece.getGraphic() == null) {
                chessPosition.setText(chessPiece.getName());
            } else {
                chessPosition.setGraphic(chessPiece.getGraphic());
            }
        }
    }

    public void switchTeams() {
        if (this.playerTeam == Team.BLACK) {
            this.playerTeam = Team.WHITE;
        } else {
            this.playerTeam = Team.BLACK;
        }

        for (ChessPiece chessPiece : chessPieces) {
            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece.getTeam() == playerTeam) {
                chessPosition.setDisable(false);
            } else {
                chessPosition.setDisable(true);
            }
        }
    }

    public void switchTeam(Team team) {
        this.playerTeam = team;
    }

    protected ChessBoard() {
        this("chessBoard",  "chessBoard.css");
    }

    public ChessPosition[] getChessPositions() {
        return chessPositions;
    }

    public ChessPiece getChessPiece(int btnID) {
        return chessPositions[btnID].getChessPiece();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void setOnPieceMovement(EventHandler<ChessEvent> movement) {
        this.addEventHandler(MovementEvent.MOVEMENT, movement);
    }

    public void setOnCheckMate(EventHandler<ChessEvent> movement) {
        this.addEventHandler(CheckMateEvent.CHECK_MATE, movement);
    }

    public void setOnCheck(EventHandler<ChessEvent> movement) {
        this.addEventHandler(CheckEvent.CHECK, movement);
    }

    public void setChessEvent(EventHandler<Event> movement) {
        this.addEventHandler(ChessEvent.ANY, movement);
    }

    public static class Builder {
        ChessBoard chessBoard;
        ChessPosition[] chessPositions;
        BoardType boardType;

        private enum BoardType {
            NORMAL,
            EMPTY
        }

        Builder() {
            this.chessBoard = new ChessBoard();
            this.chessPositions = new ChessPosition[64];
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
                Bishop bishop = new Bishop(pos, Team.BLACK);
                chessPositions[pos.getBtnID()].setChessPiece(bishop);
            }

            // black queen
            if (row == 7 && col == 3) {
                Queen queen = new Queen(pos, Team.BLACK);
                chessPositions[pos.getBtnID()].setChessPiece(queen);
            }

            // black king
            if (row == 7 && col == 4) {
                King king = new King(pos, Team.BLACK);
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

                    ChessPosition pos = new ChessPosition(new Coordinate(row, col));
                    pos.setDisable(true);

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
                    ChessPosition pos = chessPositions[btnID];

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

        public Builder setSwitchTeams() {
            EventHandler<ChessEvent> handle = (ChessEvent me) -> chessBoard.switchTeams();

            chessBoard.addEventFilter(MovementEvent.MOVEMENT, handle);

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
            Move.chessBoard = chessBoard;
            return chessBoard;
        }
    }
}

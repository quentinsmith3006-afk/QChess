package qchess.chess.logic;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.GridPane;
import qchess.chess.chessmen.*;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.interfaces.Castlable;
import qchess.chess.create.interfaces.SpecialPiece;
import qchess.chess.logic.event.*;
import qchess.chess.logic.promotion.PromotionMenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ChessBoard extends GridPane {
    public static final int width = 8;
    public static final int height = 8;
    public ChessPosition[] chessPositions;
    public ArrayList<ChessPiece> chessPieces;
    protected Team playerTeam = Team.WHITE;
    protected boolean castlingAllowed = true;
    protected boolean checkMateAllowed = true;
    protected boolean promotionAllowed = true;
    protected boolean switchTeamAllowed = true;
    protected boolean checkAllowed = true;
    protected boolean pinAllowed = true;
    protected boolean drawAllowed = true;
    protected boolean pieceInCheck = false;
    protected MoveLogic moveLogic;
    private boolean paused;
    boolean singleTeam;

    protected ChessBoard(String cssClass, String cssFile) {
        this.getStylesheets().add(cssFile);
        this.getStyleClass().add(cssClass);

        moveLogic = new MoveLogic(this);

        ChessAnnotation.chessAnnotationsInit();

        this.setMaxSize(600, 600);
    }

    protected ChessBoard() {
        this("chessBoard",  "chessBoard.css");
    }

    public void launchGame() {
        chessPieces = new ArrayList<>();

        for (ChessPosition position : chessPositions) {
            EventHandler<ActionEvent> movement = (e) -> {
                if (!paused) {
                    moveLogic.positionClick(position);
                }
            };

            position.setOnAction(movement);
            if (position.chessPiece != null) {
                chessPieces.add(position.chessPiece);
            }
        }
        boolean whiteTeamExists = false;
        boolean blackTeamExists = false;
        for (ChessPiece chessPiece : chessPieces) {
            if (chessPiece.getTeam() == playerTeam) {
                whiteTeamExists = true;
            } else {
                blackTeamExists = true;
            }
        }

        if (!whiteTeamExists || !blackTeamExists) {
            singleTeam = true;
        }

        initChessEventFilters();
        initChessPieces();
    }

    private void initChessEventFilters() {
        if (promotionAllowed) {
            this.addEventFilter(PromotionEvent.PROMOTION, event -> {
                new PromotionMenu(event.getPromotableChessPiece(), this);
            });

            this.addEventFilter(PostPromotionEvent.POSTPROMOTION, (ChessEvent me) -> {
                moveLogic.playableSquaresRefinery(me.getInstigator(), me.getInstigator().getPosition(), chessPositions, true, false);
            });
        }
        if (switchTeamAllowed) {
            this.addEventFilter(MovementEvent.MOVEMENT, (ChessEvent me) -> this.switchTeams());
        }
        if (checkAllowed) {
            this.addEventFilter(CheckEvent.CHECK, (ChessEvent me) -> {pieceInCheck = true;});

            if (checkMateAllowed) {
                this.addEventFilter(CheckMateEvent.CHECK_MATE, event -> {
                    System.out.println("CHECKMATE");
                });
            }
        }
        this.addEventFilter(DrawEvent.DRAW, even -> {
            System.out.println("Draw");
        });
    }

    private void initChessPieces() {
        for (ChessPiece chessPiece : chessPieces) {
            chessPiece.setPosition(chessPositions[chessPiece.getBtnID()]);
            ChessPosition chessPosition = chessPiece.getPosition();

            // init castle vectors
            if (chessPiece instanceof Castlable castlable && castlingAllowed) {
                HashMap<PieceScalar, CastleVector> castleDirections = castlable.getCastleDirections();
                castleDirections.forEach((scalar, vector) -> {
                    try {
                        ChessPiece castleDependent = chessPositions[vector.getTerminalPoint().getBtnID()].getChessPiece();
                        vector.setCastleDependent(castleDependent);
                    } catch (NoSuchElementException e) {
                    }
                });

                castlable.setInitializedCastleDirections(castleDirections);
            }

            if (chessPiece.getTeam() == playerTeam) {
                chessPosition.setDisable(false);
            } else if (!singleTeam) {
                moveLogic.addAttackers(chessPiece, chessPosition);
            }

            //graphics init
            if (chessPiece.getGraphic() == null) {
                chessPosition.setText(chessPiece.getName());
            } else {
                chessPosition.setGraphic(chessPiece.getGraphic());
            }
        }

        enableChessPieces();

        if (checkMateAllowed) {
            initialCheckForCheck();
        }
    }

    public void enableChessPieces() {
        if (chessPieces.isEmpty()) {
            return;
        }

        boolean aPieceWasEnabled = false;
        for (ChessPiece chessPiece : chessPieces) {
            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece.getTeam() == playerTeam) {
                aPieceWasEnabled = true;
            }
            boolean chessPieceIsSpecial = chessPiece instanceof SpecialPiece;
            if (!chessPieceIsSpecial) {
                chessPosition.setDisable(chessPiece.getTeam() != playerTeam);
            }
        }

        // Mechanism which allows you to start on the team which you have pieces.
        if (!aPieceWasEnabled) {
            switchTeams();
            enableChessPieces();
        }
    }

    public void initialCheckForCheck() {
        for (ChessPiece chessPiece : chessPieces) {
            if (moveLogic.scanForCheck(chessPiece, chessPiece.getPosition())) {
                if (chessPiece.getTeam() == playerTeam) {
                    System.out.println("BASICALLY CHECKMATE");
                }
            }
        }
    }

    public void switchTeams() {
        if (this.playerTeam == Team.BLACK) {
            this.playerTeam = Team.WHITE;
        } else {
            this.playerTeam = Team.BLACK;
        }
    }

    public void switchTeam(Team team) {
        this.playerTeam = team;
    }

    public void reset() {
        moveLogic = new MoveLogic(this);

        ChessAnnotation.chessAnnotationsInit();

        this.launchGame();
    }

    public ArrayList<ChessPiece> getChessPieces() {
        return new ArrayList<>(chessPieces);
    }

    public MoveLogic getMoveLogic() {
        return moveLogic;
    }

    public ChessPosition[] getChessPositions() {
        return chessPositions;
    }

    public ChessPiece getChessPiece(Coordinate coordinate) {
        return chessPositions[coordinate.getBtnID()].getChessPiece();
    }

    public ChessPiece getChessPiece(int btnID) {
        return chessPositions[btnID].getChessPiece();
    }

    public void setOnCapture(EventHandler<ChessEvent> captureOperation) {
        this.addEventHandler(CaptureEvent.CAPTURE, captureOperation);
    }

    public void setOnPromotion(EventHandler<ChessEvent> promotionOperation) {
        this.addEventHandler(PromotionEvent.PROMOTION, promotionOperation);
    }

    public void setOnPostPromotion(EventHandler<ChessEvent> postPromotionOperation) {
        this.addEventHandler(PostPromotionEvent.POSTPROMOTION, postPromotionOperation);
    }

    public void setOnEnpassant(EventHandler<ChessEvent> enpassantOperation) {
        this.addEventHandler(EnpassantEvent.ENPASSANT , enpassantOperation);
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

    public boolean isSingleTeam() {
        return singleTeam;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        this.paused = true;
    }

    public void unpause() {
        this.paused = false;
    }

    public static Builder newBuilder() {
        return new Builder();
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

        public Builder disableAutoTeamSwitch() {

            chessBoard.switchTeamAllowed = false;

            return this;
        }

        public Builder add(ChessPiece chessPiece) {
            nullBoardCheck(boardType);

            chessPositions[chessPiece.getBtnID()].setChessPiece(chessPiece);

            return this;
        }

        public Builder disableCastling() {

            chessBoard.castlingAllowed = false;

            return this;
        }

        public Builder disableCheckMate() {

            chessBoard.checkMateAllowed = false;

            return this;
        }

        public Builder disableCheck() {

            chessBoard.checkAllowed = false;

            return this;
        }

        public Builder disablePromotion() {

            chessBoard.promotionAllowed = false;

            return this;
        }

        public Builder addAll(ChessPiece... chessPieces) {
            nullBoardCheck(boardType);

            for (ChessPiece chessPiece : chessPieces) {
                add(chessPiece);
            }

            return this;
        }

        public Builder addAll(Collection<ChessPiece> chessPieces) {
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

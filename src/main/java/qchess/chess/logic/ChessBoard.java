package qchess.chess.logic;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import qchess.chess.chessmen.*;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.exceptions.MultipleBoardTypesException;
import qchess.chess.create.exceptions.NullBoardException;
import qchess.chess.create.interfaces.Castlable;
import qchess.chess.create.interfaces.SpecialPiece;
import qchess.chess.logic.event.*;
import qchess.chess.logic.promotion.PromotionMenu;

import java.util.*;

/**
 * @author Quentin Smith
 *
 * The ChessBoard class is a custom JavaFX component. It represents
 * a basic 8x8 chessboard. Each position on the chessboard grid is a
 * button called {@code ChessPosition}. Each ChessPosition can hold one
 * {@code ChessPiece}. Every chessboard needs a {@code MoveLogic} object
 * to be able to handle chess piece movements.
 *
 * {@link qchess.chess.logic.ChessPosition}
 * {@link qchess.chess.logic.MoveLogic}
 * {@link qchess.chess.create.ChessPiece}
 */
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

    /**
     * Assigns the board a CSS class and a CSS file. Creates a MoveLogic
     * object and initializes annotation effects.
     * @param cssClass represents the board's CSS class
     * @param cssFile CSS file based on its local location
     */
    protected ChessBoard(String cssClass, String cssFile) {
        this.getStylesheets().add(cssFile);
        this.getStyleClass().add(cssClass);

        moveLogic = new MoveLogic(this);

        ChessAnnotation.chessAnnotationsInit();

        this.setMaxSize(600, 600);
    }

    /**
     * Applies a default "chessBoard" CSS class and a default "chessBoard.css" file.
     */
    protected ChessBoard() {
        this("chessBoard",  "chessBoard.css");
    }

    /**
     * Initializes chess pieces, event filters and chess positions which launches the game.
     */
    public void launchGame() {
        chessPieces = new ArrayList<>();

        for (ChessPosition position : chessPositions) {
            EventHandler<ActionEvent> movement = (e) -> {
                if (!paused) {
                    moveLogic.positionClick(position);
                } // if
            };

            position.setOnAction(movement);
            if (position.chessPiece != null) {
                chessPieces.add(position.chessPiece);
            } // if
        } // for-each
        boolean whiteTeamExists = false;
        boolean blackTeamExists = false;
        for (ChessPiece chessPiece : chessPieces) {
            if (chessPiece.getTeam() == playerTeam) {
                whiteTeamExists = true;
            } else {
                blackTeamExists = true;
            } // else
        } // for-each

        if (!whiteTeamExists || !blackTeamExists) {
            singleTeam = true;
        } // if

        initChessEventFilters();
        initChessPieces();
    }

    /**
     * Initializes chess event filters based on what is allowed.
     */
    private void initChessEventFilters() {
        if (promotionAllowed) {
            this.addEventFilter(PromotionEvent.PROMOTION, event -> {
                new PromotionMenu(event.getPromotableChessPiece(), this);
            });

            this.addEventFilter(PostPromotionEvent.POSTPROMOTION, (ChessEvent me) -> {
                moveLogic.playableSquaresRefinery(me.getInstigator(), me.getInstigator().getPosition(), chessPositions, true, false);
            });
        } // if
        if (switchTeamAllowed) {
            this.addEventFilter(MovementEvent.MOVEMENT, (ChessEvent me) -> this.switchTeams());
        } // if
        if (checkAllowed) {
            this.addEventFilter(CheckEvent.CHECK, (ChessEvent me) -> {pieceInCheck = true;});

            if (checkMateAllowed) {
                this.addEventFilter(CheckMateEvent.CHECK_MATE, event -> {
                    System.out.println("CHECKMATE");
                });
            } // if
        } // if
        this.addEventFilter(DrawEvent.DRAW, even -> {
            System.out.println("Draw");
        });
    }

    /**
     * Initialize chess piece's attack squares, castling vectors and images/text.
     */
    private void initChessPieces() {
        for (ChessPiece chessPiece : chessPieces) {
            chessPiece.setPosition(chessPositions[chessPiece.getBtnID()]);
            ChessPosition chessPosition = chessPiece.getPosition();

            // init castle vectors
            if (chessPiece instanceof Castlable castlable && castlingAllowed) {
                HashMap<PieceScalar, CastleVector> castleDirections = getCastleDirectionsMap(castlable);

                castlable.setInitializedCastleDirections(castleDirections);
            } // if

            if (chessPiece.getTeam() == playerTeam) {
                chessPosition.setDisable(false);
            } else if (!singleTeam) {
                moveLogic.addAttackers(chessPiece, chessPosition);
            } // else

            //graphics init
            if (chessPiece.getGraphic() == null) {
                chessPosition.setText(chessPiece.getName());
            } else {
                chessPosition.setGraphic(chessPiece.getGraphic());
            } // else
        }

        enableChessPieces();

        if (checkMateAllowed) {
            initialCheckForCheck();
        } // if
    }

    /**
     * @param castlable chess piece which can castle.
     * @return hashmap of castle directions.
     */
    private HashMap<PieceScalar, CastleVector> getCastleDirectionsMap(Castlable castlable) {
        HashMap<PieceScalar, CastleVector> castleDirections = castlable.getCastleDirections();
        castleDirections.forEach((scalar, vector) -> {
            try {
                ChessPiece castleDependent = chessPositions[vector.getTerminalPoint().getBtnID()].getChessPiece();
                vector.setCastleDependent(castleDependent);
            } catch (NoSuchElementException e) {
                // This just means that the terminal point doesn't exist because the vector didn't generate
                // due to chess piece being at the far end of the chess board.
            } // try-catch
        });
        return castleDirections;
    }

    /**
     * Sets each chess piece's position to enabled if that chess piece is the same team as {@code playerTeam}.
     */
    public void enableChessPieces() {
        if (chessPieces.isEmpty()) {
            return;
        } // if

        boolean aPieceWasEnabled = false;
        for (ChessPiece chessPiece : chessPieces) {
            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece.getTeam() == playerTeam) {
                aPieceWasEnabled = true;
            } // if
            boolean chessPieceIsSpecial = chessPiece instanceof SpecialPiece;
            if (!chessPieceIsSpecial) {
                chessPosition.setDisable(chessPiece.getTeam() != playerTeam);
            } // if
        } // for-each

        // Mechanism which allows you to start on the team which you have pieces.
        if (!aPieceWasEnabled) {
            switchTeams();
            enableChessPieces();
        } // if
    }

    /**
     * Checks for a check at the start of the game.
     */
    public void initialCheckForCheck() {
        for (ChessPiece chessPiece : chessPieces) {
            if (moveLogic.scanForCheck(chessPiece, chessPiece.getPosition())) {
                if (chessPiece.getTeam() == playerTeam) {
                    System.out.println("BASICALLY CHECKMATE");
                } // if
            } // if
        } // for-each
    }

    /**
     * Switches teams based on the current {@code playerTeam}.
     */
    public void switchTeams() {
        if (this.playerTeam == Team.BLACK) {
            this.playerTeam = Team.WHITE;
        } else {
            this.playerTeam = Team.BLACK;
        } // else
    }

    /**
     * Switches teams to the specified team.
     * @param team team to switch to.
     */
    public void switchTeam(Team team) {
        this.playerTeam = team;
    }

    /**
     * Resets the chess board.
     */
    public void reset() {
        moveLogic = new MoveLogic(this);

        ChessAnnotation.chessAnnotationsInit();

        this.launchGame();
    }

    /**
     * Getter method for {@code playerTeam}.
     * @return current team.
     */
    public Team getPlayerTeam() {
        return playerTeam;
    }

    /**
     * Getter method for {@code chessPieces}.
     * @return all chess pieces in as an arraylist.
     */
    public ArrayList<ChessPiece> getChessPieces() {
        return new ArrayList<>(chessPieces);
    }

    /**
     * Getter method for {@code moveLogic}.
     * {@see qchess.chess.logic.MoveLogic}.
     * @return information and actions pertaining to this chessboard's movement.
     */
    public MoveLogic getMoveLogic() {
        return moveLogic;
    }

    /**
     * Getter method for {@code chessPositions}.
     * {@see qchess.chess.logic.ChessPosition}
     * @return all chess positions as an array.
     */
    public ChessPosition[] getChessPositions() {
        return chessPositions;
    }

    /**
     * Gets the chess piece at the specified coordinate or returns null if there isn't one.
     * @throws IndexOutOfBoundsException when the coordinate defined is out of the chess board bounds.
     * @param coordinate coordinate to search for a chess piece.
     * @return the chess piece at the specified coordinate.
     */
    public ChessPiece getChessPiece(Coordinate coordinate) {
        return chessPositions[coordinate.getBtnID()].getChessPiece();
    }

    /**
     * Gets the chess piece at the specified coordinate or returns null if there isn't one.
     * @throws IndexOutOfBoundsException when the btnID is out of the chess board bounds.
     * @param btnID the id of the chess position to search for a chess piece.
     * @return the chess piece at the specified btnID.
     *
     * {@link <a href="https://github.com/quentinsmith3006-afk/QChess">BtnID</a>}
     */
    public ChessPiece getChessPiece(int btnID) {
        return chessPositions[btnID].getChessPiece();
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.logic.event.CaptureEvent}.
     * @param captureOperation event handler which executes.
     */
    public void setOnCapture(EventHandler<ChessEvent> captureOperation) {
        this.addEventHandler(CaptureEvent.CAPTURE, captureOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.logic.event.PromotionEvent}.
     * @param promotionOperation event handler which executes.
     */
    public void setOnPromotion(EventHandler<ChessEvent> promotionOperation) {
        this.addEventHandler(PromotionEvent.PROMOTION, promotionOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.logic.event.CaptureEvent}.
     * @param postPromotionOperation event handler which executes.
     */
    public void setOnPostPromotion(EventHandler<ChessEvent> postPromotionOperation) {
        this.addEventHandler(PostPromotionEvent.POSTPROMOTION, postPromotionOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.logic.event.CaptureEvent}.
     * @param enpassantOperation event handler which executes.
     */
    public void setOnEnpassant(EventHandler<ChessEvent> enpassantOperation) {
        this.addEventHandler(EnpassantEvent.ENPASSANT , enpassantOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.logic.event.CaptureEvent}.
     * @param movementOperation event handler which executes.
     */
    public void setOnPieceMovement(EventHandler<ChessEvent> movementOperation) {
        this.addEventHandler(MovementEvent.MOVEMENT, movementOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.event.CaptureEvent}.
     * @param checkMateOperation event handler which executes.
     */
    public void setOnCheckMate(EventHandler<ChessEvent> checkMateOperation) {
        this.addEventHandler(CheckMateEvent.CHECK_MATE, checkMateOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.event.CaptureEvent}.
     * @param checkOperation event handler which executes.
     */
    public void setOnCheck(EventHandler<ChessEvent> checkOperation) {
        this.addEventHandler(CheckEvent.CHECK, checkOperation);
    }

    /**
     * Executes the EventHandler upon the firing of a {@code qchess.chess.event.CaptureEvent}.
     * @param chessEventOperation event handler which executes.
     */
    public void setChessEvent(EventHandler<Event> chessEventOperation) {
        this.addEventHandler(ChessEvent.ANY, chessEventOperation);
    }

    /**
     * @return a boolean which is true when there is only 1 team and false otherwise.
     */
    public boolean isSingleTeam() {
        return singleTeam;
    }

    /**
     * @return a boolean which is true when the chess board is paused and false otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Pauses the chess board which makes all pieces unable to move.
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Unpauses the chess board which allows pieces to move normally.
     */
    public void unpause() {
        this.paused = false;
    }

    /**
     * Begins the construction of a chess board.
     * @return a builder to build a chess board.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder class to build an immutable chess board.
     * Not thread-safe.
     */
    public static class Builder {
        ChessBoard chessBoard;
        ChessPosition[] chessPositions;
        BoardType boardType;

        /**
         * Enumeration that represents each board state.
         * Algebraic, BtnID, and Grid board types are showcase boards.
         * Normal and Empty are playable boards.
         */
        private enum BoardType {
            NORMAL,
            EMPTY,
            ALGEBRAIC,
            BTNID,
            GRID
        }

        /**
         * Initializes the chess board anc its chess positions.
         * {@see qchess.chess.logic.ChessPosition}
         */
        Builder() {
            this.chessBoard = new ChessBoard();
            this.chessPositions = new ChessPosition[64];
        }

        /**
         * Adds the normal white team pieces to the chess board.
         * @param row row which may have a chess piece placed.
         * @param col column which may have a chess piece placed.
         */
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

        /**
         * Adds the normal black team pieces to the chess board.
         * @param row row which may have a chess piece placed.
         * @param col column which may have a chess piece placed.
         */
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

        /**
         * Checks if a board type is exclusive.
         * @throws MultipleBoardTypesException exception thrown if multiple board states are used on a single chess board.
         */
        private void exclusivityCheck() {
            if (boardType != null) {
                throw new MultipleBoardTypesException("Board types are mutually exclusive");
            } // if
        }

        /**
         * Checks if the board type is null.
         * @throws NullBoardException if the board type is null.
         */
        private void nullBoardCheck() {
            if (boardType == null) {
                throw new NullBoardException("Board not built");
            } // if
        }

        /**
         * @throws IllegalStateException if a showcase board is present.
         */
        private void showcaseBoardCheck() {
            switch (boardType) {
                case ALGEBRAIC:
                case BTNID:
                case GRID:
                    throw new IllegalStateException("Cannot customize rules for a BoardType which is for showcasing.");
            } // switch
        }

        /**
         * Creates an empty playable chess board.
         * @throws MultipleBoardTypesException if board types are not mutually exclusive.
         * @return new state of the builder.
         */
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
                    } // else

                    pos.getStylesheets().add("chessboard.css");
                    chessBoard.add(chessPositions[btnID], col, row);
                } // for
            } // for

            return this;
        }

        /**
         * Creates a playable chess board which has its pieces set up as a classic chess game.
         * @throws MultipleBoardTypesException if board types are not mutually exclusive.
         * @return new state of the builder.
         */
        public Builder normalChessBoard() {
            exclusivityCheck();
            emptyChessBoard();
            boardType = BoardType.NORMAL;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {

                    addWhiteTeam(row, col);
                    addBlackTeam(row, col);
                } // for
            } // for

            return this;
        }

        /**
         * Creates a showcase chess board which displays the algebraic notation of each {@code ChessPosition}.
         * @throws MultipleBoardTypesException if board types are not mutually exclusive.
         * @return new state of the builder.
         */
        public Builder algebraicNotationSystem() {
            exclusivityCheck();
            emptyChessBoard();
            boardType = BoardType.ALGEBRAIC;

            for (ChessPosition pos : chessPositions) {
                pos.setText(pos.coordinate.getAlgebraicName());
                pos.setFont(new Font("Calibri Bold", 20));
            } // for-each

            return this;
        }

        /**
         * Creates a showcase chess board which displays the btnID of each {@code ChessPosition}.
         * @throws MultipleBoardTypesException if board types are not mutually exclusive.
         * @return new state of the builder.
         */
        public Builder btnIDSystem() {
            exclusivityCheck();
            emptyChessBoard();
            boardType = BoardType.BTNID;

            for (ChessPosition pos : chessPositions) {
                pos.setText(pos.coordinate.getBtnID() + "");
                pos.setFont(new Font("Calibri Bold", 20));
            } // for-each

            return this;
        }

        /**
         * Creates a showcase chess board which displays the grid coordinates of each {@code ChessPosition}.
         * @throws MultipleBoardTypesException if board types are not mutually exclusive.
         * @return new state of the builder.
         */
        public Builder gridSystem() {
            exclusivityCheck();
            emptyChessBoard();
            boardType = BoardType.GRID;

            for (ChessPosition pos : chessPositions) {
                pos.setText( "(" + pos.coordinate.getRow() + ", " + pos.coordinate.getCol() + ")");
                pos.setFont(new Font("Calibri Bold", 15));
            } // for-each

            return this;
        }

        /**
         * Sets the traditional checkerboard pattern that chess normally has.
         * @param oddSquaresID CSS id of the odd squares.
         * @param evenSquaresID CSS id of the event squares.
         * @param cssFile CSS file which <i>should</i> customize the even and odd squares based on the provided IDs.
         * @throws NullBoardException when {@code BoardType} is null.
         * @return new state of the builder.
         */
        public Builder stylizeChessPositions(String oddSquaresID, String evenSquaresID, String cssFile) {
            nullBoardCheck();

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
                } // for
            } // for

            return this;
        }

        /**
         * Stylizes the chess board.
         * @param cssClass CSS class of the chess board.
         * @param cssFile CSS file which is assigned to the chess board's style sheets.
         * @throws NullBoardException when {@code BoardType} is null.
         * @return new state of the builder.
         */
        public Builder stylizeChessBoard (String cssClass, String cssFile) {
            nullBoardCheck();

            chessBoard.getStylesheets().add(cssFile);
            chessBoard.getStyleClass().add(cssClass);

            return this;
        }

        /**
         * Disables teams automatically switching after each move.
         * @throws IllegalStateException when {@code boardType} is a showcase board
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder disableAutoTeamSwitch() {
            showcaseBoardCheck();
            nullBoardCheck();

            chessBoard.switchTeamAllowed = false;

            return this;
        }

        /**
         * Disables castling even for {@code Castlables}.
         * @throws IllegalStateException when {@code boardType} is a showcase board
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder disableCastling() {
            showcaseBoardCheck();
            nullBoardCheck();

            chessBoard.castlingAllowed = false;

            return this;
        }

        /**
         * Disables check mate which stops {@code CheckMateEvent} events from firing.
         * @throws IllegalStateException when {@code boardType} is a showcase board
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder disableCheckMate() {
            showcaseBoardCheck();
            nullBoardCheck();

            chessBoard.checkMateAllowed = false;

            return this;
        }

        /**
         * Disables check which stops {@code CheckEvent} events from firing.
         * @throws IllegalStateException when {@code boardType} is a showcase board
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder disableCheck() {
            showcaseBoardCheck();
            nullBoardCheck();

            chessBoard.checkAllowed = false;

            return this;
        }

        /**
         * Disables promotion which stops {@code PromotionEvent} and {@code PostPromotionEvent} events from firing.
         * @throws IllegalStateException when {@code boardType} is a showcase board
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder disablePromotion() {
            showcaseBoardCheck();
            nullBoardCheck();

            chessBoard.promotionAllowed = false;

            return this;
        }

        /**
         * Disables draw which stops {@code DrawEvent} events from firing.
         * @throws IllegalStateException when {@code boardType} is a showcase board
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder disableDraw() {
            showcaseBoardCheck();
            nullBoardCheck();

            chessBoard.drawAllowed = false;

            return this;
        }

        /**
         * Adds chess pieces to the chess board.
         * @param chessPieces varargs of chess pieces to add to the board.
         * @return new state of the builder.
         */
        public Builder addAll(ChessPiece... chessPieces) {
            addAll(Arrays.asList(chessPieces));
            return this;
        }

        /**
         * Adds chess pieces to the chess board.
         * @param chessPieces {@code Collection} of chess pieces to add to the board.
         * @return new state of the builder.
         */
        public Builder addAll(Collection<ChessPiece> chessPieces) {
            nullBoardCheck();

            for (ChessPiece chessPiece : chessPieces) {
                add(chessPiece);
            } // for-each

            return this;
        }

        /**
         * Adds a single chess piece to the chess board.
         * @param chessPiece chess piece to add to the chess board.
         * @throws NullBoardException when {@code boardType} is null.
         * @return new state of the builder.
         */
        public Builder add(ChessPiece chessPiece) {
            nullBoardCheck();
            switch (boardType) {
                case ALGEBRAIC:
                case BTNID:
                case GRID:
                    throw new IllegalStateException("Cannot add a chess piece to BoardType which is for showcasing.");
            } // switch

            chessPositions[chessPiece.getBtnID()].setChessPiece(chessPiece);

            return this;
        }

        /**
         * Completes the chess board.
         * @throws NullBoardException when {@code boardType} is null.
         * @return completed chess board.
         */
        public ChessBoard build() {
            nullBoardCheck();

            chessBoard.chessPositions = this.chessPositions;

            return chessBoard;
        }
    }
}

package qchess.chess.logic;

import qchess.chess.chessmen.Pawn;
import qchess.chess.create.*;
import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.exceptions.IncompleteSwitchPositionsException;
import qchess.chess.create.exceptions.NothingToCaptureException;
import qchess.chess.create.exceptions.PieceInWayException;
import qchess.chess.create.interfaces.*;
import qchess.chess.create.piecemodifiers.Xray;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.special.Enpassant;
import qchess.chess.logic.event.*;

import java.util.*;

/**
 * @author Quentin Smith
 * <br>
 * <p>
 * This class contains all move logic that pertains to chess pieces. This includes all chess rules that
 * govern chess piece movement.
 * </p>
 * <ul>
 *     <li>Playables -> what a chess piece can play given their current position.</li>
 *     <li>Capturable -> what a chess piece can capture given their current position.</li>
 *     <li>Current team -> refers to the team which is active on the chess board.</li>
 * </ul>
 *
 * {@link qchess.chess.create.ChessPiece}
 * {@link qchess.chess.logic.ChessPosition}
 * {@link qchess.chess.logic.ChessBoard}
 * {@link qchess.chess.create.Coordinate}
 * {@link qchess.chess.create.direction.ChessDirection}
 */
public class MoveLogic extends ChessLogic {
    public ArrayList<ChessPosition> pastPositions;
    public ChessPosition pastChessPosition;
    private final ArrayList<PinnerPinnedCheckable> pinnerPinnedStorage;
    private final HashMap<ChessPiece, ChessPiece>  discoveredCheckMap;
    private final HashMap<ChessPiece, List<ChessPosition>> memoizedPastPositions;
    private final HashMap<ChessPiece, List<ChessPosition>> memoizedAttacks;
    private final HashMap<ChessPiece, Integer> chessPieceNumMovesMap;
    private final ChessPosition[] drawByRepetitionStorageWhite;
    private final ChessPosition[] drawByRepetitionStorageBlack;

    private final ArrayList<Information> pinInformation;
    private final ArrayList<Information> checkInformation;

    final HashMap<String, Boolean> castleIsPossibleMap;
    final HashMap<String, Boolean> canCastleMap;


    boolean altCastle = false;

    // debugging
    int num;
    int totnum;

    // Meta data
    final int numRepetitionToDraw = 3;
    int individualRepetitionToDraw = (numRepetitionToDraw * 2) - 1;
    int numMoves;
    int numFullMoves;
    int numMovesToFiftyMoveRuleDraw;
    int numMovesAvailable;
    EnpassantInfo enpassantInfo; // records whether there is an enpassant piece or not.

    /**
     * Holds the 3 chess pieces involved in a pin: instigator, victim and checkable.
     * @param pinner piece that pins {@code victim} to {@code checkable}.
     * @param pinned piece that is victim.
     * @param checkable piece which can be checked.
     */
    record PinnerPinnedCheckable(ChessPiece pinner, ChessPiece pinned, ChessPiece checkable) {}

    /**
     *
     */
    sealed interface Information {
        Integer distFromOrigin();
        ChessDirection direction();
        ChessPiece victim();
        ChessPiece instigator();
    }

    /**
     * Holds the information regarding a pin.
     * @param direction The exact {@code ChessDirection} originating from {@code pinnerPiece} which pins {@code pinnedPiece} to a checkable
     * @param instigator piece that pins {@code victim} to a checkable.
     * @param victim piece that is victim.
     */
    record PinInformation(Integer distFromOrigin, ChessDirection direction, ChessPiece victim, ChessPiece instigator) implements Information {} // Renamed from pinnedPiece to victim

    /**
     * Holds the information regarding a check.
     * @param distFromOrigin the distance of the checkable from the checking piece.
     * @param direction the exact {@code ChessDirection} originating from {@code instigator} which checks a checkable.
     * @param victim the checkable chess piece.
     * @param instigator the chess piece that initiates check onto a checkable.
     */
    record CheckInformation(Integer distFromOrigin, ChessDirection direction, ChessPiece victim, ChessPiece instigator) implements Information {}

    /**
     * Initializes storage capabilities for various items of interest.
     * @param chessBoard chess board that the move logic will use.
     */
    public MoveLogic(ChessBoard chessBoard) {
        super(chessBoard);
        
        pinInformation = new ArrayList<>();
        checkInformation = new ArrayList<>();
        castleIsPossibleMap = new HashMap<>();
        canCastleMap = new HashMap<>();

        chessPieceNumMovesMap = new HashMap<>();
        discoveredCheckMap = new HashMap<>();
        pinnerPinnedStorage = new ArrayList<>();
        memoizedPastPositions = new HashMap<>();
        memoizedAttacks  = new HashMap<>();

        drawByRepetitionStorageWhite = new ChessPosition[individualRepetitionToDraw];
        drawByRepetitionStorageBlack = new ChessPosition[individualRepetitionToDraw];
    }

    /**
     * When a button is clicked, this method activates.
     * @param clickedPosition represents the current clicked position.
     */
     void positionClick(ChessPosition clickedPosition) {
        ChessPiece chessPiece = clickedPosition.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement (empty ChessPosition is clicked)
        if (chessPiece == null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            disablePastPlayablePositions(pastChessPiece);
            disablePastPositionsAttacked(pastChessPiece);

            resetMetaData();

            processNormalMovement(clickedPosition, chessPositions);
            return;
        } // if

        // Capturing (piece from different team is clicked)
        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();

            if (!(clickedPosition.getChessPiece() instanceof Castlable) && altCastle && chessBoard.castlingAllowed) {
                castle((Castlable) pastChessPiece, pastChessPiece, clickedPosition, chessPositions);
                altCastle = false;
                return;
            }
            // Clicked castlable twice
            altCastle = pastChessPiece instanceof Castlable && pastChessPiece.equals(chessPiece);

            if (pastChessPiece != null && (pastChessPiece.getTeam() != chessPiece.getTeam() || chessPiece instanceof SpecialPiece) && pastChessPiece.getTeam() == chessBoard.playerTeam) {
                disablePastPlayablePositions(pastChessPiece);
                disablePastPositionsAttacked(pastChessPiece);

                resetMetaData();

                processCapture(clickedPosition);
                return;
            } // if
        } // if

        // if a piece from the same-team is clicked
        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            disablePastPlayablePositions(pastChessPiece);
        } // if

        // Playable Squares Refiner
        playableSquaresRefinery(chessPiece, clickedPosition, chessPositions, true,true);
    }

    /**
     * Sets prerequisites to a chess piece's movements.
     * @throws IllegalStateException if the past chess position (i.e. the position clicked before the current one) doesn't have a chess piece.
     * @param clickedPosition represents the current clicked position.
     * @param chessPositions all chess positions on the chess board.
     */
    private void processNormalMovement(ChessPosition clickedPosition, ChessPosition[] chessPositions) {
        // If, somehow, a empty square is clicked twice.
        if (pastChessPosition == null) {
            return;
        } // if
        if (pastChessPosition.getChessPiece() == null) {
            throw new IllegalStateException("This should literally be impossible and if it is then QChess has a bug");
        } // if

        ChessPiece pastChessPiece = pastChessPosition.getChessPiece();

        // Draw handling
        if (chessBoard.drawAllowed) {
            Team pastChessPositionTeam = pastChessPiece.getTeam();
            numMovesToFiftyMoveRuleDraw++;
            if (pastChessPiece.canResetNumMovesToDraw()) {
                numMovesToFiftyMoveRuleDraw = 0;
            } // if
            if (numMovesToFiftyMoveRuleDraw == 50) {
                chessBoard.fireEvent(new DrawEvent(pastChessPiece));
            } // if

            numMoves++;
            switch (pastChessPositionTeam) {
                case BLACK:
                    drawByRepetitionStorageBlack[numMoves % individualRepetitionToDraw] = clickedPosition;
                    break;
                case WHITE:
                    drawByRepetitionStorageWhite[numMoves % individualRepetitionToDraw] = clickedPosition;
                    break;
            } // switch
            evaluateRepetitionDraw(pastChessPosition.getChessPiece());
        } // if


        Class<? extends ChessPiece> chessPieceClass = pastChessPosition.getChessPiece().getClass();
        removeSpecialPieces();

        int pastBtnId = pastChessPosition.coordinate.getBtnID();
        int currentBtnId = clickedPosition.coordinate.getBtnID();
        boolean isPieceTwoAway = Math.abs(pastBtnId - currentBtnId) == 16;

        // Hard coded piece
        if (chessPieceClass.equals(Pawn.class) && isPieceTwoAway) {
            createEnpassantPiece(chessPositions);
        } // if

        // Castling
        if (pastChessPiece instanceof Castlable castlablePiece && chessBoard.castlingAllowed) {
            if (!castlablePiece.hasCastled() && pastChessPiece.isOnStart() && !pastChessPiece.hasMoved()) {
                castle(castlablePiece, pastChessPiece, clickedPosition, chessPositions);

                if (castlablePiece.hasCastled()) {
                    return;
                } // if
            } // if
        } // if

        move(pastChessPosition, clickedPosition);
    }

    /**
     * Evaluates draw by repetition. If players repeat moves {@code numRepetitionToDraw} number of times,
     * a draw event should be fired.
     *
     * <b>WARNING -> UNTESTED</b>
     *
     * @param chessPiece chess piece of the past clicked position.
     */
    private void evaluateRepetitionDraw(ChessPiece chessPiece) {
        boolean blackRepeatedToDraw = countPosInArr(drawByRepetitionStorageBlack) == numRepetitionToDraw;
        boolean whiteRepeatedToDraw = countPosInArr(drawByRepetitionStorageWhite) == numRepetitionToDraw;

        if (blackRepeatedToDraw && whiteRepeatedToDraw) {
            chessBoard.fireEvent(new DrawEvent(chessPiece));
        } // if
    }

    /**
     * Counts the amount of the first element in a given array.
     * {@see qchess.chess.logic.ChessPosition}
     * @param arr array to have its first element counted.
     * @return number of times the first element appears in the array.
     */
    private int countPosInArr(ChessPosition[] arr) {
        if (arr[0] == null) {
            return 0;
        } // if

        int num = 0;
        for (ChessPosition pos : arr) {
            if (pos == null){
                return 0;
            } // if

            if (arr[0].coordinate.equals(pos.coordinate)) {
                num++;
            } // if
        } // for

        return num;
    }

    /**
     * Castles a chess piece.
     * @param castlablePiece Chess piece which can castle (is instance of castlable).
     * @param pastChessPiece Past chess piece.
     * @param clickedPosition represents the current clicked position.
     * @param chessPositions all chess positions on the chess board.
     */
    private void castle(Castlable castlablePiece, ChessPiece pastChessPiece, ChessPosition clickedPosition, ChessPosition[] chessPositions) {
        HashMap<PieceScalar, CastleVector> castleDirections = (castlablePiece.getInitializedCastleDirections());

        for (Map.Entry<PieceScalar, CastleVector> entry : castleDirections.entrySet()) {

            PieceScalar scalar = entry.getKey();
            CastleVector vector = entry.getValue();
            int vectorTerminalPointBtnID = vector.getTerminalPoint().getBtnID();
            if (!canCastleMap.get(vector.getName())) {
                continue;
            }
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            for (Coordinate coord : scalar) {
                coordinates.add(coord);
            } // for

            ChessPiece terminalPiece = chessPositions[vectorTerminalPointBtnID].getChessPiece();
            boolean castleDependentWasClicked = clickedPosition.coordinate.equals(vector.getTerminalPoint());
            boolean posDesignatedForCastlingWasClicked = coordinates.contains(clickedPosition.coordinate);

            if (!terminalPiece.hasMoved() && (posDesignatedForCastlingWasClicked || (altCastle && castleDependentWasClicked))) {
                disablePastPlayablePositions((ChessPiece) castlablePiece);
                int dependentToBtnID = vector.getDependentTo().getBtnID();
                int castlableToBtnID = vector.getCastlableTo().getBtnID();

                ChessPosition positionOfDependent = chessPositions[vectorTerminalPointBtnID];
                ChessPosition posWhereDependentGoes = chessPositions[dependentToBtnID];
                try {
                    move(positionOfDependent, posWhereDependentGoes, false);
                } catch (PieceInWayException e) {
                    Coordinate coordOfCoCastlingPiece = positionOfDependent.getChessPiece().getCoordinate();
                    // If there is any piece in the way then it is the castlable.
                    if (!coordOfCoCastlingPiece.equals(vector.getDependentTo())) {
                        switchPlaces(posWhereDependentGoes, positionOfDependent);
                    } // if

                    ChessPosition dependentToPos = chessPositions[dependentToBtnID];
                    if (dependentToPos.getChessPiece() == null) {
                        move(chessPositions[dependentToBtnID], dependentToPos, false);
                    } // if
                } // catch

                try {
                    move(chessPositions[pastChessPiece.getBtnID()], chessPositions[castlableToBtnID], false);
                } catch (PieceInWayException e) {
                    if (!pastChessPiece.getCoordinate().equals(vector.getCastlableTo())) {
                        switchPlaces(chessPositions[castlableToBtnID], chessPositions[pastChessPiece.getBtnID()]);
                    } // if

                    ChessPosition castlableToPos = chessPositions[castlableToBtnID];
                    if (castlableToPos.getChessPiece() == null) {
                        move(pastChessPosition, castlableToPos, false);
                    } // if
                } // catch

                if (pastChessPiece.getTeam() == Team.BLACK) {
                    numFullMoves++;
                } // if

                // ensures only one movement event call
                chessBoard.fireEvent(new MovementEvent(pastChessPiece));
                chessBoard.enableChessPieces();

                chessBoard.fireEvent(new CastleEvent(pastChessPiece, terminalPiece));
                castlablePiece.setHasCastled(true);
            } // if
        } // for-each
    }

    /**
     * Handles prerequisites for a chess piece to capture another chess piece.
     * {@see qchess.chess.create.special.Enpassant}
     * @param clickedPosition represents the current clicked position.
     */
    private void processCapture(ChessPosition clickedPosition) {
        ChessPiece movedChessPiece = pastChessPosition.getChessPiece();
        boolean hasChessPiece = movedChessPiece != null;
        if (chessBoard.drawAllowed) {
            numMovesToFiftyMoveRuleDraw = 0;
        } // if

        // Theoretically impossible for this conditional to be false. It is here just to be safe
        if (hasChessPiece) {
            ChessPiece capturedPiece = clickedPosition.getChessPiece();

            /*
            Enpassantable is useless here and this is still essentially a hard-coded
            pawn here. Enpassantable will be useful in the future but for now, using it might
            do more harm than good.
             */
            if (movedChessPiece instanceof SpecifyCapture && movedChessPiece instanceof Enpassantable) {
                List<ChessDirection> capturableSquares = ChessAnnotation.applyAnnotations(movedChessPiece, MovesType.SPECIFY_CAPTURE);

                /*
                Scans capturable squares to verify that the captured piece was a special piece.
                 */
                for (ChessDirection vector : capturableSquares) {
                    if (vector.contains(clickedPosition.coordinate)) {
                        Coordinate coord = capturedPiece.getCoordinate();
                        if (capturedPiece instanceof SpecialPiece) {
                            int deltaRow = ((SpecialPiece) capturedPiece).deltaRow();
                            int deltaCol = ((SpecialPiece) capturedPiece).deltaCol();

                            int momentum = capturedPiece.getTeam() == Team.WHITE ? 1 : -1;

                            int newRow = coord.getRow() + deltaRow * momentum;
                            int newCol = coord.getCol() + deltaCol * momentum;
                            coord = new Coordinate(newRow, newCol);
                            ChessPosition newPosition = chessBoard.chessPositions[coord.getBtnID()];
                            remove(newPosition, true);

                            chessBoard.fireEvent(new EnpassantEvent(clickedPosition.getChessPiece()));
                        } // if
                        capture(pastChessPosition, clickedPosition);
                    } // if
                } // for
            } else {
                capture(pastChessPosition, clickedPosition);
            } // else

            removeSpecialPieces();
        } // if
    }

    /**
     * Processes the vectors or scalars from a chess piece's playables to produce
     * all attack squares and edit meta information such as {@code pastChessPositions}.
     * Past chess positions within the method represents the current piece's <i>possible</i> playable moves.
     * Outside the method, it represents the past positions which were enabled.
     * @param chessPiece The chess piece of the current clickedPosition.
     * @param clickedPosition represents the current clicked position.
     * @param chessPositions all chess positions on the chess board.
     * @param canSetAttack whether the call can set where the pieces attack.
     * @param isFromClick Whether the call is the result of a click.
     * @return The positions which are attacked by the {@code chessPiece}.
     */
    List<ChessPosition> playableSquaresRefinery(ChessPiece chessPiece, ChessPosition clickedPosition, ChessPosition[] chessPositions, boolean canSetAttack, boolean isFromClick) {
        boolean pieceIsPinning = false;
        for (Information info : pinInformation) {
            if (chessPiece.equals(info.instigator())) {
                pieceIsPinning = true;
                break;
            } // if
        } // for

        totnum++;

        /*
        The purpose of this memoization is to store values of already clicked chess pieces. If a pawn were clicked several times, it becomes
        memoized.

        The memoization has no real affect if the game is in a state of constant movement because each move causes a
        massive change on the chess board that the memoization algorithm currently can't account for.
         */
        if (memoizedPastPositions.containsKey(chessPiece) && isFromClick && !pieceIsPinning && !(chessPiece instanceof Castlable) && !chessBoard.pieceInCheck) {

            pastChessPosition = clickedPosition;

            if (canSetAttack) {
                setAttackedPositions(chessPiece, memoizedAttacks.get(chessPiece), true);
            } // if

            enableRefinedPlayableSquares(chessPiece, memoizedPastPositions.get(chessPiece), true);

            return memoizedAttacks.get(chessPiece);
        } // if

        num++;

        ArrayList<ChessPosition> attackedPositions = new ArrayList<>();

        numMovesAvailable = 0;

        if (isFromClick) {
            pastChessPosition = clickedPosition;
        } // if
        pastPositions = new ArrayList<>();

        memoizedAttacks.put(chessPiece, attackedPositions);
        memoizedPastPositions.put(chessPiece, pastPositions);

        List<ChessDirection> pieceVectors = new ArrayList<>(new LinkedHashSet<>(chessPiece.getPlayableDirections()));

        if (clickedPosition.getChessPiece() != null) {
            // Castling
            if (chessPiece instanceof Castlable castlablePiece && !chessBoard.pieceInCheck && chessBoard.castlingAllowed) {
                determineCastlingMoves(chessPiece, castlablePiece, chessPositions);
            } // if

            // Specific attack vectors
            if (chessPiece instanceof SpecifyCapture) {
                determineSpecificCaptureMoves(chessPiece, chessPositions, attackedPositions);
            } // if

            // General vectors
            determineGeneralMoves(chessPiece, chessPositions, pieceVectors, attackedPositions);
        } // if

        if (canSetAttack) {
            setAttackedPositions(chessPiece, attackedPositions, isFromClick);
        } // if

        enableRefinedPlayableSquares(chessPiece, pastPositions, isFromClick);

        chessPieceNumMovesMap.put(chessPiece, numMovesAvailable);

        return attackedPositions;
    }

    /**
     * Adds the playables to castle if they can be added.
     * @param chessPiece The chess piece of the current clickedPosition.
     * @param castlablePiece the Castlable which needs its castle related playables determined.
     * @param chessPositions all chess positions on the chess board.
     */
    private void determineCastlingMoves(ChessPiece chessPiece, Castlable castlablePiece, ChessPosition[] chessPositions) {

        HashMap<PieceScalar, CastleVector> castleDirections = castlablePiece.getInitializedCastleDirections();

        castleDirections.forEach((scalar, castleVector) -> {
            boolean canCastle = false;
            boolean castlePossible = false;


            if (!castlablePiece.hasCastled() && !chessPiece.hasMoved()) {
                ChessPiece terminalPiece;
                try {
                    terminalPiece = chessPositions[castleVector.getTerminalPoint().getBtnID()].getChessPiece();
                } catch (NoSuchElementException nsee) {

                    terminalPiece = null;
                } // try-catch



                boolean terminalPieceConditionals = terminalPiece != null && !terminalPiece.hasMoved();
                if (terminalPieceConditionals) {

                    castlePossible = true;

                    boolean vectorLineSegmentEmpty = false;

                    for (Coordinate coordinate : castleVector) {
                        if (chessPiece instanceof Checkable && chessBoard.checkAllowed) {
                            ChessPosition posOfFocus = chessBoard.chessPositions[coordinate.getBtnID()];
                            if (posOfFocus != null && posOfFocus.isAttacked()) {
                                if (!terminalPiece.equals(posOfFocus.getChessPiece())) {
                                    break;
                                } // if
                            } // if
                        } // if
                        if (coordinate.equals(castleVector.getTerminalPoint())) {
                            vectorLineSegmentEmpty = true;
                            break;
                        } // if
                        if (chessPositions[coordinate.getBtnID()].getChessPiece() != null) {
                            break;
                        } // if
                    } // for

                    ChessPosition kingTo = chessBoard.chessPositions[castleVector.getCastlableTo().getBtnID()];
                    ChessPosition dependentTo = chessBoard.chessPositions[castleVector.getDependentTo().getBtnID()];
                    boolean kingToEmpty = kingTo.getChessPiece() == null || kingTo.getChessPiece().equals(terminalPiece) || kingTo.getChessPiece().equals(chessPiece);

                    boolean dependentToEmpty = dependentTo.getChessPiece() == null  || dependentTo.getChessPiece().equals(terminalPiece) || dependentTo.getChessPiece().equals(chessPiece);

                    if (castleVector.getCastleDependent() != null && kingToEmpty && dependentToEmpty && !kingTo.isAttacked()) {
                        if (vectorLineSegmentEmpty && castleVector.getCastleDependent().isOnStart()) {
                            for (Coordinate coordinate : scalar) {
                                canCastleMap.put(castleVector.getName(), true);
                                ChessPosition posOfCoord = chessPositions[coordinate.getBtnID()];
                                pastPositions.add(posOfCoord);
                            } // for
                            canCastle = true;
                        } // if
                    } // if
                } // if
            }
            canCastleMap.put(castleVector.getName(), canCastle);

            castleIsPossibleMap.put(castleVector.getName(), castlePossible);
        }); // for-each
    }

    /**
     * Scans capturable moves as defined by the {@code SpecifyCapture} to see if they are eligible to capture.
     * If they are then they are added to {@code pastChessPositions} to be displayed as a possible move.
     * @param chessPiece The chess piece of the current clickedPosition.
     * @param chessPositions all chess positions on the chess board.
     * @param attackedPositions chess positions which are attacked by the {@code chessPiece}.
     */
    private void determineSpecificCaptureMoves(ChessPiece chessPiece, ChessPosition[] chessPositions, ArrayList<ChessPosition> attackedPositions) {
        List<ChessDirection> capturableSquares = ChessAnnotation.applyAnnotations(chessPiece, MovesType.SPECIFY_CAPTURE);;
        for (ChessDirection direction : capturableSquares) {

            boolean pieceInWay = false;
            boolean checkablePieceInWay = false;
            boolean firstPotentialPin = false;
            ChessPiece possiblePin = null;

            int distFromOrigin = 0;
            for (Coordinate coord : direction) {
                distFromOrigin++;

                ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                boolean posHasChessPiece = coordinatePiece != null;

                if (chessPiece instanceof Checkable && chessBoard.checkAllowed) {
                    if (isChessPieceInDanger(chessPiece, posOfCoord)) {
                        break;
                    } // if
                } // if

                if (!pieceInWay || checkablePieceInWay) {
                    attackedPositions.add(posOfCoord);
                } // if

                if (!firstPotentialPin && posHasChessPiece) {
                    if (!pieceInWay) {
                        pastPositions.add(posOfCoord);
                    } // if
                } // if

                if (posHasChessPiece) {
                    boolean chessPieceIsSpecialPiece = coordinatePiece instanceof SpecialPiece;
                    boolean chessPieceIsCheckable = coordinatePiece instanceof Checkable;

                    if (firstPotentialPin && chessPieceIsCheckable) {
                        pinnerPinnedStorage.add(new PinnerPinnedCheckable(chessPiece, possiblePin, coordinatePiece));
                    } // if

                    if (chessPiece.getTeam() != coordinatePiece.getTeam()) {
                        if (coordinatePiece instanceof Checkable && chessBoard.checkAllowed) {
                            if (firstPotentialPin) {
                                PinInformation info = new PinInformation (
                                        ++distFromOrigin, direction, coordinatePiece, chessPiece
                                );

                                pinInformation.add(info);
                            } else {
                                CheckInformation info = new CheckInformation (
                                        ++distFromOrigin, direction, coordinatePiece, chessPiece
                                );

                                checkInformation.add(info);
                            } // else
                        } // if
                    } // if

                    if (!chessPieceIsSpecialPiece) {
                        if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {

                            if (chessPieceIsCheckable && !pieceInWay) {
                                checkablePieceInWay = true;
                            } // if

                            pieceInWay = true;

                            if (!chessPieceIsCheckable && firstPotentialPin) {
                                break;
                            } // if
                        } // if

                        if (!firstPotentialPin) {
                            possiblePin = coordinatePiece;
                            firstPotentialPin = true;
                        } // if
                    } // if
                } // if
            } // for-each
        } // for-each
    }

    /**
     * Filters chess moves for ones which pass chess rules and adds them to {@code pastChessPositions}.
     * It also adds attacked positions to {@code attackedPositions}.
     * @param chessPiece The chess piece of the current clickedPosition.
     * @param chessPositions all chess positions on the chess board.
     * @param chessDirections Vectors or scalars which specify a chess piece's movement pattern.
     * @param attackedPositions all positions which are attacked by chess pieces in the current chess board state.
     */
    private void determineGeneralMoves(ChessPiece chessPiece, ChessPosition[] chessPositions, List<ChessDirection> chessDirections, List<ChessPosition> attackedPositions) {
        for (ChessDirection direction : chessDirections) {
            boolean pieceInWay = false;
            boolean checkablePieceInWay = false;
            boolean firstPotentialPin = false;
            ChessPiece possiblePin = null;

            int distFromOrigin = 0;
            for (Coordinate coord : direction) {

                ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                boolean posHasChessPiece = coordinatePiece != null;

                if (chessPiece instanceof Checkable && chessBoard.checkAllowed) {
                    if (isChessPieceInDanger(chessPiece, posOfCoord)) {
                        break;
                    } // if
                } // if

                if (chessPiece instanceof SpecifyCapture) { // Fixes pawns showing what is directly in front of them
                    if (!posHasChessPiece) {
                        if (!pieceInWay && !firstPotentialPin) {
                            pastPositions.add(posOfCoord);
                        } // if
                    } // if
                } else {

                    if (!pieceInWay || (checkablePieceInWay)) {
                        attackedPositions.add(posOfCoord);
                    } // if

                    if (!firstPotentialPin) {
                        if (!pieceInWay) {
                            pastPositions.add(posOfCoord);
                        } // if
                    } // if
                } // else

                if (posHasChessPiece) {
                    boolean chessPieceIsSpecialPiece = coordinatePiece instanceof SpecialPiece;
                    boolean chessPieceIsCheckable = coordinatePiece instanceof Checkable;
                    boolean chessPieceIsCheckableAndDifTeam = coordinatePiece instanceof Checkable && chessPiece.getTeam() != coordinatePiece.getTeam();


                    if (firstPotentialPin && chessPieceIsCheckable) {
                        pinnerPinnedStorage.add(new PinnerPinnedCheckable(chessPiece, possiblePin, coordinatePiece));
                    } // if

                    // Coordinate sees a checkable piece
                    if (!(chessPiece instanceof SpecifyCapture) && chessPiece.getTeam() != coordinatePiece.getTeam()) {
                        if (coordinatePiece instanceof Checkable && chessBoard.checkAllowed) {
                            if (firstPotentialPin) {
                                PinInformation info = new PinInformation (
                                        ++distFromOrigin, direction, coordinatePiece, chessPiece
                                );

                                pinInformation.add(info);
                            } else {
                                CheckInformation info = new CheckInformation (
                                        ++distFromOrigin, direction, coordinatePiece, chessPiece
                                );

                                checkInformation.add(info);
                            } // else
                        } // if
                    } // if

                    if (!chessPieceIsSpecialPiece) {
                        if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {

                            if (chessPieceIsCheckable && !pieceInWay && coordinatePiece.getTeam() != chessPiece.getTeam()) {
                                checkablePieceInWay = true;
                            } // if

                            pieceInWay = true;

                            if (!chessPieceIsCheckable && firstPotentialPin) {
                                break;
                            } // if
                        } // if

                        if (!firstPotentialPin) {
                            possiblePin = coordinatePiece;
                            firstPotentialPin = true;
                        } // if
                    } // if
                } // if

                distFromOrigin++;
            }  // for-each
        }  // for-each
    }

    /**
     * Checks if the chess piece is in danger based on its coordinate.
     * @param chessPiece chess to check for danger.
     * @param posOfCoord position to check for danger.
     * @return true if the chess piece is in danger and false otherwise.
     */
    private boolean isChessPieceInDanger(ChessPiece chessPiece, ChessPosition posOfCoord) {
        boolean posOfCoordAttackedByOtherTeam = false;
        if (posOfCoord.isAttacked()) {
            for (ChessPiece attacker : posOfCoord.attackers) {
                if (attacker != null && attacker.getTeam() != chessPiece.getTeam()) {
                    posOfCoordAttackedByOtherTeam = true;
                    break;
                } // if
            } // for-each
        } // if

        return posOfCoordAttackedByOtherTeam;
    }

    /**
     * Sets chess positions which are in {@code attackedPositions} to a state of being attacked.
     * @param chessPiece piece to set positions it attacks.
     * @param attackedPositions all positions which are attacked for the chess piece.
     * @param isFromClick whether the method call is the result of a click or not.
     */
    private void setAttackedPositions(ChessPiece chessPiece, List<ChessPosition> attackedPositions, boolean isFromClick) {
        if (chessBoard.checkAllowed && !isFromClick && chessPiece.getTeam() != chessBoard.playerTeam) { // SET TO EQUALS

            for (ChessPosition pastChessPosition : attackedPositions) {

                if (chessPiece.getTeam() != chessBoard.playerTeam) {
                    pastChessPosition.attackers = new HashSet<>();
                    pastChessPosition.attackers.add(chessPiece);
                    pastChessPosition.setIsAttacked(true);
                } // if
            } // for-each
        } // if
    }

    /**
     * Enables the possible playables/capturables to be clicked.
     * @param chessPiece The chess piece of the current clickedPosition.
     * @param pastPositions all past positions relating to the chess piece.
     * @param isFromClick whether the method call is the result of a click or not.
     */
    private void enableRefinedPlayableSquares(ChessPiece chessPiece, List<ChessPosition> pastPositions, boolean isFromClick) {
        List<Coordinate> convertedPosToCoord = pastPositions.stream()
                .map(ChessPosition::getCoordinate)
                .toList();

        if (chessPiece.isPinned() && chessBoard.pinAllowed) {
            /*
            Use pinInformation and make another for-each loop to allow chess pieces to
            move along the pinning vector.
             */

            for (Information info : pinInformation) {
                pinPointMoves(chessPiece, convertedPosToCoord, info, info.direction().getDirectionFromOrigin(), isFromClick);
            } // for-each

            return;
        } // if

        if ((chessBoard.pieceInCheck) && !(chessPiece instanceof Checkable)) {
            ArrayList<Coordinate> intersect = new ArrayList<>(checkInformation.getFirst().direction().getDirectionFromOrigin());
            for (int i = 1; i < checkInformation.size(); i++) {
                intersect.retainAll(checkInformation.get(i).direction().getDirectionFromOrigin());
            } // for

            for (Information info : checkInformation) {
                pinPointMoves(chessPiece, convertedPosToCoord, info, intersect, isFromClick);
            } // for-each
            return;
        } // if

        if (chessPiece instanceof Checkable && chessBoard.checkAllowed) {
            for (ChessPosition chessPosition : pastPositions) {
                if (!chessPosition.isAttacked() && (chessPosition.getChessPiece() == null || (chessPosition.getChessPiece() != null && chessPosition.getChessPiece().getTeam() != chessPiece.getTeam()))) {
                    if (isFromClick) {
                        chessPosition.setDisable(false);
                    } // if
                    numMovesAvailable++;
                } // if

            } // for-each

            return;
        } // if

        for (ChessPosition chessPosition : pastPositions) {

            if (chessPosition.getChessPiece() == null || (chessPosition.getChessPiece() != null && chessPosition.getChessPiece().getTeam() != chessPiece.getTeam())) {
                if (isFromClick) {
                    chessPosition.setDisable(false);
                } // if
                numMovesAvailable++;
            } // if
        } // for-each
    }

    /**
     * Cross-references {@code possibleMovesWithCondition} with {@code convertedPosToCoord} to see display the moves that are in both.
     * @param chessPiece clicked chess piece.
     * @param convertedPosToCoord available chess positions converted to their respective coordinates.
     * @param info information regarding the pin/check involved.
     * @param possibleMovesWithCondition only moves which can be played given a active condition.
     * @param isFromClick true if this method call is produced from a click and false if not.
     */
    private void pinPointMoves(ChessPiece chessPiece, List<Coordinate> convertedPosToCoord, Information info, ArrayList<Coordinate> possibleMovesWithCondition, boolean isFromClick) {

        int distFromOrigin = 0;

        for (Coordinate coord : possibleMovesWithCondition) {
            if (convertedPosToCoord.contains(coord)) {
                if (info.distFromOrigin() >= distFromOrigin && info.victim().getTeam() == chessPiece.getTeam()) { // making equals fixes piece not being able to capture when piece in check

                    ChessPiece pieceInAttackVector = chessBoard.chessPositions[coord.getBtnID()].getChessPiece();

                    if (pieceInAttackVector != null) {
                        if (pieceInAttackVector.getTeam() != chessPiece.getTeam()) {
                            if (isFromClick) {
                                chessBoard.chessPositions[coord.getBtnID()].setDisable(false);
                            } // if
                            numMovesAvailable++;
                        } // if
                    } else {
                        if (isFromClick) {
                            chessBoard.chessPositions[coord.getBtnID()].setDisable(false);
                        } // if
                        numMovesAvailable++;
                    } // else

                } // if
            } // if

            distFromOrigin++;
        } // for-each

    }

    /**
     * Resets information relating to memoization, pinning and (chess) checking.
     */
    private void resetMetaData() {
        chessBoard.pieceInCheck = false;
        pinInformation.clear();
        checkInformation.clear();
        memoizedPastPositions.clear();
        memoizedAttacks.clear();
    }

    /**
     * Generates a enpassant piece on the board.
     * @param chessPositions all chess positions on the chess board.
     */
    private void createEnpassantPiece(ChessPosition[] chessPositions) {
        ChessPiece enpassPawn = pastChessPosition.getChessPiece();

        int momentum = enpassPawn.getTeam() == Team.WHITE ? 1 : -1;
        int newBtnId = enpassPawn.getBtnID() + ChessBoard.width * momentum;
        ChessPosition onePlaceUnderPawn = chessPositions[newBtnId];
        Coordinate onePlaceUnder = new Coordinate(newBtnId);

        Enpassant enpassant = new Enpassant(onePlaceUnder, enpassPawn.getTeam());
        this.enpassantInfo = new EnpassantInfo(enpassant.getCoordinate(), enpassPawn.getTeam());
        enpassant.setPosition(chessBoard.getChessPositions()[newBtnId]);
        chessBoard.allChessPieces.add(enpassant);
        onePlaceUnderPawn.setChessPiece(enpassant);
    }

    /**
     * Removes any chess pieces that extends SpecialPiece.
     */
    private void removeSpecialPieces() {
        enpassantInfo = null;
        for (int i = 0; i < chessBoard.allChessPieces.size(); i++) {
            ChessPiece chessPiece = chessBoard.allChessPieces.get(i);

            // Ensures that special piece removal doesn't have an impact on playables
            memoizedPastPositions.clear();

            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece instanceof SpecialPiece) {
                chessPosition.setDisable(true);
                chessPosition.setChessPiece(null);
                chessPosition.setText("");
                chessPieceNumMovesMap.remove(chessPiece);
                chessBoard.allChessPieces.remove(chessPiece);
                i--;
            } // if
        } // for
    }

    /**
     * Disables all past attacked moves relating to the chess piece.
     * -> When a chess piece moves, it no longer attacks the same positions it attacked before so it must be reset.
     * @param chessPiece chess piece to disable its enabled past attacked moves.
     */
    private void disablePastPositionsAttacked(ChessPiece chessPiece) {

        if (memoizedAttacks.containsKey(chessPiece)) { // past Pos
            for (ChessPosition pos : memoizedAttacks.get(chessPiece)) {

                boolean posNotNull = pos != null;

                if (posNotNull) {
                    boolean posHasChessPiece = pos.getChessPiece() != null;
                    boolean pieceTeamNotCurrentTeam = isPieceTeamCurrentTeam(pos, posHasChessPiece);

                    if (!posHasChessPiece || pieceTeamNotCurrentTeam) {
                        pos.attackers.remove(pastChessPosition.getChessPiece()); // Checkmate
                        if (pos.attackers.isEmpty()) {
                            pos.setIsAttacked(false);
                        } // if
                    } // if
                } // if
            } // for-each
        } // if

    }

    /**
     * Disables all past playable moves relating to the chess piece.
     * -> When a chess piece moves, it no longer plays the same positions it could play before so it must be reset.
     * @param chessPiece chess piece to disable its enabled past playable moves.
     */
    private void disablePastPlayablePositions(ChessPiece chessPiece) {

        if (memoizedPastPositions.containsKey(chessPiece)) { // used pastPositions

            for (ChessPosition pos : memoizedPastPositions.get(chessPiece)) {
                boolean posNotNull = pos != null;

                if (posNotNull) {
                    boolean posHasChessPiece = pos.getChessPiece() != null;
                    boolean pieceTeamNotCurrentTeam = isPieceTeamCurrentTeam(pos, posHasChessPiece);

                    if (!posHasChessPiece || pieceTeamNotCurrentTeam) {
                        pos.setDisable(true);
                    } // if
                } // if
            } // for-each
        } // if
    }

    /**
     * Checks if the {@code pos} has a chess piece which is of the same team as the current team on the chess board.
     * @param pos position to check.
     * @param posHasChessPiece whether the position has a piece or not
     * @return true if the pos has a chess piece which is the current team and false otherwise.
     */
    private boolean isPieceTeamCurrentTeam(ChessPosition pos, boolean posHasChessPiece) {
        boolean pieceTeamNotCurrentTeam;

        if (posHasChessPiece && pos.getChessPiece().getTeam() != null) {
            Team pieceTeam = pos.getChessPiece().getTeam();
            pieceTeamNotCurrentTeam = !pieceTeam.name().equals(chessBoard.playerTeam.name());
        } else {
            pieceTeamNotCurrentTeam = false;
        } // else

        return pieceTeamNotCurrentTeam;
    }

    /**
     * Switches chess pieces at {@code pastPost} and {@code futurePos}.
     * @param pastPos position which the chess piece is currently.
     * @param futurePos position which the chess piece will move to.
     */
    public void switchPlaces(ChessPosition pastPos, ChessPosition futurePos) {
        if (futurePos.getChessPiece() == null || pastPos.getChessPiece() == null) {
            throw new IncompleteSwitchPositionsException("pastPost and futurePos must have chess pieces to switch them.");
        }
        ChessPiece otherPiece = pastPos.getChessPiece();
        ChessPiece tempPiece = futurePos.getChessPiece();

        processGameEnd(otherPiece, futurePos);

        futurePos.setChessPiece(otherPiece);
        futurePos.setGraphic(otherPiece.getGraphic());
        otherPiece.setPosition(futurePos);
        otherPiece.setCoordinate(futurePos.getCoordinate());
        otherPiece.wasMoved();

        processGameEnd(tempPiece, pastPos);

        pastPos.setChessPiece(tempPiece);
        pastPos.setGraphic(tempPiece.getGraphic());
        tempPiece.setPosition(pastPos);
        tempPiece.setCoordinate(pastPos.getCoordinate());
        tempPiece.wasMoved();

        // no movement event
    }

    /**
     * Moves a chess piece from {@code pastPos} to {@code futurePos}.
     * @throws PieceInWayException if {@code futurePos} has a chess piece.
     * @param pastPos position which the chess piece is currently.
     * @param futurePos position which the chess piece will move to.
     * @param callFiresMovementEvent true if the method call fires a movement event and false if not.
     */
    private void move(ChessPosition pastPos, ChessPosition futurePos, boolean callFiresMovementEvent) {
        if (pastPos.equals(futurePos)) {
            return;
        } // if
        if (futurePos.getChessPiece() != null) {
            throw new PieceInWayException("Movement square is occupied, try capture() instead.", futurePos.getChessPiece());
        } // if

        ChessPiece chessPiece = pastPos.getChessPiece();

        futurePos.setChessPiece(chessPiece);
        if (chessPiece.getGraphic() == null) {
            futurePos.setText(chessPiece.getName());
        } else {
            futurePos.setText("");
            futurePos.setGraphic(chessPiece.getGraphic());
        } // else

        if (futurePos.getChessPiece().getTeam() == chessBoard.playerTeam) {
            futurePos.setDisable(false);
        } // if

        chessPiece.setPosition(futurePos);
        chessPiece.setCoordinate(futurePos.coordinate);
        chessPiece.wasMoved();

        remove(pastPos, false);

        // PROMOTION
        if (chessPiece instanceof Promotable promotablePiece && chessBoard.promotionAllowed) {
            handlePromotion(promotablePiece);
        } // if

        // CHECK
        if (chessBoard.checkAllowed && !chessBoard.singleTeam) {
            // All attacked squares are no longer attacked
            clearAllAttackers();

            // CHECK
            for (ChessPiece oneOfAllChessPieces : chessBoard.allChessPieces) {
                addAttackersToNewPos(chessPiece.getTeam(), oneOfAllChessPieces, pastPos, futurePos);
            } // for-each
        } // if

        // Check
        if (!(chessPiece instanceof Checkable) && !chessBoard.singleTeam) {
            processGameEnd(chessPiece, futurePos); //Analysis of attacks HERE
        } // if

        // Pin
        if (chessBoard.pinAllowed && !chessBoard.singleTeam) {
            evaluatePin();
        } // if

        // Castling calls move twice: the if statement ensures only 1 movement event is created
        if (chessPiece.getTeam() == chessBoard.playerTeam && callFiresMovementEvent) {
            if (chessPiece.getTeam() == Team.BLACK) {
                numFullMoves++;
            } // if
            chessBoard.fireEvent(new MovementEvent(chessPiece));
        } // if

        chessBoard.enableChessPieces();
    }

    /**
     * Moves a chess piece from {@code pastPos} to {@code futurePos}.
     * @throws PieceInWayException if {@code futurePos} has a chess piece.
     * @param pastPos position which the chess piece is currently.
     * @param futurePos position which the chess piece will move to.
     */
    public void move(ChessPosition pastPos, ChessPosition futurePos) {
        this.move(pastPos, futurePos, true);
    }

    /**
     * Clears all attackers from the chess board.
     */
    void clearAllAttackers() {
        for (ChessPosition pos : chessBoard.chessPositions) {
            pos.attackers.clear();
            pos.setIsAttacked(false);
        } // for-each
    }

    /**
     * Adds attackers to their new positions and removes attackers from their old ones.
     * @param chessPieceTeam team of the chess piece which was clicked.
     * @param oneOfAllChessPieces one of all chess pieces on the chess board.
     * @param pastPos past position where the attackers were.
     * @param futurePos position where the attackers move to.
     */
    void addAttackersToNewPos(Team chessPieceTeam, ChessPiece oneOfAllChessPieces, ChessPosition pastPos, ChessPosition futurePos) {
        /*
        Attackers only ever store 1 chess piece. I have left it as an array list in case I want to change that
        in the future.
         */
        if (chessPieceTeam == oneOfAllChessPieces.getTeam() && !chessBoard.singleTeam) {
            for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, pastPos, chessBoard.chessPositions, false, false)) {
                pos.attackers.remove(oneOfAllChessPieces);
                pos.setIsAttacked(false);
            } // for-each

            addAttackers(oneOfAllChessPieces, futurePos);
        } // if
    }

    /**
     * Adds attackers to a given position
     * @param oneOfAllChessPieces one of all chess pieces on the chess board.
     * @param futurePos position to add attackers to.
     */
    public void addAttackers(ChessPiece oneOfAllChessPieces, ChessPosition futurePos) {
        for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, futurePos, chessBoard.chessPositions, true, false)) {
            pos.attackers.add(oneOfAllChessPieces);
            pos.setIsAttacked(true);
        } // for-each
    }

    /**
     * Scans the given chess piece for any (chess) checks based on the position provided.
     * This method essentially uses the chess piece's playables and attaches them to the given position to
     * check if there are any (chess) checks which are in its capturables.
     * @param chessPiece piece which may or may not be giving a (chess) check.
     * @param futurePos position at which to scan the chess piece for check.
     * @return true if check occurred and false otherwise.
     */
    public boolean scanForCheck(ChessPiece chessPiece, ChessPosition futurePos) {
        for (Map.Entry<ChessPiece, ChessPiece> dscverdChessPiece : discoveredCheckMap.entrySet()) {
            if (dscverdChessPiece.getKey().equals(chessPiece)) {
                ChessPiece pinner = dscverdChessPiece.getValue();
                scanForCheck(pinner, pinner.getPosition());
            } // if
        } // for-each

        for (ChessPosition position : playableSquaresRefinery(chessPiece, futurePos, chessBoard.chessPositions, true, false)) {

            if (position.getChessPiece() instanceof Checkable && position.getChessPiece().getTeam() != chessPiece.getTeam()) {
                chessBoard.fireEvent(new CheckEvent(position.getChessPiece()));
                return true;
            } // if
        } // for-each

        return false;
    }

    /**
     * Checks if the conditions for a game-ending event are met and fires that event if so.
     * @param chessPiece chess piece to check for a game ending event.
     * @param futurePos position which is checked for a game ending event.
     */
    public void processGameEnd(ChessPiece chessPiece, ChessPosition futurePos) {
        scanForCheck(chessPiece, futurePos);

        for (ChessPiece oneOfAllChessPieces : chessBoard.allChessPieces) {
            playableSquaresRefinery(oneOfAllChessPieces, oneOfAllChessPieces.getPosition(), chessBoard.chessPositions, false, false);
        } // for-each

        for (ChessPiece oneOfAllChessPieces : chessBoard.allChessPieces) {
            playableSquaresRefinery(oneOfAllChessPieces, oneOfAllChessPieces.getPosition(), chessBoard.chessPositions, false, false);
        } // for-each


        int totalMoves = 0;
        for (Map.Entry<ChessPiece, Integer> numMovesEntry : chessPieceNumMovesMap.entrySet()) {
            if (numMovesEntry.getKey().getTeam() != chessPiece.getTeam()) {
                totalMoves += numMovesEntry.getValue();
            } // if
        } // for-each

        if (chessBoard.pieceInCheck && chessBoard.checkMateAllowed && totalMoves == 0) {
            pinInformation.clear();
            checkInformation.clear();

            chessBoard.fireEvent(new CheckMateEvent(chessPiece));

        } else if (totalMoves == 0) {
            chessBoard.fireEvent(new DrawEvent(chessPiece));
        } // else

    }

    /**
     * Scans through the {@code pinnerPinnedMap} to evaluate currently active pins on the chess board.
     */
    public void evaluatePin() {
        for (PinnerPinnedCheckable pPC : pinnerPinnedStorage) {
            ChessPiece pinner = pPC.pinner();
            ChessPiece pinned = pPC.pinned();
            ChessPiece checkable = pPC.checkable();

            if (checkable.getTeam() != pinner.getTeam()) {
                if (pinner.getTeam() != pinned.getTeam()) {
                    processPin(pinner, pinned);
                } else { // Handling discovered check
                    discoveredCheckMap.put(pinned, pinner);
                } // else
            } // if
        } // for-each

        /*
        for (Map.Entry<ChessPiece, ChessPiece> pinnerPinEntry : pinnerPinnedMap.entrySet()) {
            ChessPiece instigator = pinnerPinEntry.getKey();
            ChessPiece victim = pinnerPinEntry.getValue();

            processPin(instigator, victim);
        } // for-each

         */
    }

    /**
     * Updates the pins in the chess board's present state.
     * If a pin is invalid - no longer blocking an attack on a checkable - then the victim is no longer victim.
     *
     * @param pinner chess piece which pins.
     * @param pinned chess piece which is victim.
     */
    void processPin(ChessPiece pinner, ChessPiece pinned) {
        List<ChessDirection> directions;
        if (pinner instanceof SpecifyCapture spCaPinner) {
            directions = spCaPinner.getCapturableMoves();
        } else {
            directions = pinner.getPlayableDirections();
        } // else


        for (ChessDirection direction : directions) {
            boolean possiblePin = false;
            ChessPiece mayBePinned = null;
            ChessPiece verifiedPin = null;

            for (Coordinate coord : direction) {
                boolean posHasPiece = chessBoard.chessPositions[coord.getBtnID()].getChessPiece() != null;
                if (posHasPiece) {
                    ChessPiece pieceInVector = chessBoard.chessPositions[coord.getBtnID()].getChessPiece();
                    if (pieceInVector.getTeam() != pinner.getTeam()) {
                        if (possiblePin) {
                            if (pieceInVector instanceof Checkable) {
                                verifiedPin = mayBePinned;
                            } // if
                            break;

                        } // if

                        possiblePin = true;
                        mayBePinned = pieceInVector;

                    } // if
                } // if
            } // for-each

            if (verifiedPin != null && verifiedPin.equals(pinned)) {
                pinned.setPinned(true);
                break;
            } else {
                pinned.setPinned(false);
            } // else
        } // for-each
    }

    /**
     * Checks for conditions to promote and if they are met, fires a promotion event.
     * @param promotablePiece promotable version of {@code chessPiece}.
     */
    void handlePromotion(Promotable promotablePiece) {
        ChessPiece chessPiece = (ChessPiece) promotablePiece;

        int rowToPromote;
        int colToPromote;
        if (chessPiece.getTeam() == Team.BLACK) {
            rowToPromote = promotablePiece.getBlackPromotionSquares().row();
            colToPromote = promotablePiece.getBlackPromotionSquares().col();
        } else {
            rowToPromote = promotablePiece.getWhitePromotionSquares().row();
            colToPromote = promotablePiece.getWhitePromotionSquares().col();
        } // else
        boolean rowAbsent = rowToPromote == -1;
        boolean colAbsent = colToPromote == -1;

        if (!rowAbsent && !colAbsent) {
            boolean pieceRowOnPromote = chessPiece.getCoordinate().getRow() == rowToPromote;
            boolean pieceColOnPromote = chessPiece.getCoordinate().getCol() == colToPromote;
            if (pieceRowOnPromote && pieceColOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(chessPiece, promotablePiece, this));
            } // if
        } else if (rowAbsent) {
            boolean pieceColOnPromote = chessPiece.getCoordinate().getCol() == colToPromote;
            if (pieceColOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(chessPiece, promotablePiece, this));
            } // if
        } else {
            boolean pieceRowOnPromote = chessPiece.getCoordinate().getRow() == rowToPromote;
            if (pieceRowOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(chessPiece, promotablePiece, this));
            } // if
        } // else
    }

    /**
     * Captures a chess piece on {@code futurePos} from {@code pastPos}.
     * @throws NothingToCaptureException if {@code futurePos} doesn't have a chess piece.
     * @param pastPos position which the chess piece is currently.
     * @param futurePos position which the chess piece will capture on.
     */
    public void capture(ChessPosition pastPos, ChessPosition futurePos) {
        if (futurePos.getChessPiece() == null) {
            throw new NothingToCaptureException("nothing to capture");
        } // if
        ChessPiece capturedPiece = futurePos.getChessPiece();
        ChessPiece instigator = pastPos.getChessPiece();

        //CheckMate
        if (chessBoard.checkAllowed) {
            for (ChessPosition piecePlayables : playableSquaresRefinery(instigator, pastPos, chessBoard.chessPositions, false, false)) {
                piecePlayables.attackers.remove(instigator);
            } // for-each
        } // if

        remove(futurePos, true);
        move(pastPos, futurePos);

        boolean capturedPieceIsSpecial = capturedPiece instanceof SpecialPiece;
        if (!capturedPieceIsSpecial) {
            chessBoard.fireEvent(new CaptureEvent(instigator, capturedPiece));
        } // if
    }

    /**
     * Removes a chess piece at {@code pos}.
     * @param pos position to remove a chess piece.
     * @param capture boolean whether the removal relates to a capture.
     */
    public void remove(ChessPosition pos, boolean capture) {
        if (capture) {
            chessBoard.allChessPieces.remove(pos.chessPiece);

            //CheckMate
            for (ChessPosition piecePlayables : playableSquaresRefinery(pos.chessPiece, pos, chessBoard.chessPositions, false, false)) {
                piecePlayables.attackers.remove(pos.chessPiece);
            } // for-each
        } // if

        chessPieceNumMovesMap.remove(pos.chessPiece);

        pos.setText("");
        pos.setChessPiece(null);
        pos.setGraphic(null);
        pos.setDisable(true);
    }

    /**
     * @return the amount of moves until a draw occurs via fifty move rule.
     */
    public int getNumMovesToFiftyMoveDraw() {
        return numMovesToFiftyMoveRuleDraw;
    }

    /**
     * @return the total number of movements that occurred.
     */
    public int getTotalNumHalfMoves() {
        return numMoves;
    }

    /**
     * <p>
     * A getter method for the full movements on for a move logic. A full movement is incremented
     * when black moves. A single full movement represents a move of both black and white teams.
     * </p>
     * <br>
     * Starts at 0.
     * @return the total number of full movements that occurred.
     */
    public int getTotalNumFullMoves() {
        return numFullMoves;
    }

    /**
     * @return the information related to the enpassant piece if it is on the board and null if not.
     */
    public EnpassantInfo getEnpassantInfo() {
        return enpassantInfo;
    }

    /**
     * Checks if a piece is special or not.
     * @param chessPiece chess piece to check.
     * @return true if the chess piece is special and false if not.
     */
    public static boolean isSpecialPiece(ChessPiece chessPiece) {
        return chessPiece instanceof SpecialPiece;
    }

    /**
     * Gets boolean castling abilities from castle vectors for all chess pieces.
     * @return a hashmap with the name of the vector as the key and a boolean which is true when castling is possible on the castle vector.
     */
    public HashMap<String, Boolean> getCastleInformation() {
        return castleIsPossibleMap;
    }
}
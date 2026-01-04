package qchess.chess.logic;

import qchess.chess.chessmen.Pawn;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.interfaces.*;
import qchess.chess.create.piecemodifiers.Xray;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.special.Enpassant;
import qchess.chess.logic.event.*;

import java.util.*;

public class MoveLogic extends ChessLogic {
    public ArrayList<ChessPosition> pastPositions;
    public ChessPosition pastChessPosition;
    private final HashMap<Integer, ChessDirection> distFrmOriAttackDirectionMap;
    private final HashMap<ChessPiece, ChessPiece> pinnerPinnedMap;
    private final HashMap<ChessPiece, List<ChessPosition>> memoizedPastPositions;
    private final HashMap<ChessPiece, List<ChessPosition>> memoizedAttacks;

    public int numMoves;

    public MoveLogic(ChessBoard chessBoard) {
        super(chessBoard);

        distFrmOriAttackDirectionMap = new HashMap<>();
        pinnerPinnedMap = new HashMap<>();
        memoizedPastPositions = new HashMap<>();
        memoizedAttacks  = new HashMap<>();
    }

     void positionClick(ChessPosition clickedPosition) {
        ChessPiece chessPiece = clickedPosition.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement
        if (chessPiece == null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            disablePastPlayablePositions(pastChessPiece);
            disablePastPositionsAttacked(pastChessPiece);

            resetMetaData();

            processNormalMovement(clickedPosition, chessPositions);
            return;
        }

        // Capturing
        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();

            if (pastChessPiece != null && (pastChessPiece.getTeam() != chessPiece.getTeam() || chessPiece instanceof SpecialPiece)) {
                disablePastPlayablePositions(pastChessPiece);
                disablePastPositionsAttacked(pastChessPiece);

                resetMetaData();

                processCapture(clickedPosition);
                return;
            }
        }

        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            disablePastPlayablePositions(pastChessPiece);
        }

        // Playable Squares Refiner
        playableSquaresRefinery(chessPiece, clickedPosition, chessPositions, true);
    }

    private void processNormalMovement(ChessPosition clickedPosition, ChessPosition[] chessPositions) {
        if (pastChessPosition == null) {
            return;
        }

        Class<? extends ChessPiece> chessPieceClass = pastChessPosition.getChessPiece().getClass();

        ChessPiece pastChessPiece = pastChessPosition.getChessPiece();

        removeSpecialPieces();


        int pastBtnId = pastChessPosition.coordinate.getBtnID();
        int currentBtnId = clickedPosition.coordinate.getBtnID();
        boolean isPieceTwoAway = Math.abs(pastBtnId - currentBtnId) == 16;

        // Hard coded piece
        if (chessPieceClass.equals(Pawn.class) && isPieceTwoAway) {
            createEnpassantPiece(chessPositions);
        }

        // Castling
        if (pastChessPiece instanceof Castlable castlablePiece && chessBoard.castlingAllowed) {
            if (!castlablePiece.hasCastled() && pastChessPiece.isOnStart() && !pastChessPiece.hasMoved()) {
                HashMap<PieceScalar, CastleVector> castleDirections = (castlablePiece.getInitializedCastleDirections());
                castleDirections.forEach((scalar, vector) -> {
                    for (Coordinate coordinate : scalar) {
                        ChessPiece terminalPiece = chessPositions[vector.getTerminalPoint().getBtnID()].getChessPiece();
                        if (clickedPosition.coordinate.equals(coordinate) && !terminalPiece.hasMoved()) {
                            move(chessPositions[vector.getTerminalPoint().getBtnID()], chessPositions[vector.getInitialPoint().getBtnID()]);
                            move(pastChessPosition, chessPositions[coordinate.getBtnID()]);
                            castlablePiece.setHasCastled(true);
                        }
                    }
                });
                if (castlablePiece.hasCastled()) {
                    return;
                }
            }
        }

        move(pastChessPosition, clickedPosition);
    }

    private void processCapture(ChessPosition position) {
        ChessPiece movedChessPiece = pastChessPosition.getChessPiece();
        boolean hasChessPiece = movedChessPiece != null;

        // Theoretically impossible for this conditional to be false. It is here just to be safe
        if (hasChessPiece) {
            ChessPiece capturedPiece = position.getChessPiece();

            if (movedChessPiece instanceof SpecifyCapture && movedChessPiece instanceof Enpassantable) {
                List<ChessDirection> capturableSquares = ((SpecifyCapture) movedChessPiece).getCapturableMoves();
                for (ChessDirection vector : capturableSquares) {
                    if (vector.contains(position.coordinate)) {
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

                            chessBoard.fireEvent(new EnpassantEvent(position.getChessPiece()));
                        }
                        capture(pastChessPosition, position);
                    }
                }
            } else {
                capture(pastChessPosition, position);
            }

            removeSpecialPieces();
        }
    }

    private List<ChessPosition> playableSquaresRefinery(ChessPiece chessPiece, ChessPosition position, ChessPosition[] chessPositions, boolean isFromClick) {
        if (memoizedPastPositions.containsKey(chessPiece) && isFromClick && !chessBoard.pieceInCheck) {

            pastChessPosition = position;

            setAttackedPositions(chessPiece, memoizedAttacks.get(chessPiece), isFromClick);

            enableRefinedPlayableSquares(chessPiece, memoizedPastPositions.get(chessPiece), isFromClick);

            return memoizedAttacks.get(chessPiece);
        }

        ArrayList<ChessPosition> attackedPositions = new ArrayList<>();

        numMoves = 0;

        if (isFromClick) {
            pastChessPosition = position;
        }
        pastPositions = new ArrayList<>();

        memoizedAttacks.put(chessPiece, attackedPositions);
        memoizedPastPositions.put(chessPiece, pastPositions);


        List<ChessDirection> pieceVectors = new ArrayList<>(new LinkedHashSet<>(chessPiece.getPlayableDirections()));

        if (position.getChessPiece() != null) {
            if (chessPiece instanceof Castlable castlablePiece && !chessBoard.pieceInCheck && chessBoard.castlingAllowed) {
                if (!castlablePiece.hasCastled() && !chessPiece.hasMoved()) {
                    HashMap<PieceScalar, CastleVector> castleDirections = castlablePiece.getInitializedCastleDirections();
                    castleDirections.forEach((scalar, castleVector) -> {
                        ChessPiece terminalPiece = chessPositions[castleVector.getTerminalPoint().getBtnID()].getChessPiece();
                        boolean terminalPieceConditionals = terminalPiece != null && !terminalPiece.hasMoved();
                        if (terminalPieceConditionals) {
                            boolean vectorLineSegmentEmpty = false;

                            for (Coordinate coordinate : castleVector) {
                                if (chessPiece instanceof Checkable && chessBoard.checkAllowed) {
                                    ChessPosition posOfFocus = chessBoard.chessPositions[coordinate.getBtnID()];
                                    if (posOfFocus != null && posOfFocus.isAttacked()) {
                                        break;
                                    }
                                }
                                if (coordinate.equals(castleVector.getTerminalPoint())) {
                                    vectorLineSegmentEmpty = true;
                                    break;
                                }
                                if (chessPositions[coordinate.getBtnID()].getChessPiece() != null) {
                                    break;
                                }
                            }

                            if (castleVector.getCastleDependent() != null) {
                                if (vectorLineSegmentEmpty && castleVector.getCastleDependent().isOnStart()) {
                                    for (Coordinate coordinate : scalar) {
                                        ChessPosition posOfCoord = chessPositions[coordinate.getBtnID()];
                                        //posOfCoord.setDisable(false);
                                        pastPositions.add(posOfCoord);
                                    }
                                }
                            }
                        }
                    });
                }
            }

            if (chessPiece instanceof SpecifyCapture) {
                List<ChessDirection> capturableSquares = ((SpecifyCapture)chessPiece).getCapturableMoves();
                for (ChessDirection direction : capturableSquares) {
                    boolean pieceInWay = false;
                    boolean checkablePieceInWay = false;
                    boolean firstPotentialPin = false;
                    ChessPiece possiblePin = null;

                    int distFromOrigin = -1;
                    for (Coordinate coord : direction) {
                        distFromOrigin++;

                        ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                        ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                        boolean posHasChessPiece = coordinatePiece != null;

                        if (chessPiece instanceof Checkable && chessBoard.checkMateAllowed) {
                            boolean posOfCoordAttackedByOtherTeam = false;
                            if (posOfCoord.isAttacked()) {
                                for (ChessPiece attacker : posOfCoord.attackers) {
                                    if (attacker != null && attacker.getTeam() != chessPiece.getTeam()) {
                                        //checkAndAttackDirection.put(checkable, direction);
                                        posOfCoordAttackedByOtherTeam = true;
                                    }
                                }
                            }
                            if (posOfCoordAttackedByOtherTeam) {
                                break;
                            }
                        }

                        if (!pieceInWay || checkablePieceInWay) {
                            attackedPositions.add(posOfCoord);
                        }

                        if (!firstPotentialPin && posHasChessPiece) {
                            if (!pieceInWay) {
                                //if (isFromClick) {
                                    pastPositions.add(posOfCoord);
                                //}
                            }
                        }

                        if (posHasChessPiece) {
                            boolean chessPieceIsSpecialPiece = coordinatePiece instanceof SpecialPiece;
                            boolean chessPieceIsCheckable = coordinatePiece instanceof Checkable;

                            if (firstPotentialPin && chessPieceIsCheckable) {
                                possiblePin.setPinned(true);
                                pinnerPinnedMap.put(chessPiece, possiblePin);
                            }

                            if (chessPiece.getTeam() != coordinatePiece.getTeam()) {
                                if (coordinatePiece instanceof Checkable && chessBoard.checkMateAllowed) {
                                    distFrmOriAttackDirectionMap.put(++distFromOrigin, direction);
                                }
                            }

                            if (!chessPieceIsSpecialPiece) {
                                if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {

                                    if (chessPieceIsCheckable && !pieceInWay) {
                                        checkablePieceInWay = true;
                                    }

                                    pieceInWay = true;


                                    if (!chessPieceIsCheckable && firstPotentialPin) {
                                        break;
                                    }
                                }

                                if (!firstPotentialPin) {
                                    possiblePin = coordinatePiece;
                                    firstPotentialPin = true;
                                }
                            }
                        }
                    }
                }
            }

            for (ChessDirection direction : pieceVectors) {
                boolean pieceInWay = false;
                boolean checkablePieceInWay = false;
                boolean firstPotentialPin = false;
                ChessPiece possiblePin = null;

                int distFromOrigin = -1;
                for (Coordinate coord : direction) {
                    distFromOrigin++;

                    ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                    ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                    boolean posHasChessPiece = coordinatePiece != null;

                    if (chessPiece instanceof Checkable && chessBoard.checkMateAllowed) {
                        boolean posOfCoordAttackedByOtherTeam = false;
                        if (posOfCoord.isAttacked()) {
                            for (ChessPiece attacker : posOfCoord.attackers) {
                                if (attacker != null && attacker.getTeam() != chessPiece.getTeam()) {
                                    //checkAndAttackDirection.put(checkable, direction);
                                    posOfCoordAttackedByOtherTeam = true;
                                }
                            }
                        }
                        if (posOfCoordAttackedByOtherTeam) {
                            break;
                        }
                    }

                    if (chessPiece instanceof SpecifyCapture) { // Fixes pawns showing what is directly in front of them
                        if (!posHasChessPiece) {
                            //posOfCoord.setDisable(false);
                            if (!pieceInWay && !firstPotentialPin) {
                                //if (isFromClick) {
                                    pastPositions.add(posOfCoord);
                                //}
                            }
                        }
                    } else {
                        //posOfCoord.setDisable(false);

                        if (!pieceInWay || checkablePieceInWay) {
                            attackedPositions.add(posOfCoord);
                        }

                        if (!firstPotentialPin) { //
                            if (!pieceInWay) { //isFromClick
                                pastPositions.add(posOfCoord);
                            }
                        }
                    }

                    if (posHasChessPiece) {
                        boolean chessPieceIsSpecialPiece = coordinatePiece instanceof SpecialPiece;
                        boolean chessPieceIsCheckable = coordinatePiece instanceof Checkable;

                        if (firstPotentialPin && chessPieceIsCheckable) {
                            possiblePin.setPinned(true);
                            pinnerPinnedMap.put(chessPiece, possiblePin);
                        }

                        // Coordinate sees a checkable piece
                        if (!(chessPiece instanceof SpecifyCapture) && chessPiece.getTeam() != coordinatePiece.getTeam()) {
                            if (coordinatePiece instanceof Checkable && chessBoard.checkMateAllowed) {
                                distFrmOriAttackDirectionMap.put(++distFromOrigin, direction);
                            }
                        }

                        if (!chessPieceIsSpecialPiece) {
                            if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {

                                if (chessPieceIsCheckable && !pieceInWay) {
                                    checkablePieceInWay = true;
                                }

                                pieceInWay = true;

                                if (!chessPieceIsCheckable && firstPotentialPin) {
                                    break;
                                }
                            }

                            if (!firstPotentialPin) {
                                possiblePin = coordinatePiece;
                                firstPotentialPin = true;
                            }
                        }
                    }
                }
            }
        }

        setAttackedPositions(chessPiece, attackedPositions, isFromClick);

        enableRefinedPlayableSquares(chessPiece, pastPositions, isFromClick);

        return attackedPositions;
    }

    private void setAttackedPositions(ChessPiece chessPiece, List<ChessPosition> attackedPositions, boolean isFromClick) {
        if (chessBoard.checkMateAllowed) {
            for (ChessPosition pastChessPosition : attackedPositions) {

                if (chessPiece.getTeam() != chessBoard.playerTeam && !isFromClick) {
                    pastChessPosition.attackers = new HashSet<>();
                    pastChessPosition.attackers.add(chessPiece);
                    pastChessPosition.setIsAttacked(true);
                    pastChessPosition.setText("ATTK");
                }
            }
        }
    }

    private void enableRefinedPlayableSquares(ChessPiece chessPiece, List<ChessPosition> pastPositions, boolean isFromClick) {
        if ((chessBoard.pieceInCheck || chessPiece.isPinned()) && !(chessPiece instanceof Checkable)) {

            List<Coordinate> convertedPosToCoord = pastPositions.stream()
                    .map(ChessPosition::getCoordinate)
                    .toList();

            for (Map.Entry<Integer, ChessDirection> checkMap : distFrmOriAttackDirectionMap.entrySet()) {
                int distFromOrigin = 0;

                for (Coordinate coord : checkMap.getValue().getDirectionFromOrigin()) {
                    if (convertedPosToCoord.contains(coord)) {
                        if (checkMap.getKey() >= distFromOrigin) {// making equals fixes piece not being able to capture when piece in check

                            ChessPiece pieceInAttackVector = chessBoard.chessPositions[coord.getBtnID()].getChessPiece();

                            if (pieceInAttackVector != null) {
                                if (pieceInAttackVector.getTeam() != chessPiece.getTeam()) {
                                    if (isFromClick) {
                                        chessBoard.chessPositions[coord.getBtnID()].setDisable(false);
                                    }
                                    numMoves++;
                                }
                            } else {
                                if (isFromClick) {
                                    chessBoard.chessPositions[coord.getBtnID()].setDisable(false);
                                }
                                numMoves++;
                            }

                        }
                    }

                    distFromOrigin++;
                }
            }

            return;
        }

        if (chessPiece.isPinned()) {
            return;
        }

        for (ChessPosition chessPosition : pastPositions) {
            if (chessPosition.getChessPiece() == null || (chessPosition.getChessPiece() != null && chessPosition.getChessPiece().getTeam() != chessPiece.getTeam())) {
                if (isFromClick) {
                    chessPosition.setDisable(false);
                }
                numMoves++;
            }
        }
    }

    private void resetMetaData() {
        chessBoard.pieceInCheck = false;
        distFrmOriAttackDirectionMap.clear();
        memoizedPastPositions.clear();
        memoizedAttacks.clear();
    }

    private void createEnpassantPiece(ChessPosition[] chessPositions) {
        ChessPiece enpassPawn = pastChessPosition.getChessPiece();

        int momentum = enpassPawn.getTeam() == Team.WHITE ? 1 : -1;
        int newBtnId = enpassPawn.getBtnID() + ChessBoard.width * momentum;
        ChessPosition onePlaceUnderPawn = chessPositions[newBtnId];
        Coordinate onePlaceUnder = new Coordinate(newBtnId);

        Enpassant enpassant = new Enpassant(onePlaceUnder, enpassPawn.getTeam());
        enpassant.setPosition(chessBoard.getChessPositions()[newBtnId]);
        chessBoard.chessPieces.add(enpassant);
        onePlaceUnderPawn.setChessPiece(enpassant);
        chessBoard.getChessPositions()[newBtnId].setText("Enpassant");
    }

    private void removeSpecialPieces() {
        for (int i = 0; i < chessBoard.chessPieces.size(); i++) {
            ChessPiece chessPiece = chessBoard.chessPieces.get(i);

            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece instanceof SpecialPiece) {
                chessPosition.setDisable(true);
                chessPosition.setChessPiece(null);
                chessPosition.setText("");
                chessBoard.chessPieces.remove(chessPiece);
                i--;
            }
        }
    }

    private void disablePastPositionsAttacked(ChessPiece chessPiece) {

        if (memoizedAttacks.containsKey(chessPiece)) { // past Pos
            for (ChessPosition pos : memoizedAttacks.get(chessPiece)) {

                boolean posNotNull = pos != null;

                if (posNotNull) {
                    boolean posHasChessPiece = pos.getChessPiece() != null;
                    boolean pieceTeamNotCurrentTeam;

                    if (posHasChessPiece && pos.getChessPiece().getTeam() != null) {

                        Team pieceTeam = pos.getChessPiece().getTeam();
                        pieceTeamNotCurrentTeam = !pieceTeam.name().equals(chessBoard.playerTeam.name());
                    } else {
                        pieceTeamNotCurrentTeam = false;
                    }

                    if (!posHasChessPiece || pieceTeamNotCurrentTeam) {
                        pos.attackers.remove(pastChessPosition.getChessPiece()); // Checkmate
                        if (pos.attackers.isEmpty()) {
                            pos.setIsAttacked(false);
                            pos.setText("");
                        }
                    }
                }
            }
        }

    }

    private void disablePastPlayablePositions(ChessPiece chessPiece) {

        if (memoizedPastPositions.containsKey(chessPiece)) { // used pastPositions

            for (ChessPosition pos : memoizedPastPositions.get(chessPiece)) {
                boolean posNotNull = pos != null;

                if (posNotNull) {
                    boolean posHasChessPiece = pos.getChessPiece() != null;
                    boolean pieceTeamNotCurrentTeam;

                    if (posHasChessPiece && pos.getChessPiece().getTeam() != null) {
                        Team pieceTeam = pos.getChessPiece().getTeam();
                        pieceTeamNotCurrentTeam = !pieceTeam.name().equals(chessBoard.playerTeam.name());
                    } else {
                        pieceTeamNotCurrentTeam = false;
                    }

                    if (!posHasChessPiece || pieceTeamNotCurrentTeam) {
                        pos.setDisable(true);
                    }
                }
            }
        }
    }

    public void move(ChessPosition pastPos, ChessPosition futurePos) {

        if (futurePos.getChessPiece() != null) {
            throw new IllegalStateException("Movement square is occupied, try capture() instead.");
        }

        ChessPiece chessPiece = pastPos.getChessPiece();

        futurePos.setChessPiece(chessPiece);
        if (chessPiece.getGraphic() == null) {
            futurePos.setText(chessPiece.getName());
        } else {
            futurePos.setText("");
            futurePos.setGraphic(chessPiece.getGraphic());
        }

        if (futurePos.getChessPiece().getTeam() == chessBoard.playerTeam) {
            futurePos.setDisable(false);
        }

        chessPiece.setPosition(futurePos);
        chessPiece.setCoordinate(futurePos.coordinate);
        chessPiece.wasMoved();

        remove(pastPos, false);

        // PROMOTION
        if (chessPiece instanceof Promotable promotablePiece && chessBoard.promotionAllowed) {
            handlePromotion(chessPiece, promotablePiece);
        }

        // CHECK
        if (chessBoard.checkAllowed) {
            // All attacked squares are no longer attacked
            clearAllAttackers();

            // CHECK
            for (ChessPiece oneOfAllChessPieces : chessBoard.chessPieces) {
                addAttackersToNewPos(chessPiece.getTeam(), oneOfAllChessPieces, pastPos, futurePos);
            }
        }

        // Check
        if (!(chessPiece instanceof Checkable) && chessBoard.checkMateAllowed) {
            processCheckMate(chessPiece, futurePos);
        }

        // Pin
        if (chessBoard.pinAllowed) {
            evaluatePin();
        }

        // Castling calls move twice: the if statement ensures only 1 movement event is created
        if (chessPiece.getTeam() == chessBoard.playerTeam) {
            chessBoard.fireEvent(new MovementEvent(chessPiece));
        }

        chessBoard.enableChessPieces();
    }

    void clearAllAttackers() {
        for (ChessPosition pos : chessBoard.chessPositions) {
            pos.attackers.clear();
            pos.setText("");
            pos.setIsAttacked(false);
        }
    }

    void addAttackersToNewPos(Team chessPieceTeam, ChessPiece oneOfAllChessPieces, ChessPosition pastPos, ChessPosition futurePos) {
        if (chessPieceTeam == oneOfAllChessPieces.getTeam()) {
            //for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, pastPos, chessBoard.chessPositions, false)) {
                //pos.attackers.remove(oneOfAllChessPieces);
                //pos.setIsAttacked(false);
            //}
            addAttackers(oneOfAllChessPieces, futurePos);
        }
    }

    public void addAttackers(ChessPiece oneOfAllChessPieces, ChessPosition futurePos) {
        for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, futurePos, chessBoard.chessPositions, false)) {
            pos.attackers.add(oneOfAllChessPieces);
            pos.setIsAttacked(true);
        }
    }

    public boolean scanForCheck(ChessPiece chessPiece, ChessPosition futurePos) {
        for (ChessPosition position : playableSquaresRefinery(chessPiece, futurePos, chessBoard.chessPositions, false)) {
            if (position.getChessPiece() instanceof Checkable && position.getChessPiece().getTeam() != chessPiece.getTeam()) {
                chessBoard.fireEvent(new CheckEvent(position.getChessPiece()));
                return true;
            }
        }

        return false;
    }

    public void processCheckMate(ChessPiece chessPiece, ChessPosition futurePos) {
        scanForCheck(chessPiece, futurePos);

        if (chessBoard.pieceInCheck) {
            int totalMoves = 0;
            for (ChessPiece oneOfAllChessPieces : chessBoard.chessPieces) {
                if (oneOfAllChessPieces.getTeam() != chessPiece.getTeam()) {
                    playableSquaresRefinery(oneOfAllChessPieces, futurePos, chessBoard.chessPositions, false);
                    totalMoves += numMoves;
                    System.out.println(oneOfAllChessPieces + ": " + numMoves);
                }
            }

            if (totalMoves == 0) {
                System.out.println("CHECKMATE");
            }
        }
    }

    public void evaluatePin() {
        for (Map.Entry<ChessPiece, ChessPiece> pinnerPinEntry : pinnerPinnedMap.entrySet()) {
            ChessPiece pinner = pinnerPinEntry.getKey();
            ChessPiece pinned = pinnerPinEntry.getValue();

            if (pinner instanceof SpecifyCapture spCaPinner) {
                processPin(pinner, pinned, spCaPinner.getCapturableMoves());
            } else {
                processPin(pinner, pinned, pinner.getPlayableDirections());
            }

        }
    }

    void processPin(ChessPiece pinner, ChessPiece pinned, List<ChessDirection> directions) {
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
                                break;
                            } else {
                                break;
                            }

                        }

                        if (!possiblePin) {
                            possiblePin = true;
                            mayBePinned = pieceInVector;
                        }
                    }
                }
            }

            if (verifiedPin != null && verifiedPin.equals(pinned)) {
                pinned.setPinned(true);
                break;
            } else {
                pinned.setPinned(false);
            }
        }
    }

    void handlePromotion(ChessPiece chessPiece, Promotable promotablePiece) {
        int rowToPromote;
        int colToPromote;
        if (chessPiece.getTeam() == Team.BLACK) {
            rowToPromote = promotablePiece.getBlackPromotionSquares().row();
            colToPromote = promotablePiece.getBlackPromotionSquares().col();
        } else {
            rowToPromote = promotablePiece.getWhitePromotionSquares().row();
            colToPromote = promotablePiece.getWhitePromotionSquares().col();
        }
        boolean rowAbsent = rowToPromote == -1;
        boolean colAbsent = colToPromote == -1;

        if (!rowAbsent && !colAbsent) {
            boolean pieceRowOnPromote = chessPiece.getCoordinate().getRow() == rowToPromote;
            boolean pieceColOnPromote = chessPiece.getCoordinate().getCol() == colToPromote;
            if (pieceRowOnPromote && pieceColOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(chessPiece, promotablePiece, this));
            }
        } else if (rowAbsent) {
            boolean pieceColOnPromote = chessPiece.getCoordinate().getCol() == colToPromote;
            if (pieceColOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(chessPiece, promotablePiece, this));
            }
        } else {
            boolean pieceRowOnPromote = chessPiece.getCoordinate().getRow() == rowToPromote;
            if (pieceRowOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(chessPiece, promotablePiece, this));
            }
        }
    }

    public void capture(ChessPosition pastPos, ChessPosition futurePos) {
        if (futurePos.getChessPiece() == null) {
            throw new IllegalStateException("nothing to capture");
        }
        ChessPiece capturedPiece = futurePos.getChessPiece();
        ChessPiece instigator = pastPos.getChessPiece();

        //CheckMate
        if (chessBoard.checkMateAllowed) {
            for (ChessPosition piecePlayables : playableSquaresRefinery(instigator, pastPos, chessBoard.chessPositions, false)) {
                piecePlayables.attackers.remove(instigator);
            }
        }

        remove(futurePos, true);
        move(pastPos, futurePos);

        boolean capturedPieceIsSpecial = capturedPiece instanceof SpecialPiece;
        if (!capturedPieceIsSpecial) {
            chessBoard.fireEvent(new CaptureEvent(instigator, capturedPiece));
        }
    }

    public void remove(ChessPosition pos, boolean capture) {
        if (capture) {
            chessBoard.chessPieces.remove(pos.chessPiece);

            //CheckMate
            for (ChessPosition piecePlayables : playableSquaresRefinery(pos.chessPiece, pos, chessBoard.chessPositions, false)) {
                piecePlayables.attackers.remove(pos.chessPiece);
                piecePlayables.setText("");
            }
        }

        pos.setText("");
        pos.setChessPiece(null);
        pos.setGraphic(null);
        pos.setDisable(true);
    }
}

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
    private final ArrayList<PinCheckInformation> pinCheckInformation;
    private final HashMap<ChessPiece, ChessPiece> pinnerPinnedMap;
    private final HashMap<ChessPiece, List<ChessPosition>> memoizedPastPositions;
    private final HashMap<ChessPiece, List<ChessPosition>> memoizedAttacks;
    private final HashMap<ChessPiece, Integer> chessPieceNumMovesMap;

    public int numMoves;

    public MoveLogic(ChessBoard chessBoard) {
        super(chessBoard);

        pinCheckInformation = new ArrayList<>();
        chessPieceNumMovesMap = new HashMap<>();
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
        playableSquaresRefinery(chessPiece, clickedPosition, chessPositions, true,true);
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

    List<ChessPosition> playableSquaresRefinery(ChessPiece chessPiece, ChessPosition position, ChessPosition[] chessPositions, boolean canSetAttack, boolean isFromClick) {
        boolean pieceIsPinning = false;
        for (PinCheckInformation info : pinCheckInformation) {
            if (chessPiece.equals(info.pinnerPiece())) {
                pieceIsPinning = true;
                break;
            }
        }

        if (memoizedPastPositions.containsKey(chessPiece) && isFromClick && !chessBoard.pieceInCheck && !pieceIsPinning) {

            pastChessPosition = position;

            if (canSetAttack) {
                setAttackedPositions(chessPiece, memoizedAttacks.get(chessPiece), true);
            }

            enableRefinedPlayableSquares(chessPiece, memoizedPastPositions.get(chessPiece), true);

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
            // Castling
            if (chessPiece instanceof Castlable castlablePiece && !chessBoard.pieceInCheck && chessBoard.castlingAllowed) {
                determineCastlingMoves(chessPiece, castlablePiece, chessPositions);
            }

            // Specific attack vectors
            if (chessPiece instanceof SpecifyCapture) {
                determineSpecificCaptureMoves(chessPiece, chessPositions, attackedPositions);
            }

            // General vectors
            determineGeneralMoves(chessPiece, chessPositions, pieceVectors, attackedPositions);
        }

        if (canSetAttack) {
            setAttackedPositions(chessPiece, attackedPositions, isFromClick);
        }

        enableRefinedPlayableSquares(chessPiece, pastPositions, isFromClick);
        chessPieceNumMovesMap.put(chessPiece, numMoves);

        return attackedPositions;
    }

    private void determineCastlingMoves(ChessPiece chessPiece, Castlable castlablePiece, ChessPosition[] chessPositions) {
        if (!castlablePiece.hasCastled() && !chessPiece.hasMoved()) {
            HashMap<PieceScalar, CastleVector> castleDirections = castlablePiece.getInitializedCastleDirections();
            castleDirections.forEach((scalar, castleVector) -> {
                ChessPiece terminalPiece;
                try {
                    terminalPiece = chessPositions[castleVector.getTerminalPoint().getBtnID()].getChessPiece();
                } catch (NoSuchElementException nsee) {
                    terminalPiece = null;
                }

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
                                pastPositions.add(posOfCoord);
                            }
                        }
                    }
                }
            });
        }
    }

    private void determineSpecificCaptureMoves(ChessPiece chessPiece, ChessPosition[] chessPositions, ArrayList<ChessPosition> attackedPositions) {
        List<ChessDirection> capturableSquares = ((SpecifyCapture)chessPiece).getCapturableMoves();
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

                if (chessPiece instanceof Checkable && chessBoard.checkMateAllowed) {
                    if (isCheckableInDanger(chessPiece, posOfCoord)) {
                        break;
                    }
                }

                if (!pieceInWay || checkablePieceInWay) {
                    attackedPositions.add(posOfCoord);
                }

                if (!firstPotentialPin && posHasChessPiece) {
                    if (!pieceInWay) {
                        pastPositions.add(posOfCoord);
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
                            PinCheckInformation info = new PinCheckInformation(
                                    ++distFromOrigin, direction, coordinatePiece, chessPiece
                            );

                            pinCheckInformation.add(info);
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

                        if (!firstPotentialPin && coordinatePiece.getTeam() != chessPiece.getTeam()) {
                            possiblePin = coordinatePiece;
                            firstPotentialPin = true;
                        }
                    }
                }
            }
        }
    }

    private void determineGeneralMoves(ChessPiece chessPiece, ChessPosition[] chessPositions, List<ChessDirection> pieceVectors, List<ChessPosition> attackedPositions) {
        for (ChessDirection direction : pieceVectors) {
            boolean pieceInWay = false;
            boolean checkablePieceInWay = false;
            boolean firstPotentialPin = false;
            ChessPiece possiblePin = null;

            int distFromOrigin = 0;
            for (Coordinate coord : direction) {

                ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                boolean posHasChessPiece = coordinatePiece != null;

                if (chessPiece instanceof Checkable && chessBoard.checkMateAllowed) {
                    if (isCheckableInDanger(chessPiece, posOfCoord)) {
                        break;
                    }
                }

                if (chessPiece instanceof SpecifyCapture) { // Fixes pawns showing what is directly in front of them
                    if (!posHasChessPiece) {
                        if (!pieceInWay && !firstPotentialPin) {
                            pastPositions.add(posOfCoord);
                        }
                    }
                } else {

                    if (!pieceInWay || (checkablePieceInWay)) {
                        attackedPositions.add(posOfCoord);
                    }

                    if (!firstPotentialPin) {
                        if (!pieceInWay) {
                            pastPositions.add(posOfCoord);
                        }
                    }
                }

                if (posHasChessPiece) {
                    boolean chessPieceIsSpecialPiece = coordinatePiece instanceof SpecialPiece;
                    boolean chessPieceIsCheckable = coordinatePiece instanceof Checkable;
                    boolean chessPieceIsCheckableAndDifTeam = coordinatePiece instanceof Checkable && chessPiece.getTeam() != coordinatePiece.getTeam();


                    if (firstPotentialPin && chessPieceIsCheckableAndDifTeam) {
                        possiblePin.setPinned(true);
                        pinnerPinnedMap.put(chessPiece, possiblePin);
                    }

                    // Coordinate sees a checkable piece
                    if (!(chessPiece instanceof SpecifyCapture) && chessPiece.getTeam() != coordinatePiece.getTeam()) {
                        if (coordinatePiece instanceof Checkable && chessBoard.checkMateAllowed) {
                            PinCheckInformation info = new PinCheckInformation(
                                    ++distFromOrigin, direction, coordinatePiece, chessPiece
                            );

                            pinCheckInformation.add(info);
                        }
                    }

                    if (!chessPieceIsSpecialPiece) {
                        if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {

                            if (chessPieceIsCheckable && !pieceInWay && coordinatePiece.getTeam() != chessPiece.getTeam()) {
                                checkablePieceInWay = true;
                            }

                            pieceInWay = true;

                            if (!chessPieceIsCheckable && firstPotentialPin) {
                                break;
                            }
                        }

                        if (!firstPotentialPin && coordinatePiece.getTeam() != chessPiece.getTeam()) {
                            possiblePin = coordinatePiece;
                            firstPotentialPin = true;
                        }
                    }
                }

                distFromOrigin++;
            }
        }
    }

    private boolean isCheckableInDanger(ChessPiece chessPiece, ChessPosition posOfCoord) {
        boolean posOfCoordAttackedByOtherTeam = false;
        if (posOfCoord.isAttacked()) {
            for (ChessPiece attacker : posOfCoord.attackers) {
                if (attacker != null && attacker.getTeam() != chessPiece.getTeam()) {
                    posOfCoordAttackedByOtherTeam = true;
                    break;
                }
            }
        }

        return posOfCoordAttackedByOtherTeam;
    }


    private void setAttackedPositions(ChessPiece chessPiece, List<ChessPosition> attackedPositions, boolean isFromClick) {
        if (chessBoard.checkMateAllowed && !isFromClick && chessPiece.getTeam() != chessBoard.playerTeam) { // SET TO EQUALS

            for (ChessPosition pastChessPosition : attackedPositions) {

                if (chessPiece.getTeam() != chessBoard.playerTeam) {
                    pastChessPosition.attackers = new HashSet<>();
                    pastChessPosition.attackers.add(chessPiece);
                    pastChessPosition.setIsAttacked(true);
                }
            }
        }
    }

    private void enableRefinedPlayableSquares(ChessPiece chessPiece, List<ChessPosition> pastPositions, boolean isFromClick) {
        if ((chessBoard.pieceInCheck || chessPiece.isPinned()) && !(chessPiece instanceof Checkable)) {

            List<Coordinate> convertedPosToCoord = pastPositions.stream()
                    .map(ChessPosition::getCoordinate)
                    .toList();

            for (PinCheckInformation info : pinCheckInformation) {
                int distFromOrigin = 0;

                for (Coordinate coord : info.pinCheckDirection().getDirectionFromOrigin()) {
                    if (convertedPosToCoord.contains(coord)) {
                        if (info.distFromOrigin() >= distFromOrigin && info.pinnedPiece().getTeam() == chessPiece.getTeam()) { // making equals fixes piece not being able to capture when piece in check

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

        if (chessPiece instanceof Checkable) {
            for (ChessPosition chessPosition : pastPositions) {
                if (!chessPosition.isAttacked() && (chessPosition.getChessPiece() == null || (chessPosition.getChessPiece() != null && chessPosition.getChessPiece().getTeam() != chessPiece.getTeam()))) {
                    if (isFromClick) {
                        chessPosition.setDisable(false);
                    }
                    numMoves++;
                }
            }

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
        pinCheckInformation.clear();
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
        onePlaceUnderPawn.setText("Enpassant");
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
                    boolean pieceTeamNotCurrentTeam = isPieceTeamCurrentTeam(pos, posHasChessPiece);

                    if (!posHasChessPiece || pieceTeamNotCurrentTeam) {
                        pos.attackers.remove(pastChessPosition.getChessPiece()); // Checkmate
                        if (pos.attackers.isEmpty()) {
                            pos.setIsAttacked(false);
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
                    boolean pieceTeamNotCurrentTeam = isPieceTeamCurrentTeam(pos, posHasChessPiece);

                    if (!posHasChessPiece || pieceTeamNotCurrentTeam) {
                        pos.setDisable(true);
                    }
                }
            }
        }
    }

    private boolean isPieceTeamCurrentTeam(ChessPosition pos, boolean posHasChessPiece) {
        boolean pieceTeamNotCurrentTeam;

        if (posHasChessPiece && pos.getChessPiece().getTeam() != null) {
            Team pieceTeam = pos.getChessPiece().getTeam();
            pieceTeamNotCurrentTeam = !pieceTeam.name().equals(chessBoard.playerTeam.name());
        } else {
            pieceTeamNotCurrentTeam = false;
        }

        return pieceTeamNotCurrentTeam;
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
            processCheckMate(chessPiece, futurePos); //Analysis of attacks HERE
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
            pos.setIsAttacked(false);
        }
    }

    void addAttackersToNewPos(Team chessPieceTeam, ChessPiece oneOfAllChessPieces, ChessPosition pastPos, ChessPosition futurePos) {
        if (chessPieceTeam == oneOfAllChessPieces.getTeam()) {
            for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, pastPos, chessBoard.chessPositions, false, false)) {
                pos.attackers.remove(oneOfAllChessPieces);
                pos.setIsAttacked(false);
            }

            addAttackers(oneOfAllChessPieces, futurePos);
        }
    }

    public void addAttackers(ChessPiece oneOfAllChessPieces, ChessPosition futurePos) {
        for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, futurePos, chessBoard.chessPositions, true, false)) {
            pos.attackers.add(oneOfAllChessPieces);
            pos.setIsAttacked(true);
        }
    }

    public boolean scanForCheck(ChessPiece chessPiece, ChessPosition futurePos) {
        for (ChessPosition position : playableSquaresRefinery(chessPiece, futurePos, chessBoard.chessPositions, true, false)) {
            if (position.getChessPiece() instanceof Checkable && position.getChessPiece().getTeam() != chessPiece.getTeam()) {
                chessBoard.fireEvent(new CheckEvent(position.getChessPiece()));
                return true;
            }
        }

        return false;
    }

    public void processCheckMate(ChessPiece chessPiece, ChessPosition futurePos) {
        if (scanForCheck(chessPiece, futurePos)) {
            pinCheckInformation.clear();

            int totalMoves = 0;

            System.out.println();

            for (ChessPiece oneOfAllChessPieces : chessBoard.chessPieces) {
                playableSquaresRefinery(oneOfAllChessPieces, oneOfAllChessPieces.getPosition(), chessBoard.chessPositions, false, false);
            }

            for (Map.Entry<ChessPiece, Integer> numMovesEntry : chessPieceNumMovesMap.entrySet()) {
                if (numMovesEntry.getKey().getTeam() != chessPiece.getTeam()) {
                    totalMoves += numMovesEntry.getValue();
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
                            }
                            break;

                        }

                        possiblePin = true;
                        mayBePinned = pieceInVector;

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
            for (ChessPosition piecePlayables : playableSquaresRefinery(instigator, pastPos, chessBoard.chessPositions, false, false)) {
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
            for (ChessPosition piecePlayables : playableSquaresRefinery(pos.chessPiece, pos, chessBoard.chessPositions, false, false)) {
                piecePlayables.attackers.remove(pos.chessPiece);
            }
        }

        chessPieceNumMovesMap.remove(pos.chessPiece);

        pos.setText("");
        pos.setChessPiece(null);
        pos.setGraphic(null);
        pos.setDisable(true);
    }
}

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

// Make this none static
public class Move {
    public static ChessBoard chessBoard;
    public static ArrayList<ChessPosition> pastPositions;
    public static ChessPosition pastChessPosition;
    public static HashMap<Checkable, ChessDirection> checkAndAttackDirection = new HashMap<>();
    public static HashMap<ChessPiece, ChessPiece> pinnerPinnedMap = new HashMap<>();
    public static int numMoves;

    static int a; //DELETE

    static void positionClick(ChessPosition clickedPosition) {
        ChessPiece chessPiece = clickedPosition.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement
        if (chessPiece == null) {
            chessBoard.pieceInCheck = false;
            checkAndAttackDirection = new HashMap<>();

            disablePastPositionsAttacked();
            disablePastPlayablePositions();
            processNormalMovement(clickedPosition, chessPositions);
            return;
        }

        // Capturing
        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            if (pastChessPiece != null && pastChessPiece.getTeam() != chessPiece.getTeam()) {
                chessBoard.pieceInCheck = false;
                checkAndAttackDirection = new HashMap<>();

                processCapture(clickedPosition);
                disablePastPositionsAttacked();
                disablePastPlayablePositions();
                return;
            }
        }

        if (pastChessPosition != null) {
            disablePastPlayablePositions();
        }


        System.out.println("CLICK " + a++);

        // Playable Squares Refiner
        playableSquaresRefinery(chessPiece, clickedPosition, chessPositions, true);

        System.out.println(chessPiece + " PIECE CLICKED");
        System.out.println(!chessPiece.isPinned());

        System.out.println("PASTPOS:");
        for (ChessPosition chessPosition : pastPositions) {
            System.out.println(chessPosition.coordinate);
        }
        System.out.println();

    }

    private static boolean isCheckableCaptureConditions(ChessPosition clickedPosition) {
        boolean checkableCaptureConditions = false;
        if (pastChessPosition != null && pastChessPosition.getChessPiece() != null) {
            boolean capturedPieceConditions = clickedPosition.getChessPiece() != null && clickedPosition.getChessPiece().getTeam() != pastChessPosition.getChessPiece().getTeam();
            boolean wasAttemptToCapture = pastChessPosition != null && pastChessPosition.getChessPiece() instanceof Checkable;
            checkableCaptureConditions = (wasAttemptToCapture && capturedPieceConditions);
        }
        return checkableCaptureConditions;
    }

    private static void processNormalMovement(ChessPosition clickedPosition, ChessPosition[] chessPositions) {
        if (pastChessPosition == null) {
            return;
        }
        Class<? extends ChessPiece> chessPieceClass = pastChessPosition.getChessPiece().getClass();

        int pastBtnId = pastChessPosition.coordinate.getBtnID();
        int currentBtnId = clickedPosition.coordinate.getBtnID();
        boolean isPieceTwoAway = Math.abs(pastBtnId - currentBtnId) == 16;
        ChessPiece pastChessPiece = pastChessPosition.getChessPiece();

        removeSpecialPieces();

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

    private static void processCapture(ChessPosition position) {
        ChessPiece movedChessPiece = pastChessPosition.getChessPiece();
        boolean hasChessPiece = movedChessPiece != null;

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

    private static ArrayList<ChessPosition>  playableSquaresRefinery(ChessPiece chessPiece, ChessPosition position, ChessPosition[] chessPositions, boolean isFromClick) {
        ArrayList<ChessPosition> attackedPositions = new ArrayList<>();

        numMoves = 0;
        if (pastPositions == null) {
            pastPositions = new ArrayList<>();
        }
        if (isFromClick) {
            pastPositions = new ArrayList<>();
            pastChessPosition = position;
        }

        List<ChessDirection> pieceVectors = new ArrayList<>(new LinkedHashSet<>(chessPiece.getPlayableDirections()));

        if (position.getChessPiece() != null && chessBoard.castlingAllowed) {
            if (chessPiece instanceof Castlable castlablePiece && !chessBoard.pieceInCheck) {
                if (!castlablePiece.hasCastled() && !chessPiece.hasMoved()) {
                    HashMap<PieceScalar, CastleVector> castleDirections = castlablePiece.getInitializedCastleDirections();
                    castleDirections.forEach((scalar, castleVector) -> {
                        ChessPiece terminalPiece = chessPositions[castleVector.getTerminalPoint().getBtnID()].getChessPiece();
                        boolean terminalPieceConditionals = terminalPiece != null && !terminalPiece.hasMoved();
                        if (terminalPieceConditionals) {
                            boolean vectorLineSegmentEmpty = false;

                            for (Coordinate coordinate : castleVector) {
                                if (chessPiece instanceof Checkable && chessBoard.checkMateAllowed) {
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
                                        numMoves++;
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
                    for (Coordinate coord : direction) {
                        ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                        ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                        boolean posHasChessPiece = coordinatePiece != null;
                        attackedPositions.add(posOfCoord);

                        if (posHasChessPiece) {

                            // Coordinate sees a checkable piece
                            if (coordinatePiece instanceof Checkable checkable && chessBoard.checkMateAllowed) {
                                if (chessPiece.getTeam() != coordinatePiece.getTeam()) {
                                    checkAndAttackDirection.put(checkable, direction);
                                }
                            }

                            if (chessPiece instanceof Checkable && chessBoard.checkMateAllowed) {
                                if (posOfCoord.isAttacked()) {
                                    break;
                                } else {
                                    //posOfCoord.setDisable(false);
                                    if (isFromClick) {
                                        pastPositions.add(posOfCoord);
                                        numMoves++;
                                    }
                                }
                            } else {
                                //posOfCoord.setDisable(false);
                                if (isFromClick) {
                                    pastPositions.add(posOfCoord);
                                    numMoves++;
                                }

                            }
                            if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {
                                break;
                            }
                        }
                    }
                }
            }

            for (ChessDirection direction : pieceVectors) {
                boolean pieceInWay = false;
                boolean firstPotentialPin = false;
                ChessPiece possiblePin = null;

                for (Coordinate coord : direction) {
                    ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                    ChessPiece coordinatePiece = posOfCoord.getChessPiece();
                    boolean posHasChessPiece = coordinatePiece != null;

                    if (chessPiece instanceof Checkable checkable && chessBoard.checkMateAllowed) {
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
                                if (isFromClick) {
                                    pastPositions.add(posOfCoord);
                                    numMoves++;
                                }
                            }
                        }
                    } else {
                        //posOfCoord.setDisable(false);
                        if (!firstPotentialPin) {
                            attackedPositions.add(posOfCoord);
                            if (!pieceInWay) {
                                if (isFromClick) {
                                    pastPositions.add(posOfCoord);
                                    numMoves++;
                                }
                            }
                        }
                    }

                    if (posHasChessPiece) {
                        boolean chessPieceIsSpecialPiece = coordinatePiece instanceof SpecialPiece;
                        boolean chessPieceIsCheckable = coordinatePiece instanceof Checkable;

                        if (firstPotentialPin && chessPieceIsCheckable) {
                            possiblePin.setPinned(true);
                            pinnerPinnedMap.put(chessPiece, possiblePin);
                            System.out.println(possiblePin + " PINNED");
                        }

                        // Coordinate sees a checkable piece
                        if (!(chessPiece instanceof SpecifyCapture) && chessPiece.getTeam() != coordinatePiece.getTeam()) {
                            if (coordinatePiece instanceof Checkable checkable && chessBoard.checkMateAllowed && !firstPotentialPin) {
                                checkAndAttackDirection.put(checkable, direction);
                            }
                        }

                        if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {
                            if (!chessPieceIsSpecialPiece) {
                                pieceInWay = true;
                            }

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

        if (chessBoard.checkMateAllowed) {
            for (ChessPosition pastChessPosition : attackedPositions) {
                if (chessPiece.getTeam() == chessBoard.playerTeam && !isFromClick) {
                    pastChessPosition.attackers = new HashSet<>();
                    pastChessPosition.attackers.add(chessPiece);
                    ChessPiece pstChessPiece = pastChessPosition.getChessPiece();

                    pastChessPosition.setIsAttacked(true);
                }
            }
        }

        if (!chessPiece.isPinned()) {
            enableRefinedPlayableSquares(chessPiece, isFromClick);
        }

        return attackedPositions;
    }

    private static void enableRefinedPlayableSquares(ChessPiece chessPiece, boolean isFromClick) {
        if (chessBoard.pieceInCheck && !(chessPiece instanceof Checkable)) {
            System.out.println(chessPiece + " IN PIECECHECK");
            List<Coordinate> convertedPosToCoord = pastPositions.stream()
                    .map(ChessPosition::getCoordinate)
                    .toList();

            for (Map.Entry<Checkable, ChessDirection> checkMap : checkAndAttackDirection.entrySet()) {
                for (Coordinate coord : checkMap.getValue().getDirectionFromOrigin()) {
                    if (convertedPosToCoord.contains(coord)) {
                        if (isFromClick) {
                            chessBoard.chessPositions[coord.getBtnID()].setDisable(false);
                        }
                        numMoves++;
                    }
                }
            }
        } else {
            System.out.println(chessPiece + " GOT HERE");

            for (ChessPosition chessPosition : pastPositions) {
                System.out.println(chessPiece + " GOT HERE");
                if (isFromClick) {
                    chessPosition.setDisable(false);
                }
            }
        }
    }

    private static void createEnpassantPiece(ChessPosition[] chessPositions) {
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

    private static void removeSpecialPieces() {
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

    private static void disablePastPositionsAttacked() {
        if (pastPositions != null) {
            for (ChessPosition pos : pastPositions) {

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
                        }
                    }
                }
            }
        }
    }

    private static void disablePastPlayablePositions() {
        if (pastPositions != null) {
            for (ChessPosition pos : pastPositions) {
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

    public static void move(ChessPosition pastPos, ChessPosition futurePos) {

        if (futurePos.getChessPiece() != null) {
            throw new IllegalStateException("Movement square is occupied, try capture() instead.");
        }

        ChessPiece chessPiece = pastPos.getChessPiece();

        futurePos.setChessPiece(chessPiece);
        if (chessPiece.getGraphic() == null) {
            futurePos.setText(pastPos.getText());
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

        // Promotion
        if (chessPiece instanceof Promotable promotablePiece) {
            handlePromotion(chessPiece, promotablePiece);
        }

        if (chessBoard.checkMateAllowed) {
            // CHECKMATE
            for (ChessPosition pos : chessBoard.chessPositions) {
                pos.attackers.clear();
                pos.setIsAttacked(false);
                pos.setText("");
            }

            // CHECKMATE
            for (ChessPiece oneOfAllChessPieces : chessBoard.chessPieces) {
                if (chessPiece.getTeam() == oneOfAllChessPieces.getTeam()) {
                    for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, pastPos, chessBoard.chessPositions, false)) {
                        pos.attackers.remove(oneOfAllChessPieces);
                        pos.setIsAttacked(false);
                    }
                    for (ChessPosition pos : playableSquaresRefinery(oneOfAllChessPieces, futurePos, chessBoard.chessPositions, false)) {
                        pos.attackers.add(oneOfAllChessPieces);
                        pos.setIsAttacked(true);
                    }
                }
            }
        }

        // Check
        if (!(chessPiece instanceof Checkable)) {
            scanForCheck(chessPiece, futurePos);

            if (chessBoard.pieceInCheck) {
                int totalMoves = 0;
                for (ChessPiece oneOfAllChessPieces : chessBoard.chessPieces) {
                    if (oneOfAllChessPieces.getTeam() != chessPiece.getTeam()) {
                        playableSquaresRefinery(oneOfAllChessPieces, futurePos, chessBoard.chessPositions, false);
                        totalMoves += numMoves;
                    }
                }

                if (totalMoves == 0) {
                    System.out.println("CHECKMATE");
                }
            }
        }

        // Pin
        for (Map.Entry<ChessPiece, ChessPiece> pinnerPinEntry : pinnerPinnedMap.entrySet()) {
            ChessPiece pinner = pinnerPinEntry.getKey();
            ChessPiece pinned = pinnerPinEntry.getValue();

            //System.out.println(pinner + " PINNING " + pinned);

            for (ChessDirection direction : pinner.getPlayableDirections()) {
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
                //System.out.println(mayBePinned + " MAYBEPINNED");
                //System.out.println(pinned + " PINNED");
                if (verifiedPin != null) {
                    //System.out.println(mayBePinned.equals(pinned));
                }
                if (verifiedPin != null && verifiedPin.equals(pinned)) {
                    pinned.setPinned(true);
                    break;
                } else {
                    pinned.setPinned(false);
                }
            }
        }

        if (chessPiece.getTeam() == chessBoard.playerTeam) {
            chessBoard.fireEvent(new MovementEvent(chessPiece));
        }

        chessBoard.enableChessPieces();
    }

    public static void scanForCheck(ChessPiece chessPiece, ChessPosition futurePos) {
        for (ChessPosition position : playableSquaresRefinery(chessPiece, futurePos, chessBoard.chessPositions, false)) {
            System.out.println(position.coordinate);
            if (position.getChessPiece() instanceof Checkable && position.getChessPiece().getTeam() != chessPiece.getTeam()) {
                chessBoard.fireEvent(new CheckEvent(position.getChessPiece()));
            }
        }
    }

    static void handlePromotion(ChessPiece chessPiece, Promotable promotablePiece) {
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
                chessBoard.fireEvent(new PromotionEvent(PromotionEvent.PROMOTION, chessPiece, promotablePiece));
            }
        } else if (rowAbsent) {
            boolean pieceColOnPromote = chessPiece.getCoordinate().getCol() == colToPromote;
            if (pieceColOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(PromotionEvent.PROMOTION, chessPiece, promotablePiece));
            }
        } else {
            boolean pieceRowOnPromote = chessPiece.getCoordinate().getRow() == rowToPromote;
            if (pieceRowOnPromote) {
                chessBoard.fireEvent(new PromotionEvent(PromotionEvent.PROMOTION, chessPiece, promotablePiece));
            }
        }
    }

    public static void capture(ChessPosition pastPos, ChessPosition futurePos) {
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

        chessBoard.fireEvent(new CaptureEvent(instigator, capturedPiece));
    }

    public static void remove(ChessPosition pos, boolean capture) {
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

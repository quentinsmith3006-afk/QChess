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
import qchess.chess.logic.event.CaptureEvent;
import qchess.chess.logic.event.EnpassantEvent;
import qchess.chess.logic.event.MovementEvent;
import qchess.chess.logic.event.PromotionEvent;

import java.util.*;

// Make this none static
class Move {
    public static ChessBoard chessBoard;
    public static ArrayList<ChessPosition> pastPositions;
    public static ChessPosition pastChessPosition;

    static void positionClick(ChessPosition clickedPosition) {
        ChessPiece chessPiece = clickedPosition.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement
        if (chessPiece == null) {
            disablePastPlayablePositions();
            processNormalMovement(clickedPosition, chessPositions);
            return;
        }

        // Capturing
        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            if (pastChessPiece != null && pastChessPiece.getTeam() != chessPiece.getTeam()) {
                processCapture(clickedPosition);
                disablePastPlayablePositions();
                return;
            }
        }

        if (pastChessPosition != null) {
            disablePastPlayablePositions();
        }

        // Playable Squares Refiner
        playableSquaresRefinery(chessPiece, clickedPosition, chessPositions);
    }

    private static void processNormalMovement(ChessPosition clickedPosition, ChessPosition[] chessPositions) {
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

    private static void playableSquaresRefinery(ChessPiece chessPiece, ChessPosition position, ChessPosition[] chessPositions) {
        pastPositions = new ArrayList<>();
        pastChessPosition = position;

        List<ChessDirection> pieceVectors = new ArrayList<>(new LinkedHashSet<>(chessPiece.getPlayableDirections()));

        if (position.getChessPiece() != null && chessBoard.castlingAllowed) {
            if (chessPiece instanceof Castlable castlablePiece) {
                if (!castlablePiece.hasCastled() && !chessPiece.hasMoved()) {
                    HashMap<PieceScalar, CastleVector> castleDirections = castlablePiece.getInitializedCastleDirections();
                    castleDirections.forEach((scalar, castleVector) -> {
                        ChessPiece terminalPiece = chessPositions[castleVector.getTerminalPoint().getBtnID()].getChessPiece();
                        boolean terminalPieceConditionals = terminalPiece != null && !terminalPiece.hasMoved();
                        if (terminalPieceConditionals) {
                            boolean vectorLineSegmentEmpty = false;

                            for (Coordinate coordinate : castleVector) {
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
                                        posOfCoord.setDisable(false);
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
                    for (Coordinate coord : direction) {
                        ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                        boolean posHasChessPiece = posOfCoord.getChessPiece() != null;
                        if (posHasChessPiece) {
                            posOfCoord.setDisable(false);
                            pastPositions.add(posOfCoord);
                            if (ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {
                                break;
                            }
                        }
                    }
                }
            }

            for (ChessDirection direction : pieceVectors) {
                for (Coordinate coord : direction) {
                    ChessPosition posOfCoord = chessPositions[coord.getBtnID()];
                    boolean posHasChessPiece = posOfCoord.getChessPiece() != null;

                    if (chessPiece instanceof SpecifyCapture) { // Fixes pawns showing what is directly in front of them
                        if (!posHasChessPiece) {
                            posOfCoord.setDisable(false);
                        }
                    } else {
                        posOfCoord.setDisable(false);
                    }

                    pastPositions.add(posOfCoord);

                    if (posHasChessPiece && !ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {
                        boolean chessPieceIsSpecialPiece = posOfCoord.getChessPiece() instanceof SpecialPiece;
                        if (!chessPieceIsSpecialPiece) {
                            break;
                        }
                    }
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

        // Check


        if (chessPiece.getTeam() == chessBoard.playerTeam) {
            chessBoard.fireEvent(new MovementEvent(chessPiece));
        }
        chessBoard.enableChessPieces();
    }

    public static void handlePromotion(ChessPiece chessPiece, Promotable promotablePiece) {
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

        remove(futurePos, true);
        move(pastPos, futurePos);

        chessBoard.fireEvent(new CaptureEvent(instigator, capturedPiece));
    }

    public static void remove(ChessPosition pos, boolean capture) {
        if (capture) {
            chessBoard.chessPieces.remove(pos.chessPiece);
        }

        pos.setText("");
        pos.setChessPiece(null);
        pos.setGraphic(null);
        pos.setDisable(true);
    }

    private static List<ChessDirection> removeDuplicates(List<ChessDirection> coords) {
        coords =  new ArrayList<>(new LinkedHashSet<>(coords));
        return coords;
    }
}

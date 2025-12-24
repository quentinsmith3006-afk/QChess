package qchess.chess.logic;

import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import qchess.chess.chessmen.Pawn;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.annotations.Xray;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.interfaces.SpecialCapture;
import qchess.chess.create.interfaces.SpecialPiece;
import qchess.chess.create.special.Enpassant;
import qchess.chess.logic.event.CaptureEvent;
import qchess.chess.logic.event.EnpassantEvent;
import qchess.chess.logic.event.MovementEvent;

import javax.swing.text.Position;
import java.util.*;

class Move {
    public static ChessBoard chessBoard;
    public static ArrayList<ChessPosition> pastPositions;
    public static ChessPosition pastChessPosition;
    public static Comparator<Coordinate> coordComparator;

    static void positionClick(ChessPosition position) {
        System.out.println("Hello1");
        ChessPiece chessPiece = position.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement
        if (chessPiece == null) {
            System.out.println("MOVEMENT OCCURRED");
            processNormalMovement(position, chessPositions);
            disablePastPlayablePositions();
            return;
        }
        System.out.println("Hello3");


        // Capturing
        if (pastChessPosition != null) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            if (pastChessPiece != null && pastChessPiece.getTeam() != chessPiece.getTeam()) {
                processCapture(position);
                disablePastPlayablePositions();
                return;
            }
        }
        System.out.println("Hello4");


        disablePastPlayablePositions();

        // Playable Squares Refiner
        playableSquaresRefinery(chessPiece, position, chessPositions);
    }

    private static void processNormalMovement(ChessPosition position, ChessPosition[] chessPositions) {
        Class<? extends ChessPiece> chessPieceClass = pastChessPosition.getChessPiece().getClass();

        int pastBtnId = pastChessPosition.coordinate.getBtnID();
        int currentBtnId = position.coordinate.getBtnID();
        boolean isPieceTwoAway = Math.abs(pastBtnId - currentBtnId) == 16;

        removeSpecialPieces();

        // Hard coded piece
        if (chessPieceClass.equals(Pawn.class) && isPieceTwoAway) {
            ChessPiece enpassPawn = pastChessPosition.getChessPiece();

            int momentum = enpassPawn.getTeam() == Team.WHITE ? 1 : -1;
            int newBtnId = enpassPawn.getBtnID() + ChessBoard.width * momentum;
            ChessPosition onePlaceUnderPawn = chessPositions[newBtnId];
            Coordinate onePlaceUnder = new Coordinate(newBtnId);

            Enpassant enpassant = new Enpassant(onePlaceUnder, enpassPawn.getTeam());
            enpassant.setPosition(chessBoard.getChessPositions()[newBtnId]);
            chessBoard.chessPieces.add(enpassant);
            onePlaceUnderPawn.setChessPiece(enpassant);
        }

        move(pastChessPosition, position);
    }

    private static void processCapture(ChessPosition position) {
        ChessPiece movedChessPiece = pastChessPosition.getChessPiece();
        boolean hasChessPiece = movedChessPiece != null;

        if (hasChessPiece) {
            ChessPiece capturedPiece = position.getChessPiece();

            if (movedChessPiece instanceof SpecialCapture) {
                System.out.println("Hello3");
                List<ChessDirection> capturableSquares = ((SpecialCapture) movedChessPiece).getCapturableMoves();
                for (ChessDirection vector : capturableSquares) {
                    if (vector.contains(position.coordinate)) {
                        System.out.println(capturedPiece + " CAPTURED");
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
                System.out.println(capturedPiece + " CAPTURED");

                capture(pastChessPosition, position);
            }

            removeSpecialPieces();
        }
    }

    private static void playableSquaresRefinery(ChessPiece chessPiece, ChessPosition position, ChessPosition[] chessPositions) {
        pastPositions = new ArrayList<>();
        List<ChessDirection> pieceVectors = ChessAnnotation.applyAnnotations(chessPiece);
        pastChessPosition = position;

        if (position.getChessPiece() != null) {
            if (chessPiece instanceof SpecialCapture) {
                List<ChessDirection> capturableSquares = ((SpecialCapture)chessPiece).getCapturableMoves();
                for (ChessDirection direction : capturableSquares) {
                    for (Coordinate coord : direction) {
                        ChessPosition posOfCord = chessPositions[coord.getBtnID()];
                        if (posOfCord.getChessPiece() != null) {
                            posOfCord.setDisable(false);
                            pastPositions.add(posOfCord);
                            if (!ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {
                                break;
                            }
                        }
                    }
                }
            }

            for (ChessDirection direction : pieceVectors) {
                for (Coordinate coord : direction) {
                    ChessPosition posOfCoord = chessPositions[coord.getBtnID()];

                    System.out.println(posOfCoord.getChessPiece() + " " + posOfCoord.coordinate);
                    boolean posHasChessPiece = posOfCoord.getChessPiece() != null;
                    System.out.println(ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class));
                    if (posHasChessPiece && !ChessAnnotation.hasAnnotation(chessPiece.getClass(), Xray.class)) {
                        break;
                    }

                    System.out.println(posOfCoord.coordinate);
                    posOfCoord.setDisable(false);

                    pastPositions.add(posOfCoord);
                }
            }
        }
    }

    private static void removeSpecialPieces() {
        System.out.println("Removing special pieces...");
        for (int i = 0; i < chessBoard.chessPieces.size(); i++) {
            ChessPiece chessPiece = chessBoard.chessPieces.get(i);

            ChessPosition chessPosition = chessPiece.getPosition();
            if (chessPiece instanceof SpecialPiece) {
                System.out.println("Removing special piece " + chessPiece.getName());
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
                if (pos != null && pos.getChessPiece() == null) {
                    //System.out.println(pos.coordinate + "coord w/o chesspiece");
                    pos.setDisable(true);
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
        futurePos.setGraphic(chessPiece.getGraphic());

        chessPiece.setPosition(futurePos);
        chessPiece.setCoordinate(futurePos.coordinate);

        //System.out.println(pastPos.coordinate);
        //System.out.println(futurePos.coordinate);

        remove(pastPos, false);

        chessBoard.fireEvent(new MovementEvent(chessPiece));
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

        pos.setChessPiece(null);
        pos.setGraphic(null);
        pos.setDisable(true);
    }

    private static List<ChessDirection> removeDuplicates(List<ChessDirection> coords) {
        coords =  new ArrayList<>(new LinkedHashSet<>(coords));
        return coords;
    }
}

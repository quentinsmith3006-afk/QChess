package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.annotations.Xray;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.logic.event.CaptureEvent;
import qchess.chess.logic.event.MovementEvent;

import java.util.*;

class Move {
    public static ChessBoard chessBoard;
    public static ChessPosition[] pastPositions;
    public static ChessPosition pastChessPosition;
    public static Comparator<Coordinate> coordComparator;

    static void positionClick(ChessPosition position) {

        System.out.println("Hello");

        ChessPiece chessPiece = position.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement
        if (position.getChessPiece() == null) {
            move(pastChessPosition, position);

            disablePastPlayablePositions();
            return;
        }

        System.out.println("Hello2");

        // Capturing
        if (pastChessPosition != null ) {
            ChessPiece pastChessPiece = pastChessPosition.getChessPiece();
            boolean hasChessPiece = pastChessPiece != null;

            if (hasChessPiece) {
                Team movedPieceTeam = position.getChessPiece().getTeam();
                Team capturedPieceTeam = pastChessPiece.getTeam();

                if (movedPieceTeam != capturedPieceTeam) {
                    capture(pastChessPosition, position);
                    return;
                }
            }
        }

        disablePastPlayablePositions();

        List<ChessDirection> pieceVectors = ChessAnnotation.applyAnnotations(chessPiece);

        // Playable Squares Refiner
        int totalPlayableMoves = getTotalPlayableMoves(pieceVectors); // Change to consider vectorization
        pastPositions = new ChessPosition[totalPlayableMoves];
        pastChessPosition = position;

        int i = 0;
        if (position.getChessPiece() != null) {
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

                    pastPositions[i++] = posOfCoord;
                }
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

    private static int getTotalPlayableMoves(List<ChessDirection> directions) {
        int count = 0;
        for (ChessDirection direction : directions) {
            count += direction.getSize();
        }

        return count;
    };

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

        chessBoard.fireEvent(new MovementEvent());
    }

    public static void capture(ChessPosition pastPos, ChessPosition futurePos) {
        if (futurePos.getChessPiece() == null) {
            throw new IllegalStateException("nothing to capture");
        }

        remove(futurePos, true);
        move(pastPos, futurePos);

        chessBoard.fireEvent(new CaptureEvent());
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

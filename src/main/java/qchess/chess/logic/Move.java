package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.logic.event.CaptureEvent;
import qchess.chess.logic.event.MovementEvent;

class Move {
    public static ChessBoard chessBoard;
    public static ChessPosition[] pastPositions;
    public static ChessPosition pastChessPosition;

    static void positionClick(ChessPosition position) {
        ChessPiece chessPiece = position.getChessPiece();
        ChessPosition[] chessPositions = chessBoard.getChessPositions();

        // Normal movement
        if (position.getChessPiece() == null) {
            move(pastChessPosition, position);

            disablePastPlayablePositions();
            return;
        }

        // Capturing
        if (pastChessPosition != null ) {
            boolean hasChessPiece = pastChessPosition.getChessPiece() != null;

            if (hasChessPiece) {
                Team movedPieceTeam = position.getChessPiece().getTeam();
                Team capturedPieceTeam = pastChessPosition.getChessPiece().getTeam();

                if (movedPieceTeam != capturedPieceTeam) {
                    capture(pastChessPosition, position);
                }
                return;
            }
        }


        disablePastPlayablePositions();

        int totalPlayableMoves = chessPiece.getPlayableMoves().size();
        pastPositions = new ChessPosition[totalPlayableMoves];
        pastChessPosition = position;

        int i = 0;
        if (position.getChessPiece() != null) {
            for (Coordinate coord : chessPiece.getPlayableMoves()) {
                System.out.println(coord.getRow() + "," + coord.getCol());

                chessPositions[coord.getBtnID()].setDisable(false);
                pastPositions[i++] = chessPositions[coord.getBtnID()];
            }
        }
    }

    private static void disablePastPlayablePositions() {
        if (pastPositions != null) {
            for (ChessPosition pos : pastPositions) {
                if (pos.getChessPiece() == null) {
                    System.out.println(pos.coordinate + "coord w/o chesspiece");
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

        System.out.println(pastPos.coordinate);
        System.out.println(futurePos.coordinate);

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
        pos.setChessPiece(null);
        pos.setGraphic(null);
        pos.setDisable(true);

        if (capture) {
            // stuff I gotta do when it captures smt/completely removes a piece
        }
    }
}

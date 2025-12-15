package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Chiral;
import qchess.chess.create.Coordinate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

class Move {
    public static ChessBoard chessBoard;

    private static boolean checkChiral(ChessPiece chessPiece) {
        Class<? extends ChessPiece> chessPieceClass = chessPiece.getClass();

        Method[] chessPieceMethod = chessPieceClass.getMethods();

        Annotation[] chessPieceAnnotations = chessPieceClass.getAnnotations();

        for (Annotation annotation : chessPieceAnnotations) {
            if (annotation instanceof Chiral) {
                return true;
            }
        }

        return false;
    }

    public static void positionClick(Position position) {
        if (position.getChessPiece() != null) {
            List<Coordinate> coords = position.getChessPiece().getPlayableMoves();

            if (checkChiral(position.getChessPiece())) {

            }

            for (Coordinate coord : position.getChessPiece().getPlayableMoves()) {
                System.out.println(coord);
            }
        }
    }
}

package qchess.chess.create.exceptions;

import qchess.chess.create.ChessPiece;

public class PieceInWayException extends RuntimeException {
    private ChessPiece pieceInWay;

    public PieceInWayException(String message, ChessPiece pieceInWay) {
        super(message);
        System.out.println(pieceInWay + " PIECE IN WAY " + pieceInWay.getCoordinate());
        this.pieceInWay = pieceInWay;
    }

    public ChessPiece getPieceInWay() {
        return pieceInWay;
    }
}

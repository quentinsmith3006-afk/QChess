package qchess.chess.create.exceptions;

import qchess.chess.create.ChessPiece;

public class ChessPieceCyclicalDependencyException extends RuntimeException {
    ChessPiece chessPiece;
    public ChessPieceCyclicalDependencyException(ChessPiece chessPiece, String message) {
        super(message);

        this.chessPiece = chessPiece;
    }

    public ChessPiece getChessPiece() {
        return chessPiece;
    }
}

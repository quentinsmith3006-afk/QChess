package qchess.chess.create.interfaces;

import qchess.chess.create.ChessPiece;

import java.util.List;

public interface Operation<T> {
    public void operate(ChessPiece chessPiece, List<T> playableMoves);
}

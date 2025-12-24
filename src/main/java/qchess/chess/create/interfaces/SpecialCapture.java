package qchess.chess.create.interfaces;

import org.jetbrains.annotations.NotNull;
import qchess.chess.logic.ChessBoard;

public interface SpecialPiece {
    @NotNull
    public ChessBoard getChessBoard();
}

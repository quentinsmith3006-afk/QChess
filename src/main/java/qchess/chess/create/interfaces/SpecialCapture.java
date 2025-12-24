package qchess.chess.create.interfaces;

import org.jetbrains.annotations.NotNull;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.logic.ChessBoard;

import java.util.List;

public interface SpecialCapture {
    public List<ChessDirection> getCapturableMoves();
}

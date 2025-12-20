package qchess.chess.create.interfaces;

import java.util.List;

public interface SymmetryOperation <T> {
    public void operate(T focal, List<T> playableMoves);
}

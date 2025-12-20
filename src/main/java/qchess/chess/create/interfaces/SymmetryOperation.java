package qchess.chess.create.interfaces;

import java.util.List;

public interface SymmetryOperation <T> extends Operable {
    public void operate(T focal, List<T> playableMoves);
}

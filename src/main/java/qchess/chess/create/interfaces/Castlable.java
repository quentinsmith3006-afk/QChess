package qchess.chess.create.interfaces;

import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.PieceScalar;

import java.util.HashMap;

public interface Castlable {
    public HashMap<PieceScalar, CastleVector> getCastleDirections();
    public boolean hasCastled();
    public void setHasCastled(boolean hasCastled);
}

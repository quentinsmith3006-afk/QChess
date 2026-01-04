package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.interfaces.Castlable;
import qchess.chess.create.interfaces.Checkable;
import qchess.chess.create.interfaces.SpecialPiece;

import java.util.HashMap;
import java.util.List;

public class TempCheckChessMan extends ChessPiece implements Checkable, Castlable, SpecialPiece {

    public TempCheckChessMan(Coordinate coordinate, Team team) {
        super(coordinate, team);
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        return List.of();
    }

    @Override
    public HashMap<PieceScalar, CastleVector> getCastleDirections() {
        return null;
    }

    @Override
    public boolean hasCastled() {
        return false;
    }

    @Override
    public void setHasCastled(boolean hasCastled) {

    }

    @Override
    public void setInitializedCastleDirections(HashMap<PieceScalar, CastleVector> initializedCastleDirections) {

    }

    @Override
    public HashMap<PieceScalar, CastleVector> getInitializedCastleDirections() {
        return null;
    }

    @Override
    public int deltaRow() {
        return 0;
    }

    @Override
    public int deltaCol() {
        return 0;
    }
}

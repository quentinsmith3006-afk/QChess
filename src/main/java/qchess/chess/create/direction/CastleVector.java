package qchess.chess.create.direction;

import org.jetbrains.annotations.ApiStatus;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.Arrays;
import java.util.List;

public class CastleVector extends PieceVector {
    ChessPiece castleDependent;

    public CastleVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        super(start, deltaRow, deltaCol, magnitude);
    }

    @ApiStatus.Internal
    public ChessPiece getCastleDependent() {
        return castleDependent;
    }

    @ApiStatus.Internal
    public void setCastleDependent(ChessPiece castleDependent) {
        this.castleDependent = castleDependent;
    }

    public static void addAll(List<CastleVector> directions, CastleVector... chessVectors) {
        directions.addAll(Arrays.asList(chessVectors));
    }
}

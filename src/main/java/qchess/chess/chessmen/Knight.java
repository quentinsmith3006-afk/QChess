package qchess.chess.chessmen;

import org.jetbrains.annotations.VisibleForTesting;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;
import qchess.chess.create.piecemodifiers.Xray;

import java.util.ArrayList;
import java.util.List;

@HorizonalSymmetry
@VerticalSymmetry
public class Knight extends ChessPiece {
    public Knight(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WKnight.png","/ChessAssets/BKnight.png");
        this.pieceValue = 3;
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        PieceScalar topLeftScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() + 2,getCol() - 1));
        PieceScalar leftTopScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() + 1,getCol() - 2));

        moves.add(leftTopScalar);
        moves.add(topLeftScalar);
        return moves;
    }
}

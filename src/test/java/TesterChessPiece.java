import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.BoundedPieceVector;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.UnboundedPieceVector;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.ArrayList;
import java.util.List;

@VerticalSymmetry
@HorizonalSymmetry
public class TesterChessPiece extends ChessPiece {

    public TesterChessPiece(Coordinate coordinate, Team team) {
        super(coordinate, team);
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        UnboundedPieceVector right = new UnboundedPieceVector(this.coordinate, 1, 1, BoundedPieceVector.INF);

        moves.add(right);
        return moves;
    }
}

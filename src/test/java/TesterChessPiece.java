import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.ArrayList;
import java.util.List;

@HorizonalSymmetry
@VerticalSymmetry
public class TesterChessPiece extends ChessPiece {

    public TesterChessPiece(Coordinate coordinate, Team team) {
        super(coordinate, team);
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        PieceVector bottomRightDiagonal = new PieceVector(this.coordinate, 2, 1, PieceVector.INF);
        PieceVector diag = new PieceVector(this.coordinate, 1, 2, PieceVector.INF);


        moves.add(bottomRightDiagonal);
        moves.add(diag);
        return moves;
    }
}

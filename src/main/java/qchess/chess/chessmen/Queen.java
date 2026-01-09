package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * Creates the raw playables for the classic Queen chess piece.
 * A Queen has playables which encompass all 4 diagnoles and all squares on its left, right, top and bottom.
 */
public class Queen extends ChessPiece {
    public Queen(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WQueen.png","/ChessAssets/BQueen.png");
        this.pieceValue = 9;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        Rook rook = new Rook(this.coordinate, this.team);
        Bishop bishop = new Bishop(this.coordinate, this.team);

        moves.addAll(rook.getPlayableDirections());
        moves.addAll(bishop.getPlayableDirections());

        return moves;
    }
}

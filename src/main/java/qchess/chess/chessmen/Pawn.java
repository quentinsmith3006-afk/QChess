package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Chiral;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.ArrayList;
import java.util.List;

@Chiral("Pawn")
public class Pawn extends ChessPiece {
    public Pawn(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WPawn.png","/ChessAssets/BPawn.png");
    }

    @Override
    public List<Coordinate> getPlayableMoves() {
        ArrayList<Coordinate> moves = new ArrayList<>();
        int row = getRow();

        if (row == 7 || row == 0) {
            return moves;
        }

        int momentum = 1;

        if (team == Team.BLACK) {
            momentum = -1;
        }

        int forwardOne = (getBtnID() + 8 * momentum);

        boolean isWhiteOnStart = (row == 1);
        boolean isBlackOnStart = (row == 6);
        if (isWhiteOnStart || isBlackOnStart) {
            int forwardTwo = (getBtnID() + 16 * momentum);
            Coordinate twoPlacesAway = new Coordinate(forwardTwo);
            moves.add(twoPlacesAway);
        }

        Coordinate onePlaceAway = new Coordinate(forwardOne);
        moves.add(onePlaceAway);

        return moves;
    }
}

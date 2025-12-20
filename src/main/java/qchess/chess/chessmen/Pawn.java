package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.annotations.HorizonalSymmetry;
import qchess.chess.create.annotations.Xray;

import java.util.ArrayList;
import java.util.List;

@HorizonalSymmetry
public class Pawn extends ChessPiece {
    private boolean enpassant;

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

        int momentum = team == Team.BLACK ? -1 : 1;

        boolean isWhiteOnStart = (row == 1);
        boolean isBlackOnStart = (row == 6);
        if (isWhiteOnStart || isBlackOnStart) {
            int forwardTwo = (getBtnID() + 16 * momentum);
            Coordinate twoPlacesAway = new Coordinate(forwardTwo);
            moves.add(twoPlacesAway);
        }

        int forwardOne = (getBtnID() + 8 * momentum);
        Coordinate onePlaceAway = new Coordinate(forwardOne);
        moves.add(onePlaceAway);

        return moves;
    }
}

package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.List;

public class Pawn extends ChessPiece {
    public Pawn(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WPawn.png","/ChessAssets/BPawn.png");
    }


    @Override
    public List<Coordinate> getPlayableMoves(Coordinate startingPosition) {
        return List.of();
    }
}

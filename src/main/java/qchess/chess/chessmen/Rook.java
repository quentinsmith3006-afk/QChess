package qchess.chess.chessmen;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.List;

public class Rook extends ChessPiece {
    public Rook(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WRook.png","/ChessAssets/BRook.png");
        this.pieceValue = 5;
    }

    @Override
    public List<Coordinate> getPlayableMoves() {
        return List.of();
    }
}

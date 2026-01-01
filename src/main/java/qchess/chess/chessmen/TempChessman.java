package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.create.interfaces.SpecifyCapture;
import qchess.chess.logic.promotion.PromotionSquares;

import java.util.ArrayList;
import java.util.List;

public class TempChessman extends ChessPiece implements Promotable, SpecifyCapture {
    public TempChessman(Coordinate coordinate, Team team) {
        super(coordinate, team);
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        ArrayList<ChessDirection> directions = new ArrayList<>();
        directions.add(new PieceScalar(this.coordinate, new Coordinate(this.getRow() + 1, this.getCol())));
        return directions;
    }

    @Override
    public ChessPiece[] getPromotionOptions() {
        return new ChessPiece[] {
                new Knight(this.coordinate, this.team)
        };
    }

    @Override
    public PromotionSquares getBlackPromotionSquares() {
        return new PromotionSquares(7);
    }

    @Override
    public PromotionSquares getWhitePromotionSquares() {
        return new PromotionSquares(0);
    }

    @Override
    public List<ChessDirection> getCapturableMoves() {
        ArrayList<ChessDirection> capturableMoves = new ArrayList<>();
        capturableMoves.add(new PieceVector(this.coordinate, 1, 0));

        return capturableMoves;
    }
}

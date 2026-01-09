package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.interfaces.Enpassantable;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.create.interfaces.SpecifyCapture;
import qchess.chess.create.special.Enpassant;
import qchess.chess.logic.promotion.PromotionSquares;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * Creates the raw playables for the classic Pawn chess piece.
 * A Pawn has playables which are directly infront of it; however, it also has specific capturables which
 * are on its direct left and right diagnole.
 */
public class Pawn extends ChessPiece implements SpecifyCapture, Promotable, Enpassantable {

    public Pawn(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WPawn.png","/ChessAssets/BPawn.png");

        canResetNumMovesToDraw = true;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        ArrayList<ChessDirection> moves = new ArrayList<>();
        PieceScalar scalar = new PieceScalar(this.coordinate);

        int row = getRow();

        int momentum = team == Team.BLACK ? -1 : 1;

        boolean isWhiteOnStart = (row == 1);
        boolean isBlackOnStart = (row == 6);
        if (isWhiteOnStart || isBlackOnStart) {
            int forwardTwo = (getBtnID() + 16 * momentum);
            Coordinate twoPlacesAway = new Coordinate(forwardTwo);
            scalar.addCoordinate(twoPlacesAway);
        }

        int forwardOne = (getBtnID() + 8 * momentum);
        Coordinate onePlaceAway = new Coordinate(forwardOne);
        scalar.addCoordinate(onePlaceAway);

        moves.add(scalar);

        return moves;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> getCapturableMoves() {
        List<ChessDirection> moves = new ArrayList<>();

        int momentum = team == Team.BLACK ? -1 : 1;

        PieceVector bottomRightVector = new PieceVector(this.coordinate, momentum, 1, 1);
        PieceVector bottomLeftVector = new PieceVector(this.coordinate, momentum, -1, 1);

        moves.add(bottomRightVector);
        moves.add(bottomLeftVector);
        return moves;
    }

    /** {@inheritDoc} */
    @Override
    public ChessPiece[] getPromotionOptions() {
        return new ChessPiece[]{
                new Bishop(this.coordinate, this.team),
                new Knight(this.coordinate, this.team),
                new Queen(this.coordinate, this.team),
                new Rook(this.coordinate, this.team)
        };
    }

    /** {@inheritDoc} */
    @Override
    public PromotionSquares getBlackPromotionSquares() {
        return new PromotionSquares(0);
    }

    /** {@inheritDoc} */
    @Override
    public PromotionSquares getWhitePromotionSquares() {
        return new PromotionSquares(7);
    }
}

package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.*;
import qchess.chess.create.interfaces.Castlable;
import qchess.chess.create.interfaces.Checkable;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.*;

/**
 * @author Quentin Smith
 *
 * Creates the raw playables for the classic King chess piece.
 * A King has playables which only surround the entire chess piece.
 */
@HorizonalSymmetry
@VerticalSymmetry
public class King extends ChessPiece implements Checkable, Castlable {
    HashMap<PieceScalar, CastleVector> initializedCastleDirections;
    ChessPiece[] castleDependents;
    public boolean castled;

    /**
     * Creates a classic king chess piece.
     * @param position position of the chess piece.
     * @param team team that the King is on.
     * @param castleDependents specific pieces to be able to castle with the king.
     */
    public King(Coordinate position, Team team, Collection<ChessPiece> castleDependents) {
        super(position, team, "/ChessAssets/WKing.png","/ChessAssets/BKing.png");
        this.castleDependents = castleDependents.toArray(new ChessPiece[0]);
    }

    /**
     * Creates a classic king chess piece.
     * @param position position of the chess piece.
     * @param team team that the King is on.
     * @param castleDependents specific pieces to be able to castle with the king.
     */
    public King(Coordinate position, Team team, ChessPiece... castleDependents) {
        super(position, team, "/ChessAssets/WKing.png","/ChessAssets/BKing.png");
        this.castleDependents = castleDependents;
    }

    /**
     * Creates a classic king chess piece.
     * @param position position of the chess piece.
     * @param team team that the King is on.
     */
    public King(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WKing.png","/ChessAssets/BKing.png");
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        PieceScalar bottomLeft = new PieceScalar(this.coordinate, new Coordinate(getRow() - 1, getCol() - 1));
        PieceScalar bottom = new PieceScalar(this.coordinate, new Coordinate(getRow() - 1, getCol()));
        PieceScalar left = new PieceScalar(this.coordinate,  new Coordinate(getRow(), getCol() - 1));

        moves.add(bottomLeft);
        moves.add(bottom);
        moves.add(left);

        return moves;
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<PieceScalar, CastleVector> getCastleDirections() {
        HashMap<PieceScalar, CastleVector> moves = new HashMap<>();

        CastleVector leftVector = new CastleVector(this.coordinate, 0, -1, PieceVector.INF, "LEFT " + this.team);
        CastleVector rightVector = new CastleVector(this.coordinate, 0, 1, PieceVector.INF, "RIGHT " + this.team);

        // represents the square where the user can click to castle
        PieceScalar leftCastlePlayable =  new PieceScalar(this.coordinate, new Coordinate(getRow(), getCol() - 2));
        PieceScalar rightCastlePlayable =  new PieceScalar(this.coordinate, new Coordinate(getRow(), getCol() + 2));

        if (castleDependents != null) {
            for (ChessPiece piece : castleDependents) {
                if (piece.getCol() - startCoordinate.getCol() < 0) {
                    leftVector = new Vector960(this.coordinate, piece);
                    leftCastlePlayable =  new PieceScalar(this.coordinate, new Coordinate(getRow(), getCol() - 2));
                } else if (piece.getCol() - startCoordinate.getCol() > 0) {
                    rightVector = new Vector960(this.coordinate, piece);
                    rightCastlePlayable =  new PieceScalar(this.coordinate, new Coordinate(getRow(), getCol() + 2));
                }

            }

            moves.put(leftCastlePlayable, leftVector);
            moves.put(rightCastlePlayable, rightVector);

            return moves;
        }

        moves.put(leftCastlePlayable, leftVector);
        moves.put(rightCastlePlayable, rightVector);

        return moves;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasCastled() {
        return castled;
    }

    /** {@inheritDoc} */
    @Override
    public void setHasCastled(boolean hasCastled) {
        castled = hasCastled;
    }

    /** {@inheritDoc} */
    @Override
    public void setInitializedCastleDirections(HashMap<PieceScalar, CastleVector> initializedCastleDirections) {
        this.initializedCastleDirections = initializedCastleDirections;
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<PieceScalar, CastleVector> getInitializedCastleDirections() {
        return initializedCastleDirections;
    }
}

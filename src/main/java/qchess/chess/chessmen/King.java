package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.interfaces.Castlable;
import qchess.chess.create.interfaces.Checkable;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@HorizonalSymmetry
@VerticalSymmetry
public class King extends ChessPiece implements Checkable, Castlable {
    HashMap<PieceScalar, CastleVector> initializedCastleDirections;
    public boolean castled;

    public King(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WKing.png","/ChessAssets/BKing.png");
    }

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

    @Override
    public HashMap<PieceScalar, CastleVector> getCastleDirections() {
        HashMap<PieceScalar, CastleVector> moves = new HashMap<>();

        CastleVector leftVector = new CastleVector(this.coordinate, 0, -1, PieceVector.INF);
        CastleVector rightVector = new CastleVector(this.coordinate, 0, 1, PieceVector.INF);

        // represents the square where the user can click to castle
        PieceScalar leftCastlePlayable =  new PieceScalar(this.coordinate, new Coordinate(getRow(), getCol() - 2));
        PieceScalar rightCastlePlayable =  new PieceScalar(this.coordinate, new Coordinate(getRow(), getCol() + 2));

        moves.put(leftCastlePlayable, leftVector);
        moves.put(rightCastlePlayable, rightVector);

        return moves;
    }

    @Override
    public boolean hasCastled() {
        return castled;
    }

    @Override
    public void setHasCastled(boolean hasCastled) {
        castled = hasCastled;
    }

    @Override
    public void setInitializedCastleDirections(HashMap<PieceScalar, CastleVector> initializedCastleDirections) {
        this.initializedCastleDirections = initializedCastleDirections;
    }

    @Override
    public HashMap<PieceScalar, CastleVector> getInitializedCastleDirections() {
        return initializedCastleDirections;
    }
}

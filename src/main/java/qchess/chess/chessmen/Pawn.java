package qchess.chess.chessmen;

import org.jetbrains.annotations.NotNull;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.annotations.HorizonalSymmetry;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.interfaces.SpecialCapture;
import qchess.chess.logic.ChessBoard;

import java.util.ArrayList;
import java.util.List;

@HorizonalSymmetry
public class Pawn extends ChessPiece implements SpecialCapture {
    public boolean capturableByEnpassant;

    public Pawn(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WPawn.png","/ChessAssets/BPawn.png");
    }

    @Override
    public List<ChessDirection> getPlayableMoves() {
        ArrayList<ChessDirection> moves = new ArrayList<>();
        PieceScalar scalar = new PieceScalar(this.coordinate);

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
            scalar.addCoordinate(twoPlacesAway);
        }

        int forwardOne = (getBtnID() + 8 * momentum);
        Coordinate onePlaceAway = new Coordinate(forwardOne);
        scalar.addCoordinate(onePlaceAway);

        moves.add(scalar);
        return moves;
    }

    @Override
    public List<ChessDirection> getCapturableMoves() {
        List<ChessDirection> moves = new ArrayList<>();
        PieceScalar scalar = new PieceScalar(this.coordinate);

        int momentum = team == Team.BLACK ? -1 : 1;

        scalar.addCoordinate(new Coordinate(getRow() + momentum, getCol() + 1));
        scalar.addCoordinate(new Coordinate(getRow() + momentum, getCol() - 1));

        moves.add(scalar);
        return moves;
    }
}

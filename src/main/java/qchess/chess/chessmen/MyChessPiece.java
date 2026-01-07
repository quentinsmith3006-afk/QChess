package qchess.chess.chessmen;

import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceScalar;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyChessPiece extends ChessPiece {
    static final String WhiteTeamGraphic = "file:myWhiteTeamGraphic.png";
    static final String BlackTeamGraphic = "file:myBlackTeamGraphic.png";

    public MyChessPiece(Coordinate coordinate, Team team) {
        super(coordinate, team, WhiteTeamGraphic, BlackTeamGraphic);
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> directions = new ArrayList<>();

        PieceScalar bottomLeftScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() + 2,getCol() - 1));
        PieceScalar leftBottomScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() + 1,getCol() - 2));

        PieceScalar bottomRightScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() + 2,getCol() + 1));
        PieceScalar rightBottomScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() + 1,getCol() + 2));

        PieceScalar topLeftScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() - 2,getCol() - 1));
        PieceScalar leftTopScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() - 1,getCol() - 2));

        PieceScalar topRightScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() - 2,getCol() + 1));
        PieceScalar rightTopScalar = new PieceScalar(this.coordinate, new Coordinate(getRow() - 1,getCol() + 2));

        Collections.addAll(directions,
                bottomLeftScalar,
                leftBottomScalar,
                bottomRightScalar,
                rightBottomScalar,
                topLeftScalar,
                leftTopScalar,
                topRightScalar,
                rightTopScalar
        );
        return directions;
    }
}

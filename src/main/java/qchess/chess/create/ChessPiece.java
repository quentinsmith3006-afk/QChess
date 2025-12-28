package qchess.chess.create;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.logic.ChessAnnotation;
import qchess.chess.logic.ChessPosition;

import java.util.List;

public abstract class ChessPiece {
    protected ChessPosition position;
    protected Coordinate coordinate;
    protected final Coordinate startCoordinate;
    protected boolean pinned;
    protected String name;
    protected Team team;
    protected ImageView graphic;
    protected int pieceValue;
    protected boolean hasMoved = false;

    public ChessPiece(Coordinate coordinate, Team team, String WhiteTeamGraphic, String BlackTeamGraphic) {
        this.startCoordinate = coordinate;
        this.coordinate = coordinate;
        this.name = this.getClass().getSimpleName();
        this.team = team;

        if (WhiteTeamGraphic != null && BlackTeamGraphic != null) {
            if (team == Team.WHITE) {
                Image img = new Image(
                        getClass().getResource(WhiteTeamGraphic).toExternalForm()
                );
                this.graphic = new ImageView(img);
            } else if (team == Team.BLACK) {
                Image img = new Image(
                        getClass().getResource(BlackTeamGraphic).toExternalForm()
                );
                this.graphic = new ImageView(img);
            }

            assert this.graphic != null;
            this.graphic.setFitHeight(40);
            this.graphic.setFitWidth(40);

        }
    }

    public ChessPiece(Coordinate coordinate, Team team) {
        this(coordinate, team, null, null);
    }

    public List<ChessDirection> getPlayableDirections() {
        List<ChessDirection> moves = null;
        try {
            moves = ChessAnnotation.applyAnnotations(this);
        } catch (NullPointerException npe) {
            throw new IllegalStateException("Either there is no chessboard or chess piece is null");
        }

        return moves;
    }

    public boolean isOnStart() {
        return startCoordinate.equals(coordinate);
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void wasMoved() {
        this.hasMoved = true;
    }

    public Coordinate getCoordinate() {return coordinate;}

    public void setCoordinate(Coordinate coordinate) {this.coordinate = coordinate;}

    public ChessPosition getPosition() {return position;}

    public void setPosition(ChessPosition position) {this.position = position;}

    public boolean isPinned() {
        return pinned;
    }

    public Team getTeam() {return team;}

    public ImageView getGraphic() {return this.graphic;}

    public String getName() {return name;}

    public int getBtnID() {
        return this.coordinate.btnID;
    }

    public int getRow() {
        return this.coordinate.row;
    }

    public int getCol() {return this.coordinate.col;}

    public void setGraphic(ImageView graphic) {this.graphic = graphic;}

    public void setBtnID(int btnID) {
        this.coordinate.btnID = btnID;
    }

    public void setRow(int row) {
        this.coordinate.row = row;
    }

    public void setCol(int col) {
        this.coordinate.col = col;
    }

    public abstract List<ChessDirection> getRawPlayableDirections();

    @Override
    public String toString() {
        return name;
    }
}

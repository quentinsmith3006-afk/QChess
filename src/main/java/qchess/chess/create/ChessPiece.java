package qchess.chess.create;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.logic.ChessPosition;

import java.util.List;

public abstract class ChessPiece {
    protected ChessPosition position;
    protected Coordinate coordinate;
    protected boolean pinned;
    protected String name;
    protected Team team;
    protected ImageView graphic;
    protected int pieceValue;

    public ChessPiece(Coordinate coordinate, Team team, String WhiteTeamGraphic, String BlackTeamGraphic) {
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

            this.graphic.setFitHeight(50);
            this.graphic.setFitWidth(50);

        }
    }

    public ChessPiece(Coordinate coordinate, Team team) {
        this(coordinate, team, null, null);
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

    public abstract List<ChessDirection> getPlayableMoves();

    @Override
    public String toString() {
        return name;
    }
}

package qchess.chess.create;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.logic.ChessAnnotation;
import qchess.chess.logic.ChessPosition;

import java.util.List;
import java.util.Objects;

/**
 * @author Quentin Smith
 *
 * {@code ChessPiece} is the general representation of a basic chess piece.
 * {@link qchess.chess.create.Coordinate}
 * {@link qchess.chess.logic.ChessPosition}
 */
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
    protected boolean canResetNumMovesToDraw = false;

    /**
     * Creates a chess piece with a white and black team graphic.
     * @param coordinate coordinate of the chess piece on the chess board.
     * @param team team of the chess piece.
     * @param WhiteTeamGraphic image for chess pieces on the white team.
     * @param BlackTeamGraphic image for chess pieces on the black team.
     */
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

    /**
     * Creates a chess piece with a null white team graphic and null black team graphic.
     * @param coordinate coordinate of the chess piece on the chess board.
     * @param team team of the chess piece.
     */
    public ChessPiece(Coordinate coordinate, Team team) {
        this(coordinate, team, null, null);
    }

    /**
     * This applies annotation modifiers to the chess piece's playable directions and
     * returns the result.
     *
     * @throws IllegalStateException when there is no chess board.
     * @return the refined movement pattern of the chess piece.
     */
    public List<ChessDirection> getPlayableDirections() {
        List<ChessDirection> moves;
        try {
            moves = ChessAnnotation.applyAnnotations(this, MovesType.PLAYABLES);
        } catch (NullPointerException npe) {
            throw new IllegalStateException("Either there is no chessboard");
        }

        return moves;
    }

    /**
     * @return true if the chess is on its original coordinate and false otherwise.
     */
    public boolean isOnStart() {
        return startCoordinate.equals(coordinate);
    }

    /**
     * @return true if the chess piece moved at <b>any point</b> during the game.
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Sets the piece to being in a state of "moved."
     */
    public void wasMoved() {
        this.hasMoved = true;
    }

    /**
     * @return the current coordinate/location of the chess piece.
     */
    public Coordinate getCoordinate() {return coordinate;}

    /**
     * @param coordinate new location of the chess piece.
     */
    public void setCoordinate(Coordinate coordinate) {this.coordinate = coordinate;}

    /**
     * @return the position/button that the chess piece is located on.
     */
    public ChessPosition getPosition() {return position;}

    /**
     * @param position new position of the chess piece.
     */
    public void setPosition(ChessPosition position) {this.position = position;}

    /**
     * @return true if the chess piece is pinned to a {@code qchess.chess.create.interfaces.Checkable} and false otherwise.
     */
    public boolean isPinned() {
        return pinned;
    }

    /**
     * @return true if a chess piece can reset the number of moves until a draw occurs (in normal chess this is the Pawn) and false otherwise.
     * <i>For more information: 50 move rule chess</i>
     */
    public boolean canResetNumMovesToDraw() {
        return canResetNumMovesToDraw;
    }

    /**
     * @param pinned sets this chess piece to being in a state of pinned.
     */
    public void setPinned(boolean pinned) {this.pinned = pinned;}

    /**
     * @return team of the chess piece.
     */
    public Team getTeam() {return team;}

    /**
     * @return the chess piece's graphic/image representation if there is one.
     */
    public ImageView getGraphic() {return this.graphic;}

    /**
     * @return the class name of the chess piece.
     */
    public String getName() {return name;}

    /**
     * @return the btnID location of the chess piece.
     */
    public int getBtnID() {
        return this.coordinate.btnID;
    }

    /**
     * @return the row of the chess piece's coordinate.
     */
    public int getRow() {
        return this.coordinate.row;
    }

    /**
     * @return the col of the chess piece's coordinate.
     */
    public int getCol() {return this.coordinate.col;}

    /**
     * @param graphic new graphic representation of the chess piece.
     */
    public void setGraphic(ImageView graphic) {this.graphic = graphic;}


    /**
     * Playable directions hold the vector/scalar locations where a chess piece can move.
     * Each vector/scalar in the returned list defines the chess piece's movement pattern.
     *
     * This method returns the raw playable directions. This means that it returns
     * playable directions that have not had any annotation modifiers applied.
     *
     * @return the raw movement pattern of the chess piece.
     */
    public abstract List<ChessDirection> getRawPlayableDirections();

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return ((ChessPiece) obj).coordinate.equals(coordinate) && ((ChessPiece) obj).name.equals(name);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(coordinate, name);
    }
}

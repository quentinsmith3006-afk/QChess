package qchess.chess.create.direction;

import org.jetbrains.annotations.NotNull;
import qchess.chess.create.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * General class for representing chess piece movement patterns.
 *
 * {@code start} is never added to {@code coordinates} to keep separation
 * between a chess piece and its possible moves.
 */
public abstract class ChessDirection implements Iterable<Coordinate> {
    final ArrayList<Coordinate> coordinates;
    final Coordinate start;

    /**
     * @param start anchor point.
     */
    public ChessDirection(Coordinate start) {
        coordinates = new ArrayList<>();
        this.start = start;
    }

    /**
     * Sorts coordinates based on distance from the {@code start} coordinate.
     * @param coords list of coordinates to be sorted.
     */
    void sort(List<Coordinate> coords) {
        for (int i = 0; i < coords.size(); i++) {
            int smallestCoordIndex = i;
            for (int j = i + 1; j < coords.size(); j++) {
                if (distance(coords.get(smallestCoordIndex)) > distance(coords.get(j))) {
                    smallestCoordIndex = j;
                }
            }

            Coordinate temp = coords.get(i);
            coords.set(i, coords.get(smallestCoordIndex));
            coords.set(smallestCoordIndex, temp);
        }
    }

    /**
     * Grabs the distance from {@code focal} to {@code other}.
     * @param focal coordinate from.
     * @param other coordinate to.
     * @return int representing the discrete distance between the 2 coordinates.
     */
    public static int distance(Coordinate focal, Coordinate other) {
        int originalRow = focal.getRow();
        int originalCol = focal.getCol();
        int otherRow = other.getRow();
        int otherCol = other.getCol();
        return Math.abs(originalRow - otherRow) + Math.abs(originalCol - otherCol);
    }

    /**
     * Grabs the distance from {@code start} to {@code other}.
     * @param other coordinate to.
     * @return int representing the discrete distance between the 2 coordinates.
     */
    public int distance(Coordinate other) {
        return distance(start, other);
    }

    /**
     * @param coord coordinate to search for.
     * @return true if the ChessDirection contains the coordinate and false otherwise.
     */
    public boolean contains(Coordinate coord) {
        return coordinates.contains(coord);
    }

    /**
     * @return Chess direction starting from the origin point {@code start}.
     */
    public ArrayList<Coordinate> getDirectionFromOrigin() {
        ArrayList<Coordinate> direction = new ArrayList<>(coordinates);
        direction.add(start);

        this.sort(direction);
        return direction;
    }

    /**
     * @return the amount of coordinates contained in the ChessDirection.
     */
    public int getSize() {
        return coordinates.size();
    }

    /**
     * Inverses the chess direction.
     * @return inversed chess direction based on the focal point {@code start}.
     */
    public abstract ChessDirection inverse();

    /**
     * @return horizontally reflected chess direction.
     */
    // Fix the return type bruh.
    public abstract List<ChessDirection> horizontalReflection();

    /**
     * @return vertically reflected chess direction.
     */
    // Fix the return type bruh.
    public abstract List<ChessDirection> verticalReflection();

    /** {@inheritDoc} */
    @Override
    public @NotNull Iterator<Coordinate> iterator() {
        return coordinates.iterator();
    }
}

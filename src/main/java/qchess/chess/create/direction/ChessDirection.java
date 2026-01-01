package qchess.chess.create.direction;

import org.jetbrains.annotations.NotNull;
import qchess.chess.create.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class ChessDirection implements Iterable<Coordinate> {
    final ArrayList<Coordinate> coordinates;
    final Coordinate start;

    public ChessDirection(Coordinate start) {
        coordinates = new ArrayList<>();
        this.start = start;
    }

    void sort(List<Coordinate> coords, Coordinate focal) {
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

    public static int distance(Coordinate focal, Coordinate other) {
        int originalRow = focal.getRow();
        int originalCol = focal.getCol();
        int otherRow = other.getRow();
        int otherCol = other.getCol();
        return Math.abs(originalRow - otherRow) + Math.abs(originalCol - otherCol);
    }

    public int distance(Coordinate other) {
        return distance(start, other);
    }

    public boolean contains(Coordinate coord) {
        return coordinates.contains(coord);
    }

    public ArrayList<Coordinate> getDirectionFromOrigin() {
        ArrayList<Coordinate> direction = new ArrayList<>(coordinates);
        direction.add(start);
        return direction;
    }

    public int getSize() {
        return coordinates.size();
    }

    public static void addAll(List<ChessDirection> directions, ChessDirection... chessDirections) {
        directions.addAll(Arrays.asList(chessDirections));
    }

    public abstract ChessDirection inverse();

    public abstract List<ChessDirection> horizontalReflection();

    public abstract List<ChessDirection> verticalReflection();

    @Override
    public @NotNull Iterator<Coordinate> iterator() {
        return coordinates.iterator();
    }
}

package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.MovesType;
import qchess.chess.create.SpecificReflection;
import qchess.chess.create.exceptions.ChessPieceCyclicalDependencyException;
import qchess.chess.create.interfaces.SpecifyCapture;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.interfaces.ChessOperation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * Evaluates chess piece based annotations to return chess piece playables with different modifications to then be processed
 * by {@code qchess.chess.logic.MoveLogic}.
 * {@link qchess.chess.create.interfaces.SpecifyCapture}
 * {@link qchess.chess.create.piecemodifiers.HorizonalSymmetry}
 * {@link qchess.chess.create.piecemodifiers.VerticalSymmetry}
 * {@link qchess.chess.create.piecemodifiers.Xray}
 * @param annotate Annotation to scan for.
 * @param operation Action to run if {@code annotate} is found.
 */
public record ChessAnnotation(Class<? extends Annotation> annotate, ChessOperation operation) {
    static ArrayList<ChessAnnotation> chessAnnotations;

    /**
     * @param chessPiece Chess piece which produced {@code moves}
     * @param moves Moves to be transformed.
     * @param type Type of moves, either CAPTURE or PLAYABLES.
     */
    void applyAnnotation(ChessPiece chessPiece, List<ChessDirection> moves, MovesType type) {
        Class<? extends ChessPiece> chessPieceClass = chessPiece.getClass();
        Annotation[] annotations = chessPieceClass.getAnnotations();
        Annotation anno = chessPiece.getClass().getAnnotation(annotate);

        for (Annotation annotation : annotations) {
            if (anno != null && anno.equals(annotation)) {
                if (chessPiece instanceof SpecifyCapture) {
                    if (anno instanceof HorizonalSymmetry) {
                        applyToSpecificMoves(chessPiece, ((HorizonalSymmetry) anno).value(), type, moves, operation);
                    } else if (anno instanceof VerticalSymmetry) {
                        applyToSpecificMoves(chessPiece, ((VerticalSymmetry) anno).value(), type, moves, operation);
                    } // else
                } else {
                    this.operation.operate(chessPiece, moves);
                } // else
            } // if
        } // for-each
    }

    /**
     * Applies modifications to chess pieces depending on how the annotations are specified. It can  apply them
     * to capturables, Playables or both. Only chess pieces that are instances of SpecifyCapture should choose because
     * it has both capturables and playables. Any chess piece which isn't of type {@code SpecifyCapture} assumes its
     * playables are its capturables and thus has no real choice as to how the annotations are applied.
     * @param chessPiece chess piece which produces {@code playablesMoves}.
     * @param reflection reflection type {@see qchess.chess.create.SpecificReflection}
     * @param type Type of moves, either CAPTURE or PLAYABLES.
     * @param moves Moves to be transformed.
     * @param operation Action to run.
     */
    void applyToSpecificMoves(ChessPiece chessPiece, SpecificReflection reflection, MovesType type, List<ChessDirection> moves, ChessOperation operation) {
        switch (reflection) {
            case PLAYABLES:
                if (type == MovesType.PLAYABLES) {
                    this.operation.operate(chessPiece, moves);
                } // if
                break;
            case CAPTURE:
                if (type == MovesType.SPECIFY_CAPTURE) {
                    this.operation.operate(chessPiece, moves);
                } // if
                break;
            default:
                this.operation.operate(chessPiece, moves);
        } // switch

    }

    /**
     * Attempts to apply all annotations which are stored in {@code chessAnnotations} to the chess piece's raw playable moves.
     * @throws ChessPieceCyclicalDependencyException when a 2 chess pieces use each-others playable moves.
     * @param chessPiece Chess piece to be analyzed.
     * @param type Type of the playable moves.
     * @return the new playables with the applied annotation operations.
     */
    public static List<ChessDirection> applyAnnotations(ChessPiece chessPiece, MovesType type) {
        try {
            List<ChessDirection> playableMoves = chessPiece.getRawPlayableDirections();

            if (chessPiece instanceof SpecifyCapture specifyCapture && type == MovesType.SPECIFY_CAPTURE) {
                playableMoves = specifyCapture.getCapturableMoves();
            } // if

            for (ChessAnnotation annotation : chessAnnotations) {
                annotation.applyAnnotation(chessPiece, playableMoves, type);
            } // for-each

            return playableMoves;
        } catch (StackOverflowError e) {
            throw new ChessPieceCyclicalDependencyException(chessPiece, chessPiece.getClass() + " has a cyclical dependency with another chess piece!");
        } // catch
    }

    /**
     * Adds a {@code qchess.chess.logic.ChessAnnotation} object to {@code chessAnnotations} ArrayList.
     * @param chessAnnotation chess annotation to add to storage.
     */
    public static void addAnnotation(ChessAnnotation chessAnnotation) {
        chessAnnotations.add(chessAnnotation);
    }

    /**
     * Adds a {@code qchess.chess.logic.ChessAnnotation} objects to {@code chessAnnotations} ArrayList.
     * @param chessAnnotationVarArg chess annotations to add to storage.
     */
    public static void addAllAnnotations(ChessAnnotation... chessAnnotationVarArg) {
        for (ChessAnnotation annotation : chessAnnotationVarArg) {
            addAnnotation(annotation);
        } // for-each
    }

    /**
     * Adds a chess annotation to the chess annotations storage.
     * The purpose of this method is for users to add their own custom ChessAnnotations.
     *
     * <b>You cannot remove a chess annotation once added.</b>
     *
     * <b>UNTESTED</b>
     * @param chessAnnotation chess annotation to add.
     * @return chess annotations currently in storage.
     */
    public static boolean addChessAnnotation(ChessAnnotation chessAnnotation) {
        if (chessAnnotations == null) {
            chessAnnotations = new ArrayList<ChessAnnotation>();
        } // if
        chessAnnotations.add(chessAnnotation);
        return new ArrayList<>(chessAnnotations).add(chessAnnotation);
    }

    /**
     * Determines if a chess piece class has the specific annotation.
     * @param clazz class to search in.
     * @param annotation annotation to search for
     * @return true if the {@code clazz} has the {@code annotation} and false otherwise.
     */
    public static boolean hasAnnotation(Class<? extends ChessPiece> clazz, Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    /**
     * Initializes hard-coded chess annotations.
     */
    static void chessAnnotationsInit() {
        chessAnnotations = new ArrayList<ChessAnnotation>();

        ChessAnnotation horizontalSym = initHorizontalAnnotation();

        ChessAnnotation verticalSym = initVerticalAnnotation();

        addAllAnnotations(horizontalSym, verticalSym);
    }

    /**
     * Initializes {@link qchess.chess.create.piecemodifiers.HorizonalSymmetry} chess annotation.
     * @return chess annotation representation of HorizontalSymmetry.
     */
    private static ChessAnnotation initHorizontalAnnotation() {
        ChessOperation horOperation = (ChessPiece chessPiece, List<ChessDirection> playableMoves) -> {

            List<ChessDirection> reflectedMoves = new ArrayList<>();
            for (ChessDirection direction : playableMoves) {
                reflectedMoves.addAll(direction.horizontalReflection());
            } // for-each

            playableMoves.addAll(reflectedMoves);
        };

        return new ChessAnnotation(HorizonalSymmetry.class, horOperation);
    }

    /**
     * Initializes {@link qchess.chess.create.piecemodifiers.VerticalSymmetry} chess annotation.
     * @return chess annotation representation of HorizontalSymmetry.
     */
    private static ChessAnnotation initVerticalAnnotation() {
        ChessOperation verOperation = (ChessPiece chessPiece, List<ChessDirection> playableMoves) -> {
            List<ChessDirection> reflectedMoves = new ArrayList<>();
            for (ChessDirection direction : playableMoves) {
                reflectedMoves.addAll(direction.verticalReflection());
            } // for-each

            playableMoves.addAll(reflectedMoves);
        };
        return new ChessAnnotation(VerticalSymmetry.class, verOperation);
    }
}

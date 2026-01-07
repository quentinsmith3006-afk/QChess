package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.PlayablesType;
import qchess.chess.create.SpecificReflection;
import qchess.chess.create.exceptions.ChessPieceCyclicalDependencyException;
import qchess.chess.create.interfaces.SpecifyCapture;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.interfaces.ChessOperation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public record ChessAnnotation(Class<? extends Annotation> annotate, ChessOperation<ChessDirection> operation) {
    static ArrayList<ChessAnnotation> chessAnnotations;

    void applyAnnotation(ChessPiece chessPiece, List<ChessDirection> playableMoves, PlayablesType type) {
        Class<? extends ChessPiece> chessPieceClass = chessPiece.getClass();
        Annotation[] annotations = chessPieceClass.getAnnotations();
        Annotation anno = chessPiece.getClass().getAnnotation(annotate);

        for (Annotation annotation : annotations) {
            if (anno != null && anno.equals(annotation)) {
                if (chessPiece instanceof SpecifyCapture) {
                    if (anno instanceof HorizonalSymmetry) {
                        applyToSpecificMoves(chessPiece, ((HorizonalSymmetry) anno).value(), type, playableMoves, operation);
                    } else if (anno instanceof VerticalSymmetry) {
                        applyToSpecificMoves(chessPiece, ((VerticalSymmetry) anno).value(), type, playableMoves, operation);
                    }
                } else {
                    this.operation.operate(chessPiece, playableMoves);
                }
            }
        }
    }

    void applyToSpecificMoves(ChessPiece chessPiece, SpecificReflection reflection, PlayablesType type, List<ChessDirection> playableMoves, ChessOperation<ChessDirection> operation) {
        switch (reflection) {
            case PLAYABLES:
                if (type == PlayablesType.PLAYABLES) {
                    this.operation.operate(chessPiece, playableMoves);
                }
                break;
            case CAPTURE:
                if (type == PlayablesType.SPECIFY_CAPTURE) {
                    this.operation.operate(chessPiece, playableMoves);
                }
                break;
            default:
                this.operation.operate(chessPiece, playableMoves);
        }

    }

    public static List<ChessDirection> applyAnnotations(ChessPiece chessPiece, PlayablesType type) {
        try {
            List<ChessDirection> playableMoves = chessPiece.getRawPlayableDirections();

            if (chessPiece instanceof SpecifyCapture specifyCapture && type == PlayablesType.SPECIFY_CAPTURE) {
                playableMoves = specifyCapture.getCapturableMoves();
            }

            for (ChessAnnotation annotation : chessAnnotations) {
                annotation.applyAnnotation(chessPiece, playableMoves, type);
            }

            return playableMoves;
        } catch (StackOverflowError e) {
            throw new ChessPieceCyclicalDependencyException(chessPiece, chessPiece.getClass() + " has a cyclical dependency with another chess piece!");
        }
    }

    public static void addAnnotation(ChessAnnotation chessAnnotation) {
        chessAnnotations.add(chessAnnotation);
    }

    public static void addAllAnnotations(ChessAnnotation... chessAnnotationVarArg) {
        for (ChessAnnotation annotation : chessAnnotationVarArg) {
            addAnnotation(annotation);
        }
    }

    public static boolean addChessAnnotation(ChessAnnotation chessAnnotation) {
        if (chessAnnotations == null) {
            chessAnnotations = new ArrayList<ChessAnnotation>();
        }
        return chessAnnotations.add(chessAnnotation);
    }

    public static boolean hasAnnotation(Class<? extends ChessPiece> clazz, Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    static void chessAnnotationsInit() {
        chessAnnotations = new ArrayList<ChessAnnotation>();

        ChessAnnotation horizontalSym = initHorizontalAnnotation();

        ChessAnnotation verticalSym = initVerticalAnnotation();

        addAllAnnotations(horizontalSym, verticalSym);
    }

    private static ChessAnnotation initHorizontalAnnotation() {
        ChessOperation<ChessDirection> horOperation = (ChessPiece chessPiece, List<ChessDirection> playableMoves) -> {

            List<ChessDirection> reflectedMoves = new ArrayList<>();
            for (ChessDirection direction : playableMoves) {
                reflectedMoves.addAll(direction.horizontalReflection());
            }

            playableMoves.addAll(reflectedMoves);
        };

        return new ChessAnnotation(HorizonalSymmetry.class, horOperation);
    }

    private static ChessAnnotation initVerticalAnnotation() {
        ChessOperation<ChessDirection> verOperation = (ChessPiece chessPiece, List<ChessDirection> playableMoves) -> {
            List<ChessDirection> reflectedMoves = new ArrayList<>();
            for (ChessDirection direction : playableMoves) {
                reflectedMoves.addAll(direction.verticalReflection());
            }

            playableMoves.addAll(reflectedMoves);
        };
        return new ChessAnnotation(VerticalSymmetry.class, verOperation);
    }
}

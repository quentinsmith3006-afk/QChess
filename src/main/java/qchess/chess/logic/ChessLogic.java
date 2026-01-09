package qchess.chess.logic;

/**
 * @author Quentin Smith
 *
 * All logic based classes extend chess logic. Chess logic simply holds the chess board which all other Logics
 * rely on.
 */
public abstract class ChessLogic {
    protected ChessBoard chessBoard;

    /**
     * Initializes a chess logic with a given chess board.
     * @param chessBoard chess board to apply logic.
     */
    public ChessLogic(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }
}

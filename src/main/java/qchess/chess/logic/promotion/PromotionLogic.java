package qchess.chess.logic.promotion;

import javafx.stage.Stage;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;
import qchess.chess.logic.ChessLogic;
import qchess.chess.logic.ChessPosition;
import qchess.chess.logic.MoveLogic;
import qchess.chess.logic.event.PostPromotionEvent;

/**
 * @author Quentin Smith
 *
 * Logic that pertains to promotion is contained here.
 */
public class PromotionLogic extends ChessLogic {
    Promotable promotableChessPiece;
    MoveLogic moveLogic;
    Stage stage;

    /**
     * @param stageToDisable Stage that will be disabled upon promotion.
     * @param promotableChessPiece Chess piece that can be promoted.
     * @param chessBoard chess board where the logic operates.
     */
    public PromotionLogic(Stage stageToDisable, Promotable promotableChessPiece, ChessBoard chessBoard) {
        super(chessBoard);
        this.promotableChessPiece = promotableChessPiece;
        this.stage = stageToDisable;
        this.moveLogic = chessBoard.getMoveLogic();
    }

    /**
     * @param promotableChessPiece Chess piece that can be promoted.
     * @param chessBoard chess board where the logic operates.
     */
    public PromotionLogic(Promotable promotableChessPiece, ChessBoard chessBoard) {
        super(chessBoard);
        this.promotableChessPiece = promotableChessPiece;
        this.moveLogic = chessBoard.getMoveLogic();
    }

    /**
     * Promotes {@code promotableChessPiece} to {@code promotionPiece}.
     * @param promotionPiece this is what the original piece will promote to.
     */
    public void promote(ChessPiece promotionPiece) {
        /*
        If you want to promote a piece manually you have to make a new promotion logic for each promotion.
        -> For now, I will hopefully fix it.
         */
        ChessPiece originalPiece = (ChessPiece) promotableChessPiece;

        ChessPosition pos = chessBoard.chessPositions[originalPiece.getBtnID()];
        pos.setChessPiece(promotionPiece);
        if (promotionPiece.getGraphic() != null) {
            pos.setGraphic(promotionPiece.getGraphic());
            pos.setText("");
        } else {
            pos.setText(promotionPiece.getName());
        }
        promotionPiece.setPosition(pos);
        promotionPiece.setCoordinate(pos.getCoordinate());

        moveLogic.scanForCheck(promotionPiece, pos);

        chessBoard.chessPieces.remove(originalPiece);
        chessBoard.chessPieces.add(promotionPiece);

        if (stage != null && stage.isShowing()) {
            stage.close();
        }

        chessBoard.fireEvent(new PostPromotionEvent(promotionPiece));

        chessBoard.unpause();
    }
}

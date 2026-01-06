package qchess.chess.logic.promotion;

import javafx.stage.Stage;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;
import qchess.chess.logic.ChessLogic;
import qchess.chess.logic.ChessPosition;
import qchess.chess.logic.MoveLogic;
import qchess.chess.logic.event.PostPromotionEvent;

public class PromotionLogic extends ChessLogic {
    Promotable promotableChessPiece;
    MoveLogic moveLogic;
    Stage stage;

    public PromotionLogic(Stage stageToDisable, Promotable promotableChessPiece, ChessBoard chessBoard) {
        super(chessBoard);
        this.promotableChessPiece = promotableChessPiece;
        this.stage = stageToDisable;
        this.moveLogic = chessBoard.getMoveLogic();
    }

    public PromotionLogic(Promotable promotableChessPiece, ChessBoard chessBoard) {
        super(chessBoard);
        this.promotableChessPiece = promotableChessPiece;
        this.moveLogic = chessBoard.getMoveLogic();
    }

    public void promote(ChessPiece promotionPiece) {
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

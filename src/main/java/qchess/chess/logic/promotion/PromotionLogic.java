package qchess.chess.logic.promotion;

import javafx.stage.Stage;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;
import qchess.chess.logic.ChessLogic;
import qchess.chess.logic.ChessPosition;
import qchess.chess.logic.Move;

public class PromotionLogic extends ChessLogic {
    Promotable promotableChessPiece;
    Stage stage;

    public PromotionLogic(Stage stageToDisable, Promotable promotableChessPiece, ChessBoard chessBoard) {
        super(chessBoard);
        this.promotableChessPiece = promotableChessPiece;
        this.stage = stageToDisable;
    }

    public PromotionLogic(Promotable promotableChessPiece, ChessBoard chessBoard) {
        super(chessBoard);
        this.promotableChessPiece = promotableChessPiece;
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

        Move.scanForCheck(promotionPiece, pos);

        chessBoard.chessPieces.remove(originalPiece);
        chessBoard.chessPieces.add(promotionPiece);

        if (stage != null && stage.isShowing()) {
            stage.close();
        }

        chessBoard.unpause();
    }
}

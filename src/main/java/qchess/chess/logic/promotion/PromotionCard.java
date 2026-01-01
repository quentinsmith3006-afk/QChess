package qchess.chess.logic.promotion;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;

public class PromotionCard extends VBox {
    ChessPiece promotionPiece;
    Button promoteButton;
    PromotionLogic promotionLogic;

    public PromotionCard(PromotionLogic promotionLogic, ChessPiece promotionPiece) {
        this.promoteButton = initPromoteButton();
        this.promotionPiece = promotionPiece;
        this.promoteButton.setGraphic(promotionPiece.getGraphic());
        this.promotionLogic = promotionLogic;

        this.getChildren().addAll(promoteButton);
    }

    public Button initPromoteButton() {
        Button promoteButton = new Button();
        promoteButton.setOnAction(e -> {
            promotionLogic.promote(promotionPiece);
        });

        promoteButton.setPrefSize(80, 80);

        return promoteButton;
    }
}

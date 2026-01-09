package qchess.chess.logic.promotion;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import qchess.chess.create.ChessPiece;

/**
 * @author Quentin Smith
 *
 * This is essentially is a button with a chess piece graphic in it.
 *
 * This changes a traditional VBox by adding a button with a chess piece graphic.
 * That button triggers a promotion.
 */
public class PromotionCard extends VBox {
    ChessPiece promotionPiece;
    Button promoteButton;
    PromotionLogic promotionLogic;

    /**
     * Creates a promotion card with promotion logic and a single promotion chess piece.
     * @param promotionLogic logic for the button to function.
     * @param promotionPiece piece to promote to.
     */
    public PromotionCard(PromotionLogic promotionLogic, ChessPiece promotionPiece) {
        this.promoteButton = initPromoteButton();
        this.promotionPiece = promotionPiece;
        this.promoteButton.setGraphic(promotionPiece.getGraphic());
        this.promotionLogic = promotionLogic;

        this.getChildren().addAll(promoteButton);
    }

    /**
     * Creates a button which has a {@code setOnAction} set to use {@code promotionLogic} to promote a piece.
     * @return button which can now promote chess pieces.
     */
    public Button initPromoteButton() {
        Button promoteButton = new Button();
        promoteButton.setOnAction(e -> {
            promotionLogic.promote(promotionPiece);
        });

        promoteButton.setPrefSize(80, 80);

        return promoteButton;
    }
}

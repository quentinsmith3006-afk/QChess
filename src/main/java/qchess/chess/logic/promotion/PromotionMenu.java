package qchess.chess.logic.promotion;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;

/**
 * @author Quentin Smith
 *
 * This graphic showcases many {@code PromotionCard} objects for users to click on and promote their chess piece.
 * {@link qchess.chess.logic.promotion.PromotionCard}
 */
public class PromotionMenu extends HBox {
    PromotionLogic promotionLogic;
    ChessPiece[] possiblePromotions;
    Promotable promotableChessPiece;
    PromotionCard[] promotionCards;
    ScrollPane scrollPane;
    HBox cardDisplay;
    ChessBoard chessBoard;

    /**
     * Creates a new stage and scene to hold promotion cards.
     * @param promotableChessPiece chess piece which is eligible for promotion.
     * @param chessBoard Chessboard which this menu operates on.
     */
    public PromotionMenu(Promotable promotableChessPiece, ChessBoard chessBoard) {
        Stage stage = new Stage();
        Scene scene = new Scene(this);
        stage.setScene(scene);
        stage.show();

        this.promotionLogic = new PromotionLogic(stage, promotableChessPiece, chessBoard);

        this.possiblePromotions = promotableChessPiece.getPromotionOptions();
        this.promotableChessPiece = promotableChessPiece;
        this.cardDisplay = new HBox();
        this.scrollPane = new ScrollPane(cardDisplay);
        this.promotionCards = initPromotionCards();
        this.chessBoard = chessBoard;

        this.getChildren().addAll(scrollPane);
        this.setPrefSize(300, 100);

        chessBoard.pause();
    }

    /**
     * Creates promotion cards and adds them {@code cardDisplay}.
     * @return Array with all created promotion cards in it.
     */
    private PromotionCard[] initPromotionCards() {
        PromotionCard[] promotionCardsResult = new PromotionCard[possiblePromotions.length];
        for (int i = 0; i < possiblePromotions.length; i++) {
            promotionCardsResult[i] = new PromotionCard(promotionLogic, possiblePromotions[i]);
            cardDisplay.getChildren().add(promotionCardsResult[i]);
        }

        return promotionCardsResult;
    }
}

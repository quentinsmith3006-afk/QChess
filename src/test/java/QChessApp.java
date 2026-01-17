import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import qchess.chess.chessmen.*;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.logic.ChessBoard;
import qchess.chess.logic.ChessPosition;
import qchess.chess.logic.event.ChessEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QChessApp extends Application {
    Stage stage;
    Scene scene;

    // Layout
    StackPane root;
    ImageView background;
    BorderPane borderPane;
    ChessBoard chessBoard;

    @Override
    public void init() {
        Image img = new Image("galaxybackground.png");
        background = new ImageView(img); // provide image
        Rook rook1 = new Rook(new Coordinate(0,1), Team.WHITE);
        Rook rook2 = new Rook(new Coordinate(0,7), Team.WHITE);


        chessBoard = ChessBoard.newBuilder()
                .normalChessBoard()
                .build()
        ;

        for (ChessPosition pos: chessBoard.chessPositions) {
            if (pos.getChessPiece() != null && (pos.getChessPiece() instanceof Rook || pos.getChessPiece() instanceof King || pos.getChessPiece() instanceof Pawn)) {

            } else {
                //pos.setChessPiece(null);
            }
        }
        // chessBoard.chessPositions[new Coordinate(5,5).getBtnID()].setChessPiece(new Bishop(new Coordinate(3,3), Team.WHITE));

        chessBoard.launchGame();


        // Maybe add more information to the castleinfo hashmap
        EventHandler<ChessEvent> eventHandler = (event) -> {
            //System.out.println();

            chessBoard.getMoveLogic().getCastleInformation().forEach((String CastleName, Boolean canCastle) -> {
                //System.out.println(CastleName + " " + canCastle);
            });
            //System.out.println();

        };

        chessBoard.setOnPieceMovement(eventHandler);

        double width = 500;
        double height = 25;

        HBox top = new HBox();
        top.setPrefSize(width, height);

        HBox bottom = new HBox();
        bottom.setPrefSize(width, height);

        HBox left = new HBox();
        left.setPrefSize(height, width);

        HBox right = new HBox();
        right.setPrefSize(height, width);


        borderPane = new BorderPane(chessBoard, top, right, bottom, left);

        root = new StackPane();
        scene = new Scene(root, 600, 600);
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;

        root.getChildren().addAll(background, borderPane);

        stage.setScene(scene);
        //stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("QChessV2");
        stage.show();
    }

    @Override
    public void stop() {

    }
}

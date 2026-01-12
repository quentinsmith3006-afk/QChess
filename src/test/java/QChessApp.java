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
import qchess.chess.logic.event.ChessEvent;

import java.io.IOException;

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
        chessBoard = ChessBoard.newBuilder()
                .emptyChessBoard()
                .add(new King(new Coordinate(0,0), Team.BLACK))
                .add(new King(new Coordinate(2,0), Team.WHITE))
                .add(new Queen(new Coordinate(1,5), Team.WHITE))
                .add(new Queen(new Coordinate(3,5), Team.WHITE))
                .build()
        ;

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

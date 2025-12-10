package qchess.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import qchess.chess.logic.ChessBoard;

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
                .build()
        ;

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
        scene = new Scene(root, 500, 500);
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;

        root.getChildren().addAll(background, borderPane);


        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("QChessV2");
        stage.show();
    }

    @Override
    public void stop() {

    }
}

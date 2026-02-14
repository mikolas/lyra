package net.mikolas.lyra.ui.control;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Visual test for custom controls.
 */
public class WidgetGallery extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a1a;");
        root.setAlignment(Pos.CENTER);

        // Dials
        HBox dialRow = new HBox(20);
        dialRow.setAlignment(Pos.CENTER);
        
        Dial cutoff = new Dial("Cutoff", 0, 127, 64);
        cutoff.setPrefSize(60, 80);
        
        Dial resonance = new Dial("Resonance", 0, 127, 20);
        resonance.setPrefSize(60, 80);
        
        Dial drive = new Dial("Drive", 0, 127, 0);
        drive.setPrefSize(60, 80);
        
        dialRow.getChildren().addAll(cutoff, resonance, drive);

        // Envelope
        EnvelopeEditor env = new EnvelopeEditor();
        env.setAttack(20);
        env.setDecay(40);
        env.setSustain(80);
        env.setRelease(30);

        root.getChildren().addAll(dialRow, env);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Lyra Widget Gallery");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

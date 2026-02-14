package net.mikolas.lyra;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.mikolas.lyra.midi.MidiManager;

/**
 * Main application entry point for Lyra.
 */
public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    // Initialize MIDI service
    MidiManager.getInstance().initialize();

    // Set AtlantaFX theme
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

    // Load Librarian view
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/mikolas/lyra/ui/LibrarianView.fxml"));
    Parent root = loader.load();
    net.mikolas.lyra.ui.LibrarianController controller = loader.getController();

    // Apply custom CSS
    Scene scene = new Scene(root, 1200, 800);
    scene.getStylesheets().add(getClass().getResource("/net/mikolas/lyra/ui/librarian.css").toExternalForm());

    primaryStage.setTitle("Lyra - Sound Librarian");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(1000);
    primaryStage.setMinHeight(700);
    primaryStage.show();

    // Cleanup on exit
    primaryStage.setOnCloseRequest(event -> {
      if (controller != null) {
        controller.shutdown();
      }
      MidiManager.getInstance().shutdown();
      net.mikolas.lyra.db.Database.shutdown();
    });
  }

  public static void main(String[] args) {
    launch(args);
  }
}

package net.mikolas.lyra.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.midi.MidiManager;
import net.mikolas.lyra.midi.MidiService;
import net.mikolas.lyra.model.Sound;

import java.io.File;
import java.sql.SQLException;

public class DumpDialogController {
  
  @FXML private RadioButton allBanksRadio;
  @FXML private ToggleGroup bankGroup;
  @FXML private CheckBox saveToDatabaseCheck;
  @FXML private CheckBox saveToFileCheck;
  @FXML private Label statusLabel;
  @FXML private ProgressBar progressBar;
  @FXML private Label progressLabel;
  @FXML private Button startBtn;
  @FXML private Button cancelBtn;
  
  private Database database;
  private File exportDirectory;
  private boolean dumping = false;
  private int totalSounds = 0;
  private int receivedSounds = 0;
  
  public void setDatabase(Database database) {
    this.database = database;
  }
  
  @FXML
  public void initialize() {
    cancelBtn.setText("Close");
  }
  
  @FXML
  private void handleStart() {
    MidiService midi = MidiManager.getInstance().getService();
    if (midi == null || !midi.isConnected()) {
      showError("MIDI not connected", "Please connect to Blofeld first");
      return;
    }
    
    if (!saveToDatabaseCheck.isSelected() && !saveToFileCheck.isSelected()) {
      showError("No destination", "Select at least one import destination");
      return;
    }
    
    // Select export directory if saving to file
    if (saveToFileCheck.isSelected()) {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Select Export Directory");
      exportDirectory = chooser.showSaveDialog(startBtn.getScene().getWindow());
      if (exportDirectory != null) {
        exportDirectory = exportDirectory.getParentFile();
      } else {
        return; // User cancelled
      }
    }
    
    dumping = true;
    startBtn.setDisable(true);
    cancelBtn.setText("Cancel");
    
    // Set up sound dump callback
    midi.setSoundDumpCallback(this::onSoundReceived);
    
    // Determine bank
    RadioButton selected = (RadioButton) bankGroup.getSelectedToggle();
    String bankText = selected.getText();
    
    if (bankText.equals("All Banks (1024 sounds)")) {
      totalSounds = 1024;
      statusLabel.setText("Requesting all banks...");
      midi.requestAllDumps(null, this::onProgress)
          .thenAccept(count -> onComplete());
    } else {
      totalSounds = 128;
      int bank = bankText.charAt(5) - 'A'; // "Bank A" -> 0
      statusLabel.setText("Requesting bank " + bankText.charAt(5) + "...");
      midi.requestBankDump(bank, null, this::onProgress)
          .thenAccept(count -> onComplete());
    }
    
    receivedSounds = 0;
    updateProgress();
  }
  
  @FXML
  private void handleCancel() {
    if (dumping) {
      dumping = false;
      statusLabel.setText("Cancelled");
      startBtn.setDisable(false);
      cancelBtn.setText("Close");
    } else {
      closeDialog();
    }
  }
  
  private void onProgress(Integer count) {
    if (!dumping) return;
    
    Platform.runLater(() -> {
      receivedSounds = count;
      updateProgress();
    });
  }
  
  private void onSoundReceived(Sound sound) {
    if (!dumping) return;
    
    Platform.runLater(() -> {
      if (saveToDatabaseCheck.isSelected() && database != null) {
        try {
          // Check if sound already exists (by bank+program unique combo)
          Sound existing = database.sounds.queryBuilder()
              .where()
              .eq("bank", sound.getBank())
              .and()
              .eq("program", sound.getProgram())
              .queryForFirst();
          
          if (existing != null) {
            // Update existing sound
            sound.setId(existing.getId());
          }
          
          database.sounds.createOrUpdate(sound);
        } catch (SQLException e) {
          System.err.println("Failed to save sound to database: " + e.getMessage());
        }
      }
      
      if (saveToFileCheck.isSelected() && exportDirectory != null) {
        try {
          String bankName = (char) ('A' + sound.getBank()) + "";
          String filename = String.format("%s%03d_%s.mid", 
              bankName, sound.getProgram() + 1, sanitizeFilename(sound.getName()));
          File file = new File(exportDirectory, filename);
          // TODO: Write MIDI file
        } catch (Exception e) {
          System.err.println("Failed to save sound to file: " + e.getMessage());
        }
      }
    });
  }
  
  private String sanitizeFilename(String name) {
    return name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
  
  private void onComplete() {
    Platform.runLater(() -> {
      dumping = false;
      statusLabel.setText("Dump complete!");
      startBtn.setDisable(false);
      cancelBtn.setText("Close");
    });
  }
  
  private void updateProgress() {
    double progress = totalSounds > 0 ? (double) receivedSounds / totalSounds : 0;
    progressBar.setProgress(progress);
    progressLabel.setText(receivedSounds + " / " + totalSounds + " sounds received");
  }
  
  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
  
  private void closeDialog() {
    Stage stage = (Stage) cancelBtn.getScene().getWindow();
    stage.close();
  }
}

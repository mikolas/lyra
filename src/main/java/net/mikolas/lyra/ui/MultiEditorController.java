package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.mikolas.lyra.db.MultiRepository;
import net.mikolas.lyra.exception.MidiException;
import net.mikolas.lyra.midi.MidiManager;
import net.mikolas.lyra.midi.MidiService;
import net.mikolas.lyra.model.MultiPatch;
import net.mikolas.lyra.model.MultiPart;
import net.mikolas.lyra.ui.control.Dial;
import net.mikolas.lyra.util.FxUtils;

import java.io.IOException;
import java.text.NumberFormat;

public class MultiEditorController {

    @FXML private HBox headerPane;
    @FXML private ScrollPane mixerScrollPane;
    @FXML private HBox mixerHBox;
    @FXML private Dial masterVolumeDial;
    @FXML private TextField tempoField;
    @FXML private net.mikolas.lyra.ui.control.MultiDisplay multiDisplay;
    @FXML private ToggleButton groupEditToggle;
    @FXML private ToggleGroup viewToggleGroup;
    @FXML private VelocityEditorController velocityEditorController;
    @FXML private VBox velocityEditor;
    @FXML private KeyEditorController keyEditorController;
    @FXML private VBox keyEditor;

    private MultiPatch multiPatch;
    private int currentMultiIndex = 0;

    @FXML
    public void initialize() {
        FxUtils.setTextFieldFormatter(tempoField, NumberFormat.getIntegerInstance());
        
        // View switching logic
        mixerScrollPane.visibleProperty().bind(viewToggleGroup.getToggles().get(0).selectedProperty());
        velocityEditor.visibleProperty().bind(viewToggleGroup.getToggles().get(1).selectedProperty());
        keyEditor.visibleProperty().bind(viewToggleGroup.getToggles().get(2).selectedProperty());

        // Setup MIDI callback
        setupMidiCallback();
    }

    public void showVelocityView() {
        viewToggleGroup.getToggles().get(1).setSelected(true);
    }

    public void showKeysView() {
        viewToggleGroup.getToggles().get(2).setSelected(true);
    }
    
    private void setupMidiCallback() {
        MidiService midi = MidiManager.getInstance().getService();
        if (midi != null) {
            midi.setMultiDumpCallback(this::handleIncomingMultiDump);
        }
    }

    private void handleIncomingMultiDump(MultiPatch incoming) {
        javafx.application.Platform.runLater(() -> {
            multiDisplay.setLoading(false); // Clear visual loading state
            if (this.multiPatch != null) {
                // Update current patch with incoming data
                this.multiPatch.updateFromMidi(incoming.getData());
                
                // Trust Hardware ID: Sync our local index tracker to what the hardware sent
                int prog = incoming.getData()[1] & 0xFF;
                this.currentMultiIndex = prog;
                
                updateIndexDisplay();
            }
        });
    }

    private void updateIndexDisplay() {
        if (multiPatch != null) {
            multiDisplay.setSlotId(currentMultiIndex + 1);
            // multiDisplay.setName(multiPatch.getName()); // No longer needed if bound
        }
    }

    @FXML
    private void handlePrevMulti() {
        requestMulti(currentMultiIndex - 1);
    }

    @FXML
    private void handleNextMulti() {
        requestMulti(currentMultiIndex + 1);
    }

    private void requestMulti(int index) {
        if (index < 0) index = 127;
        if (index > 127) index = 0;
        
        MidiService midi = MidiManager.getInstance().getService();
        if (midi == null || !midi.isConnected()) {
            showError("MIDI Error", "Not connected to Blofeld.");
            return;
        }

        final int targetIndex = index;
        multiDisplay.setLoading(true);
        
        midi.switchToMultiMode()
            .thenRun(() -> {
                try {
                    Thread.sleep(300);
                    midi.requestMultiDump(0, targetIndex);
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        multiDisplay.setLoading(false);
                        showError("Request Failed", "Failed to request Multi " + targetIndex + ": " + e.getMessage());
                    });
                }
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    multiDisplay.setLoading(false);
                    showError("Mode Switch Failed", "Failed to switch to Multi mode: " + ex.getMessage());
                });
                return null;
            });

        // Watchdog: If no response in 3s, revert visual state
        javafx.animation.PauseTransition watchdog = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        watchdog.setOnFinished(e -> {
            if (multiDisplay.isLoading()) {
                multiDisplay.setLoading(false);
            }
        });
        watchdog.play();
    }

    @FXML
    private void handleSave() {
        if (multiPatch == null) return;
        MultiRepository.getInstance().save(multiPatch);
        
        MidiService midi = MidiManager.getInstance().getService();
        if (midi != null && midi.isConnected()) {
            midi.switchToMultiMode()
                .thenRun(() -> {
                    try {
                        midi.sendMultiDump(multiPatch, 127, 0);
                    } catch (MidiException e) {
                        javafx.application.Platform.runLater(() -> 
                            showError("Sync Error", "Saved to DB but failed to sync to Blofeld: " + e.getMessage())
                        );
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> 
                        showError("Mode Switch Failed", "Saved to DB but failed to switch to Multi mode: " + ex.getMessage())
                    );
                    return null;
                });
        }
    }

    @FXML
    private void handleSaveAs() {
        // TODO: Show a small naming/slot dialog
        handleSave();
    }

    @FXML
    private void handleManage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiBrowser.fxml"));
            VBox root = loader.load();
            MultiBrowserController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Multi Browser");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            
            controller.setOnSelect(selected -> {
                // Load this multi into the editor
                this.multiPatch.updateFromMidi(selected.getData());
                setMultiPatch(this.multiPatch);
                
                // Sync to hardware Edit Buffer
                MidiService midi = MidiManager.getInstance().getService();
                if (midi != null && midi.isConnected()) {
                    midi.switchToMultiMode()
                        .thenRun(() -> {
                            try {
                                midi.sendMultiDump(this.multiPatch, 127, 0);
                            } catch (MidiException e) {
                                e.printStackTrace();
                            }
                        })
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            return null;
                        });
                }
            });
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDump() {
        MidiService midi = MidiManager.getInstance().getService();
        if (midi == null || !midi.isConnected()) {
            showError("MIDI Error", "Not connected to Blofeld.");
            return;
        }
        
        midi.switchToMultiMode()
            .thenRun(() -> {
                try {
                    midi.requestMultiDump(127, 0);
                } catch (MidiException e) {
                    javafx.application.Platform.runLater(() -> 
                        showError("Dump Request Failed", "Failed to request Multi Dump: " + e.getMessage())
                    );
                }
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> 
                    showError("Mode Switch Failed", "Failed to switch to Multi mode: " + ex.getMessage())
                );
                return null;
            });
    }

    public void setMultiPatch(MultiPatch multiPatch) {
        this.multiPatch = multiPatch;
        
        // Group Editing Engine
        multiPatch.setParameterChangeListener((offset, value) -> {
            if (groupEditToggle.isSelected() && offset >= 34) {
                int relativeOffset = (offset - 34) % 24;
                int sourcePartIndex = (offset - 34) / 24;
                
                // Mirror to all other SELECTED parts
                for (int i = 0; i < 16; i++) {
                    MultiPart targetPart = multiPatch.getParts().get(i);
                    if (i != sourcePartIndex && targetPart.isSelected()) {
                        int targetOffset = 34 + (i * 24) + relativeOffset;
                        multiPatch.updateParameterSilently(targetOffset, value);
                    }
                }
            }
            
            // Send to MIDI (TODO: verify if this should happen for mirrored params too)
            // For now, let the existing MIDI logic handle it if it listens to MultiPatch
        });

        // Bind header controls
        masterVolumeDial.valueProperty().bindBidirectional(multiPatch.volumeProperty());
        tempoField.textProperty().bindBidirectional(multiPatch.tempoProperty(), new javafx.util.converter.NumberStringConverter());
        multiDisplay.nameProperty().bindBidirectional(multiPatch.nameProperty());
        
        // Update display when name changes
        multiPatch.nameProperty().addListener((obs, old, newVal) -> updateIndexDisplay());
        updateIndexDisplay();

        // Initialize specialized editors
        if (velocityEditorController != null) {
            velocityEditorController.setMultiPatch(multiPatch);
        }
        if (keyEditorController != null) {
            keyEditorController.setMultiPatch(multiPatch);
        }

        // Load strips
        mixerHBox.getChildren().clear();
        for (int i = 0; i < 16; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiStrip.fxml"));
                VBox strip = loader.load();
                MultiStripController controller = loader.getController();
                controller.bind(multiPatch.getParts().get(i));
                mixerHBox.getChildren().add(strip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
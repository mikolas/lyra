package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import net.mikolas.lyra.model.Arpeggiator;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class ArpController {

    @FXML private ComboBox<String> modeCombo;
    @FXML private ComboBox<String> patternCombo;
    @FXML private ComboBox<String> clockCombo;
    @FXML private ComboBox<String> lengthCombo;
    @FXML private ComboBox<String> directionCombo;
    @FXML private ComboBox<String> octaveRangeCombo;
    
    @FXML private Dial tempoDial;
    @FXML private Dial octaveDial;
    @FXML private ToggleButton resetBtn;

    private Arpeggiator arpeggiator;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        modeCombo.getItems().clear();
        modeCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.ARP_MODE));
        
        patternCombo.getItems().clear();
        patternCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.ARP_PATTERN));
        
        clockCombo.getItems().clear();
        clockCombo.getItems().addAll("1/1", "1/2", "1/4", "1/8", "1/16", "1/32");
        
        lengthCombo.getItems().clear();
        lengthCombo.getItems().addAll("Legato", "1/1", "1/2", "1/4", "1/8", "1/16");
        
        directionCombo.getItems().clear();
        directionCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.ARP_DIRECTION));
        
        // Note: octaveRangeCombo removed - octave controlled by dial
    }

    public void setArpeggiator(Arpeggiator arpeggiator) {
        this.arpeggiator = arpeggiator;
        bindProperties();
    }

    private void bindProperties() {
        if (arpeggiator == null) return;

        linkDialToProperty(tempoDial, arpeggiator.tempoProperty());
        linkDialToProperty(octaveDial, arpeggiator.octaveProperty());

        setupComboBoxSync(modeCombo, arpeggiator.modeProperty());
        setupComboBoxSync(patternCombo, arpeggiator.patternProperty());
        setupComboBoxSync(clockCombo, arpeggiator.clockProperty());
        setupComboBoxSync(lengthCombo, arpeggiator.lengthProperty());
        setupComboBoxSync(directionCombo, arpeggiator.directionProperty());
        // Note: octaveRangeCombo removed - octave is controlled by octaveDial
        
        // Force initial UI update
        javafx.application.Platform.runLater(() -> {
            modeCombo.getSelectionModel().select(arpeggiator.modeProperty().get());
        });

        // Reset Toggle
        resetBtn.setSelected(arpeggiator.patternResetProperty().get() > 0);
        resetBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            arpeggiator.patternResetProperty().set(newVal ? 1 : 0);
        });
        arpeggiator.patternResetProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                isUpdating = true;
                try {
                    resetBtn.setSelected(newVal.intValue() > 0);
                } finally {
                    isUpdating = false;
                }
            });
        });
    }

    private void setupComboBoxSync(ComboBox<String> combo, javafx.beans.property.IntegerProperty property) {
        // UI -> Model
        combo.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            if (isUpdating) return;
            if (newVal != null && newVal.intValue() != -1) {
                property.set(newVal.intValue());
            }
        });

        // Model -> UI
        property.addListener((obs, old, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    int val = newVal.intValue();
                    if (val >= 0 && val < combo.getItems().size()) {
                        combo.getSelectionModel().select(val);
                    }
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Initial select
        int initial = property.get();
        if (initial >= 0 && initial < combo.getItems().size()) {
            combo.getSelectionModel().select(initial);
        }
    }

    @FXML
    private void handleEditPattern() {
        if (arpeggiator == null) return;
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ArpPatternEditorView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ArpPatternEditorController controller = loader.getController();
            controller.setArpeggiator(arpeggiator);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Lyra - Arpeggiator Step Sequencer");
            stage.initModality(javafx.stage.Modality.NONE);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            
            // Apply theme/icon if available
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void linkDialToProperty(Dial dial, javafx.beans.property.IntegerProperty property) {
        dial.setValue(property.get());
        dial.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (int)newVal.doubleValue() != property.get()) {
                property.set((int)newVal.doubleValue());
            }
        });
        property.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.intValue() != (int) dial.getValue()) {
                dial.setValue(newVal.doubleValue());
            }
        });
    }
}
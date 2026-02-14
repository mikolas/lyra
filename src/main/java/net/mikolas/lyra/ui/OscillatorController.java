package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;
import net.mikolas.lyra.model.Oscillator;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class OscillatorController {

    @FXML private Label oscLabel;
    @FXML private ComboBox<String> shapeCombo;
    @FXML private Button wtBrowserBtn;
    
    @FXML private Dial octaveDial;
    @FXML private Dial semitoneDial;
    @FXML private Dial detuneDial;
    @FXML private Dial pwDial;
    @FXML private Dial bendDial;
    @FXML private Dial brillianceDial;
    @FXML private Dial fmDial;
    @FXML private Dial pwmDial;
    @FXML private Dial keytrackDial;
    
    @FXML private ComboBox<String> fmSrcCombo;
    @FXML private ComboBox<String> pwmSrcCombo;
    @FXML private ComboBox<String> pitchSrcCombo;
    @FXML private Dial pitchAmtDial;
    
    @FXML private ToggleButton syncBtn;
    @FXML private ToggleButton limitWtBtn;

    private Oscillator oscillator;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        fmSrcCombo.getItems().clear();
        fmSrcCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));

        pwmSrcCombo.getItems().clear();
        pwmSrcCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));

        pitchSrcCombo.getItems().clear();
        pitchSrcCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));

        // Setup shapeCombo CellFactory for greying out unpopulated wavetables
        shapeCombo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else {
                    setText(item);
                    int index = getIndex();
                    if (!isWavetablePopulated(index, item)) {
                        setDisable(true);
                        setStyle("-fx-text-fill: #666666;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });
        // Also update the button cell (the selected item display)
        shapeCombo.setButtonCell(shapeCombo.getCellFactory().call(null));
    }

    private boolean isWavetablePopulated(int index, String name) {
        if (index < 86) return true; // Factory shapes and wavetables
        // For User slots, if it starts with "User ", it's just a placeholder
        return name != null && !name.startsWith("User ");
    }

    @FXML
    private void handleWtBrowser() {
        if (oscillator == null) return;

        List<String> allValues = ParameterValues.getValues(ParameterValueType.OSC_SHAPES);
        if (allValues == null) return;

        // Create a searchable list of wavetables (index 5 to 127)
        java.util.List<WavetableEntry> wavetables = new java.util.ArrayList<>();
        for (int i = 5; i < allValues.size(); i++) {
            wavetables.add(new WavetableEntry(i, allValues.get(i)));
        }

        // Simple Search Dialog
        javafx.scene.control.Dialog<WavetableEntry> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Select Wavetable");
        dialog.setHeaderText("Choose a wavetable for " + oscLabel.getText());

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        javafx.scene.control.TextField filterField = new javafx.scene.control.TextField();
        filterField.setPromptText("Search wavetables...");
        
        javafx.scene.control.ListView<WavetableEntry> listView = new javafx.scene.control.ListView<>();
        javafx.collections.transformation.FilteredList<WavetableEntry> filteredData = 
            new javafx.collections.transformation.FilteredList<>(javafx.collections.FXCollections.observableArrayList(wavetables), p -> true);
        
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(wt -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return wt.name().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        listView.setCellFactory(lv -> new ListCell<WavetableEntry>() {
            @Override
            protected void updateItem(WavetableEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item.name());
                    if (!isWavetablePopulated(item.index(), item.name())) {
                        setDisable(true);
                        setStyle("-fx-text-fill: #666666;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });

        listView.setItems(filteredData);
        listView.setPrefHeight(400);
        listView.setPrefWidth(300);

        content.getChildren().addAll(filterField, listView);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        // Styling for dark theme
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("synth.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("sound-editor");

        dialog.setResultConverter(button -> {
            if (button == javafx.scene.control.ButtonType.OK) {
                WavetableEntry selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null && isWavetablePopulated(selected.index(), selected.name())) {
                    return selected;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selected -> {
            oscillator.shapeProperty().set(selected.index());
            updateShapeComboSelection();
        });
    }

    private record WavetableEntry(int index, String name) {
        @Override public String toString() { return name; }
    }

    public void setOscillator(int index, Oscillator osc) {
        this.oscillator = osc;
        this.oscLabel.setText("OSC " + (index + 1));
        
        // Show WT button only for OSC 1 & 2
        wtBrowserBtn.setVisible(index < 2);
        limitWtBtn.setVisible(index < 2);

        // Populate shapeCombo based on oscillator type
        shapeCombo.getItems().clear();
        List<String> allShapes = ParameterValues.getValues(ParameterValueType.OSC_SHAPES);
        if (allShapes != null) {
            if (index < 2) {
                // OSC 1 & 2 have all shapes and wavetables
                shapeCombo.getItems().addAll(allShapes);
            } else {
                // OSC 3 only has basic shapes
                if (allShapes.size() >= 5) {
                    shapeCombo.getItems().addAll(allShapes.subList(0, 5));
                }
            }
        }
        
        bindProperties();
    }

    private void bindProperties() {
        if (oscillator == null) return;

        linkDialToProperty(octaveDial, oscillator.octaveProperty());
        linkDialToProperty(semitoneDial, oscillator.semitoneProperty());
        linkDialToProperty(detuneDial, oscillator.detuneProperty());
        linkDialToProperty(pwDial, oscillator.pulsewidthProperty());
        linkDialToProperty(bendDial, oscillator.bendRangeProperty());
        linkDialToProperty(brillianceDial, oscillator.brillianceProperty());
        linkDialToProperty(fmDial, oscillator.fmAmountProperty());
        linkDialToProperty(pwmDial, oscillator.pwmAmountProperty());
        linkDialToProperty(keytrackDial, oscillator.keytrackProperty());
        linkDialToProperty(pitchAmtDial, oscillator.pitchModAmountProperty());

        setupComboBoxSync(shapeCombo, oscillator.shapeProperty());
        setupComboBoxSync(fmSrcCombo, oscillator.fmSourceProperty());
        setupComboBoxSync(pwmSrcCombo, oscillator.pwmSourceProperty());
        setupComboBoxSync(pitchSrcCombo, oscillator.pitchModSourceProperty());

        // Sync Toggle
        syncBtn.setSelected(oscillator.syncProperty().get() > 0);
        syncBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            oscillator.syncProperty().set(newVal ? 1 : 0);
        });
        oscillator.syncProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                isUpdating = true;
                try {
                    syncBtn.setSelected(newVal.intValue() > 0);
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Limit WT Toggle
        limitWtBtn.setSelected(oscillator.limitWtProperty().get() > 0);
        limitWtBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            oscillator.limitWtProperty().set(newVal ? 1 : 0);
        });
        oscillator.limitWtProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                isUpdating = true;
                try {
                    limitWtBtn.setSelected(newVal.intValue() > 0);
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

    private void updateShapeComboSelection() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            int val = oscillator.shapeProperty().get();
            if (val >= 0 && val < shapeCombo.getItems().size()) {
                shapeCombo.getSelectionModel().select(val);
            }
        } finally {
            isUpdating = false;
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
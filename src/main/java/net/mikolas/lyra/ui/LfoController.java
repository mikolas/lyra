package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.mikolas.lyra.model.LFO;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

/**
 * Controller for a single modular LFO component.
 */
public class LfoController {

    @FXML private Label lfoLabel;
    @FXML private ComboBox<String> shapeCombo;
    @FXML private Dial speedDial;
    @FXML private Dial phaseDial;
    @FXML private Dial delayDial;
    @FXML private Dial keytrackDial;
    @FXML private Dial fadeDial;
    @FXML private ToggleButton syncBtn;
    @FXML private ToggleButton clockedBtn;

    private LFO lfo;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        shapeCombo.getItems().clear();
        shapeCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.LFO_SHAPES));
    }

    public void setLfo(int index, LFO lfo) {
        this.lfo = lfo;
        this.lfoLabel.setText("LFO " + (index + 1));
        bindProperties();
    }

    private void bindProperties() {
        if (lfo == null) return;

        linkDialToProperty(speedDial, lfo.speedProperty());
        linkDialToProperty(phaseDial, lfo.startPhaseProperty());
        linkDialToProperty(delayDial, lfo.delayProperty());
        linkDialToProperty(keytrackDial, lfo.keytrackProperty());
        linkDialToProperty(fadeDial, lfo.fadeProperty());

        // Sync (Boolean/Integer toggle)
        syncBtn.setSelected(lfo.syncProperty().get() > 0);
        syncBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            lfo.syncProperty().set(newVal ? 127 : 0);
        });
        lfo.syncProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    syncBtn.setSelected(newVal.intValue() > 0);
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Clocked
        clockedBtn.setSelected(lfo.clockedProperty().get() > 0);
        clockedBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            lfo.clockedProperty().set(newVal ? 127 : 0);
        });
        lfo.clockedProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    clockedBtn.setSelected(newVal.intValue() > 0);
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Shape
        setupComboBoxSync(shapeCombo, lfo.shapeProperty());
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

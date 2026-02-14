package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import net.mikolas.lyra.model.CommonParameters;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class CommonSectionController {

    @FXML private ComboBox<String> allocationCombo;
    @FXML private ComboBox<String> glideModeCombo;
    @FXML private Dial unisonDetuneDial;
    @FXML private Dial glideRateDial;
    @FXML private Dial pitchBendRangeDial;
    @FXML private ToggleButton glideBtn;

    private CommonParameters common;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        allocationCombo.getItems().clear();
        allocationCombo.getItems().addAll("Poly", "Mono", "Dual", "Unison");
        glideModeCombo.getItems().clear();
        glideModeCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.GLIDE_MODE));
    }

    public void setCommon(CommonParameters common) {
        this.common = common;
        bindProperties();
    }

    private void bindProperties() {
        if (common == null) return;

        linkDialToProperty(unisonDetuneDial, common.unisonDetuneProperty());
        linkDialToProperty(glideRateDial, common.glideRateProperty());
        linkDialToProperty(pitchBendRangeDial, common.pitchModAmountProperty());

        // Allocation
        setupComboBoxSync(allocationCombo, common.allocationModeProperty());
        
        // Glide
        setupComboBoxSync(glideModeCombo, common.glideModeProperty());

        // Glide Toggle
        glideBtn.setSelected(common.glideActiveProperty().get() > 0);
        glideBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            common.glideActiveProperty().set(newVal ? 1 : 0);
        });
        common.glideActiveProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    glideBtn.setSelected(newVal.intValue() > 0);
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
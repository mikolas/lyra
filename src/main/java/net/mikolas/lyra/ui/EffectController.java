package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import net.mikolas.lyra.model.Effect;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class EffectController {

    @FXML private Label effectLabel;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Dial mixDial;
    @FXML private Dial p1Dial;
    @FXML private Dial p2Dial;
    @FXML private Dial p3Dial;
    @FXML private Dial p4Dial;
    @FXML private Dial p5Dial;

    private Effect effect;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        typeCombo.getItems().clear();
        typeCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.EFFECT_TYPE));
    }

    public void setEffect(int index, Effect effect) {
        this.effect = effect;
        this.effectLabel.setText("EFFECT " + (index + 1));
        bindProperties();
    }

    private void bindProperties() {
        if (effect == null) return;

        linkDialToProperty(mixDial, effect.mixProperty());
        linkDialToProperty(p1Dial, effect.parameterProperty(1));
        linkDialToProperty(p2Dial, effect.parameterProperty(2));
        linkDialToProperty(p3Dial, effect.parameterProperty(3));
        linkDialToProperty(p4Dial, effect.parameterProperty(4));
        linkDialToProperty(p5Dial, effect.parameterProperty(5));

        setupComboBoxSync(typeCombo, effect.typeProperty());
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

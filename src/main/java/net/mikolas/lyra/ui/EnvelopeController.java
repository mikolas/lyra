package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import net.mikolas.lyra.model.Envelope;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;
import net.mikolas.lyra.ui.control.EnvelopeEditor;

public class EnvelopeController {

    @FXML private Label envLabel;
    @FXML private ComboBox<String> modeCombo;
    @FXML private EnvelopeEditor editor;
    @FXML private Dial aDial;
    @FXML private Dial alDial;
    @FXML private Dial dDial;
    @FXML private Dial d2Dial;
    @FXML private Dial sDial;
    @FXML private Dial s2Dial;
    @FXML private Dial rDial;

    private Envelope envelope;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        modeCombo.getItems().clear();
        modeCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.ENV_MODE));
    }

    public void setEnvelope(int index, Envelope env) {
        this.envelope = env;
        String name = switch (index) {
            case 0 -> "FILTER ENV";
            case 1 -> "AMP ENV";
            case 2 -> "ENV 3";
            case 3 -> "ENV 4";
            default -> "ENV";
        };
        this.envLabel.setText(name);
        bindProperties();
    }

    private void bindProperties() {
        if (envelope == null) return;

        // Bind Editor to Model
        editor.attackProperty().bindBidirectional(envelope.attackProperty());
        editor.decayProperty().bindBidirectional(envelope.decayProperty());
        editor.sustainProperty().bindBidirectional(envelope.sustainProperty());
        editor.releaseProperty().bindBidirectional(envelope.releaseProperty());

        // Bind Dials to Model
        linkDialToProperty(aDial, envelope.attackProperty());
        linkDialToProperty(alDial, envelope.attackLevelProperty());
        linkDialToProperty(dDial, envelope.decayProperty());
        linkDialToProperty(d2Dial, envelope.decay2Property());
        linkDialToProperty(sDial, envelope.sustainProperty());
        linkDialToProperty(s2Dial, envelope.sustain2Property());
        linkDialToProperty(rDial, envelope.releaseProperty());

        // Mode
        setupComboBoxSync(modeCombo, envelope.modeProperty());
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

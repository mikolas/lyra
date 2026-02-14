package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import net.mikolas.lyra.model.CommonParameters;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class AmpController {

    @FXML private Dial volumeDial;
    @FXML private Dial velocityDial;
    @FXML private ComboBox<String> modSourceCombo;
    @FXML private Dial modAmountDial;

    private CommonParameters common;

    @FXML
    public void initialize() {
        modSourceCombo.getItems().clear();
        modSourceCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));
    }

    public void setCommon(CommonParameters common) {
        this.common = common;
        bindProperties();
    }

    private void bindProperties() {
        if (common == null) return;

        linkDialToProperty(volumeDial, common.ampVolumeProperty());
        linkDialToProperty(velocityDial, common.ampVelocityProperty());
        linkDialToProperty(modAmountDial, common.ampModAmountProperty());

        modSourceCombo.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.intValue() != -1) {
                common.ampModSourceProperty().set(newVal.intValue());
            }
        });
        modSourceCombo.getSelectionModel().select(Math.clamp(common.ampModSourceProperty().get(), 0, modSourceCombo.getItems().size() - 1));
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

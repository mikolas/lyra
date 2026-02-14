package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import net.mikolas.lyra.model.CommonParameters;
import net.mikolas.lyra.model.Filter;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class FilterController {

    @FXML private Label filterLabel;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Dial cutoffDial;
    @FXML private Dial resonanceDial;
    @FXML private Dial driveDial;
    @FXML private ComboBox<String> driveCurveCombo;
    
    @FXML private Dial envModDial;
    @FXML private Dial envVelDial;
    @FXML private Dial keytrackDial;
    @FXML private ComboBox<String> modSourceCombo;
    
    @FXML private Dial modAmtDial;
    @FXML private Dial fmDial;
    @FXML private Dial panDial;
    @FXML private ComboBox<String> panSourceCombo;
    @FXML private ComboBox<String> routingCombo;

    private Filter filter;
    private CommonParameters common;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        typeCombo.getItems().clear();
        typeCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.FILTERS));
        
        driveCurveCombo.getItems().clear();
        driveCurveCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.DRIVE_CURVE));
        
        modSourceCombo.getItems().clear();
        modSourceCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));
        
        panSourceCombo.getItems().clear();
        panSourceCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));

        routingCombo.getItems().clear();
        routingCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.FILTER_ROUTING));
    }

    public void setFilter(int index, Filter filter, CommonParameters common) {
        this.filter = filter;
        this.common = common;
        this.filterLabel.setText("FILTER " + (index + 1));
        
        routingCombo.setVisible(index == 0);
        routingCombo.setManaged(index == 0);
        
        bindProperties();
    }

    private void bindProperties() {
        if (filter == null) return;

        linkDialToProperty(cutoffDial, filter.cutoffProperty());
        linkDialToProperty(resonanceDial, filter.resonanceProperty());
        linkDialToProperty(driveDial, filter.driveProperty());
        linkDialToProperty(envModDial, filter.envAmountProperty());
        linkDialToProperty(envVelDial, filter.envVelocityProperty());
        linkDialToProperty(keytrackDial, filter.keytrackProperty());
        linkDialToProperty(modAmtDial, filter.modAmountProperty());
        linkDialToProperty(fmDial, filter.fmAmountProperty());
        linkDialToProperty(panDial, filter.panProperty());

        // Bidirectional ComboBox Sync
        setupComboBoxSync(typeCombo, filter.typeProperty());
        setupComboBoxSync(driveCurveCombo, filter.driveCurveProperty());
        setupComboBoxSync(modSourceCombo, filter.modSourceProperty());
        setupComboBoxSync(panSourceCombo, filter.panSourceProperty());

        if (common != null) {
            setupComboBoxSync(routingCombo, common.filterRoutingProperty());
        }
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

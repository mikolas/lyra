package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import net.mikolas.lyra.model.Arpeggiator;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.ui.control.Dial;

public class ArpPatternEditorController {

    @FXML private Label modeLabel;
    @FXML private Label patternLabel;
    @FXML private Label clockLabel;
    @FXML private Label tempoLabel;
    @FXML private HBox stepContainer;
    @FXML private Slider lengthSlider;
    @FXML private Label lengthLabel;

    private Arpeggiator arpeggiator;
    private StepView[] steps = new StepView[16];
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        stepContainer.setSpacing(2);
        for (int i = 0; i < 16; i++) {
            steps[i] = new StepView(i);
            stepContainer.getChildren().add(steps[i].getRoot());
        }
        
        lengthSlider.setSnapToTicks(true);
        lengthSlider.setMinorTickCount(0);
        lengthSlider.setMajorTickUnit(1);
        lengthSlider.setSkin(new net.mikolas.lyra.ui.control.SliderSkin(lengthSlider));
        
        lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            if (arpeggiator != null) {
                // UI 1-16 maps to Model 0-15
                arpeggiator.patternLengthProperty().set(newVal.intValue() - 1);
            }
            lengthLabel.setText(String.valueOf(newVal.intValue()));
        });
    }

    public void setArpeggiator(Arpeggiator arp) {
        this.arpeggiator = arp;
        
        // Header Sync
        modeLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
            () -> ParameterValues.getDisplayValue(ParameterValueType.ARP_MODE, arp.modeProperty().get()),
            arp.modeProperty()));
            
        patternLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
            () -> ParameterValues.getDisplayValue(ParameterValueType.ARP_PATTERN, arp.patternProperty().get()),
            arp.patternProperty()));

        tempoLabel.textProperty().bind(arp.tempoProperty().asString());
        
        // Initial setup for length slider: Model 0-15 -> UI 1-16
        isUpdating = true;
        try {
            int modelVal = arp.patternLengthProperty().get();
            lengthSlider.setValue(modelVal + 1);
            lengthLabel.setText(String.valueOf(modelVal + 1));
        } finally {
            isUpdating = false;
        }
        
        arp.patternLengthProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    lengthSlider.setValue(newVal.doubleValue() + 1);
                    lengthLabel.setText(String.valueOf(newVal.intValue() + 1));
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Bind steps
        for (int i = 0; i < 16; i++) {
            steps[i].bind(arp);
        }
    }

    @FXML private void handleReset() {
        for (int i = 0; i < 16; i++) {
            arpeggiator.patternStepGlideAccentProperty(i).set(64);
            arpeggiator.patternStepTimingLengthProperty(i).set(64);
        }
    }

    @FXML private void handleShiftLeft() {
        int firstGlide = arpeggiator.patternStepGlideAccentProperty(0).get();
        int firstTiming = arpeggiator.patternStepTimingLengthProperty(0).get();
        for (int i = 0; i < 15; i++) {
            arpeggiator.patternStepGlideAccentProperty(i).set(arpeggiator.patternStepGlideAccentProperty(i+1).get());
            arpeggiator.patternStepTimingLengthProperty(i).set(arpeggiator.patternStepTimingLengthProperty(i+1).get());
        }
        arpeggiator.patternStepGlideAccentProperty(15).set(firstGlide);
        arpeggiator.patternStepTimingLengthProperty(15).set(firstTiming);
    }

    @FXML private void handleShiftRight() {
        int lastGlide = arpeggiator.patternStepGlideAccentProperty(15).get();
        int lastTiming = arpeggiator.patternStepTimingLengthProperty(15).get();
        for (int i = 15; i > 0; i--) {
            arpeggiator.patternStepGlideAccentProperty(i).set(arpeggiator.patternStepGlideAccentProperty(i-1).get());
            arpeggiator.patternStepTimingLengthProperty(i).set(arpeggiator.patternStepTimingLengthProperty(i-1).get());
        }
        arpeggiator.patternStepGlideAccentProperty(0).set(lastGlide);
        arpeggiator.patternStepTimingLengthProperty(0).set(lastTiming);
    }

    @FXML private void handleRandomize() {
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            arpeggiator.patternStepGlideAccentProperty(i).set(rnd.nextInt(128));
            arpeggiator.patternStepTimingLengthProperty(i).set(rnd.nextInt(128));
        }
    }

    @FXML private void handleClose() {
        stepContainer.getScene().getWindow().hide();
    }

    private class StepView {
        private final VBox root;
        private final Dial typeDial;
        private final Label typeValueLabel;
        private final ToggleButton glideBtn;
        private final Slider accentSlider;
        private final Label accentValueLabel;
        private final Dial timingDial;
        private final Label timingValueLabel;
        private final Dial lengthDial;
        private final Label lengthValueLabel;
        private final int index;

        public StepView(int index) {
            this.index = index;
            this.root = new VBox(2);
            this.root.setAlignment(Pos.TOP_CENTER);
            this.root.setPrefWidth(65);
            this.root.getStyleClass().add("synth-panel");
            this.root.setStyle("-fx-border-color: #333; -fx-padding: 3;");

            Label stepLabel = new Label(String.format("%02d", index + 1));
            stepLabel.getStyleClass().add("tactical-label");
            stepLabel.setStyle("-fx-text-fill: #808080;");

            typeDial = new Dial("STEP", 0, 7, 0);
            typeDial.setPrefSize(40, 50);
            typeValueLabel = createValueLabel();

            glideBtn = new ToggleButton("GLIDE");
            glideBtn.getStyleClass().add("compact-toggle");
            glideBtn.setMaxWidth(Double.MAX_VALUE);

            accentSlider = new Slider(0, 7, 4);
            accentSlider.setOrientation(javafx.geometry.Orientation.VERTICAL);
            accentSlider.setPrefHeight(80);
            accentSlider.setShowTickMarks(true);
            accentSlider.setMajorTickUnit(1);
            accentSlider.setSnapToTicks(true);
            accentSlider.getStyleClass().add("accent-slider");
            accentSlider.setSkin(new net.mikolas.lyra.ui.control.SliderSkin(accentSlider));
            accentValueLabel = createValueLabel();

            timingDial = new Dial("TIMIN", 0, 7, 4);
            timingDial.setPrefSize(40, 50);
            timingValueLabel = createValueLabel();

            lengthDial = new Dial("LENGT", 0, 7, 4);
            lengthDial.setPrefSize(40, 50);
            lengthValueLabel = createValueLabel();

            this.root.getChildren().addAll(stepLabel, typeDial, typeValueLabel, glideBtn, 
                accentSlider, accentValueLabel, timingDial, timingValueLabel, lengthDial, lengthValueLabel);
        }

        private Label createValueLabel() {
            Label l = new Label("-");
            l.setStyle("-fx-font-family: 'Geist Mono'; -fx-font-size: 9px; -fx-text-fill: #00FFFF;");
            return l;
        }

        public VBox getRoot() { return root; }

        public void bind(Arpeggiator arp) {
            linkDial(typeDial, typeValueLabel, arp.getStepProperty(index, 0), ParameterValueType.ARP_STEPS);
            
            // Glide Toggle
            javafx.beans.property.IntegerProperty glideProp = arp.getStepProperty(index, 1);
            glideBtn.setSelected(glideProp.get() == 1);
            glideBtn.selectedProperty().addListener((obs, old, val) -> {
                if (isUpdating) return;
                glideProp.set(val ? 1 : 0);
            });
            glideProp.addListener((obs, old, val) -> {
                javafx.application.Platform.runLater(() -> {
                    isUpdating = true;
                    try {
                        glideBtn.setSelected(val.intValue() == 1);
                    } finally {
                        isUpdating = false;
                    }
                });
            });

            // Accent Slider
            javafx.beans.property.IntegerProperty accentProp = arp.getStepProperty(index, 2);
            accentSlider.setValue(accentProp.get());
            accentSlider.setMinorTickCount(0);
            accentSlider.setSnapToTicks(true);
            accentValueLabel.setText(ParameterValues.getDisplayValue(ParameterValueType.ARP_ACCENTS, accentProp.get()));
            
            accentSlider.valueProperty().addListener((obs, old, val) -> {
                if (isUpdating) return;
                int intVal = (int) Math.round(val.doubleValue());
                accentProp.set(intVal);
                accentValueLabel.setText(ParameterValues.getDisplayValue(ParameterValueType.ARP_ACCENTS, intVal));
            });
            
            accentProp.addListener((obs, old, val) -> {
                javafx.application.Platform.runLater(() -> {
                    isUpdating = true;
                    try {
                        accentSlider.setValue(val.doubleValue());
                        accentValueLabel.setText(ParameterValues.getDisplayValue(ParameterValueType.ARP_ACCENTS, val.intValue()));
                    } finally {
                        isUpdating = false;
                    }
                });
            });

            linkDial(timingDial, timingValueLabel, arp.getStepProperty(index, 3), ParameterValueType.ARP_TIMINGS);
            linkDial(lengthDial, lengthValueLabel, arp.getStepProperty(index, 4), ParameterValueType.ARP_LENGTHS);
        }

        private void linkDial(Dial dial, Label valueLabel, javafx.beans.property.IntegerProperty prop, ParameterValueType type) {
            dial.setValue(prop.get());
            valueLabel.setText(ParameterValues.getDisplayValue(type, prop.get()));
            
            dial.valueProperty().addListener((obs, old, val) -> {
                if (isUpdating) return;
                int intVal = (int) Math.round(val.doubleValue());
                prop.set(intVal);
                valueLabel.setText(ParameterValues.getDisplayValue(type, intVal));
            });
            
            prop.addListener((obs, old, val) -> {
                javafx.application.Platform.runLater(() -> {
                    isUpdating = true;
                    try {
                        dial.setValue(val.doubleValue());
                        valueLabel.setText(ParameterValues.getDisplayValue(type, val.intValue()));
                    } finally {
                        isUpdating = false;
                    }
                });
            });
        }
    }
}

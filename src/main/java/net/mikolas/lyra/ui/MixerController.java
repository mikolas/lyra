package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import net.mikolas.lyra.model.Mixer;
import net.mikolas.lyra.ui.control.Dial;

public class MixerController {

    @FXML private Slider osc1LevelSlider;
    @FXML private Dial osc1BalanceDial;
    @FXML private Slider osc2LevelSlider;
    @FXML private Dial osc2BalanceDial;
    @FXML private Slider osc3LevelSlider;
    @FXML private Dial osc3BalanceDial;
    @FXML private Slider rmLevelSlider;
    @FXML private Dial rmBalanceDial;
    @FXML private Slider noiseLevelSlider;
    @FXML private Dial noiseBalanceDial;
    @FXML private Dial noiseColorDial;

    private Mixer mixer;

    public void setMixer(Mixer mixer) {
        this.mixer = mixer;
        bindProperties();
    }

    private void bindProperties() {
        if (mixer == null) return;

        linkSliderToProperty(osc1LevelSlider, mixer.osc1LevelProperty());
        linkDialToProperty(osc1BalanceDial, mixer.osc1BalanceProperty());

        linkSliderToProperty(osc2LevelSlider, mixer.osc2LevelProperty());
        linkDialToProperty(osc2BalanceDial, mixer.osc2BalanceProperty());

        linkSliderToProperty(osc3LevelSlider, mixer.osc3LevelProperty());
        linkDialToProperty(osc3BalanceDial, mixer.osc3BalanceProperty());

        linkSliderToProperty(rmLevelSlider, mixer.ringModLevelProperty());
        linkDialToProperty(rmBalanceDial, mixer.ringModBalanceProperty());

        linkSliderToProperty(noiseLevelSlider, mixer.noiseLevelProperty());
        linkDialToProperty(noiseBalanceDial, mixer.noiseBalanceProperty());
        
        linkDialToProperty(noiseColorDial, mixer.noiseColorProperty());
    }

    private void linkSliderToProperty(Slider slider, javafx.beans.property.IntegerProperty property) {
        slider.setValue(property.get());
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.intValue() != property.get()) {
                property.set(newVal.intValue());
            }
        });
        property.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.intValue() != (int) slider.getValue()) {
                slider.setValue(newVal.doubleValue());
            }
        });
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

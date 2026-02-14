package net.mikolas.lyra.ui.control;

import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Interactive Envelope Editor for Lyra.
 * Visualizes ADSR/AHDSR curves and allows dragging nodes.
 */
public class EnvelopeEditor extends Control {

    public enum Mode { ADSR, AHDSR }

    private final DoubleProperty attack = new SimpleDoubleProperty(this, "attack", 0);
    private final DoubleProperty decay = new SimpleDoubleProperty(this, "decay", 64);
    private final DoubleProperty sustain = new SimpleDoubleProperty(this, "sustain", 127);
    private final DoubleProperty release = new SimpleDoubleProperty(this, "release", 20);
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(this, "mode", Mode.ADSR);

    public EnvelopeEditor() {
        getStyleClass().add("envelope-editor");
        setPrefSize(300, 100);
    }

    // Properties
    public DoubleProperty attackProperty() { return attack; }
    public double getAttack() { return attack.get(); }
    public void setAttack(double value) { this.attack.set(value); }

    public DoubleProperty decayProperty() { return decay; }
    public double getDecay() { return decay.get(); }
    public void setDecay(double value) { this.decay.set(value); }

    public DoubleProperty sustainProperty() { return sustain; }
    public double getSustain() { return sustain.get(); }
    public void setSustain(double value) { this.sustain.set(value); }

    public DoubleProperty releaseProperty() { return release; }
    public double getRelease() { return release.get(); }
    public void setRelease(double value) { this.release.set(value); }

    public ObjectProperty<Mode> modeProperty() { return mode; }
    public Mode getMode() { return mode.get(); }
    public void setMode(Mode mode) { this.mode.set(mode); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EnvelopeEditorSkin(this);
    }
}

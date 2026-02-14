package net.mikolas.lyra.ui.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Custom dual-thumb horizontal slider for range selection.
 * Used in Multi Editor for Velocity and Key ranges.
 */
public class RangeSlider extends Control {

    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", 0);
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", 127);
    private final DoubleProperty lowValue = new SimpleDoubleProperty(this, "lowValue", 0);
    private final DoubleProperty highValue = new SimpleDoubleProperty(this, "highValue", 127);

    public RangeSlider() {
        getStyleClass().add("range-slider");
        
        // Ensure lowValue <= highValue
        lowValue.addListener((obs, old, newVal) -> {
            if (newVal.doubleValue() > getHighValue()) {
                setHighValue(newVal.doubleValue());
            }
        });
        
        highValue.addListener((obs, old, newVal) -> {
            if (newVal.doubleValue() < getLowValue()) {
                setLowValue(newVal.doubleValue());
            }
        });
    }

    public RangeSlider(double min, double max, double low, double high) {
        this();
        setMin(min);
        setMax(max);
        setLowValue(low);
        setHighValue(high);
    }

    // Properties
    public DoubleProperty minProperty() { return min; }
    public double getMin() { return min.get(); }
    public void setMin(double value) { min.set(value); }

    public DoubleProperty maxProperty() { return max; }
    public double getMax() { return max.get(); }
    public void setMax(double value) { max.set(value); }

    public DoubleProperty lowValueProperty() { return lowValue; }
    public double getLowValue() { return lowValue.get(); }
    public void setLowValue(double value) { lowValue.set(value); }

    public DoubleProperty highValueProperty() { return highValue; }
    public double getHighValue() { return highValue.get(); }
    public void setHighValue(double value) { highValue.set(value); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RangeSliderSkin(this);
    }
}

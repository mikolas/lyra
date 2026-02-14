package net.mikolas.lyra.ui.control;

import javafx.beans.property.*;
import javafx.css.*;
import javafx.css.converter.FontConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom Rotary Knob control based on Bigglesworth/Lyra specifications.
 * 
 * Interaction:
 * - Vertical Drag: Change value
 * - Shift + Drag: Fine adjustment
 * - Scroll: Step increment/decrement
 * - Double Click: (TODO) Direct entry
 */
public class Dial extends Control {

    private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0);
    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", 0);
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", 127);
    private final DoubleProperty step = new SimpleDoubleProperty(this, "step", 1);
    private final StringProperty label = new SimpleStringProperty(this, "label", "");
    private final DoubleProperty defaultValue = new SimpleDoubleProperty(this, "defaultValue", 0);

    // CSS Styleable Font Property
    private ObjectProperty<Font> font;

    public Dial() {
        getStyleClass().add("dial");
    }

    public Dial(String label, double min, double max, double value) {
        this();
        setLabel(label);
        setMin(min);
        setMax(max);
        setValue(value);
        setDefaultValue(value);
    }

    // Properties
    public DoubleProperty valueProperty() { return value; }
    public double getValue() { return value.get(); }
    public void setValue(double value) { this.value.set(value); }

    public DoubleProperty minProperty() { return min; }
    public double getMin() { return min.get(); }
    public void setMin(double min) { this.min.set(min); }

    public DoubleProperty maxProperty() { return max; }
    public double getMax() { return max.get(); }
    public void setMax(double max) { this.max.set(max); }

    public DoubleProperty stepProperty() { return step; }
    public double getStep() { return step.get(); }
    public void setStep(double step) { this.step.set(step); }

    public StringProperty labelProperty() { return label; }
    public String getLabel() { return label.get(); }
    public void setLabel(String label) { this.label.set(label); }

    public DoubleProperty defaultValueProperty() { return defaultValue; }
    public double getDefaultValue() { return defaultValue.get(); }
    public void setDefaultValue(double value) { this.defaultValue.set(value); }

    // --- Font Support ---
    public final ObjectProperty<Font> fontProperty() {
        if (font == null) {
            font = new StyleableObjectProperty<Font>(Font.getDefault()) {
                @Override 
                public CssMetaData<Dial,Font> getCssMetaData() {
                    return FONT;
                }
                @Override 
                public Object getBean() {
                    return Dial.this;
                }
                @Override 
                public String getName() {
                    return "font";
                }
            };
        }
        return font;
    }

    public final void setFont(Font value) { fontProperty().set(value); }
    public final Font getFont() { return font == null ? Font.getDefault() : font.get(); }

    // --- CSS MetaData ---
    private static final CssMetaData<Dial, Font> FONT =
        new CssMetaData<Dial, Font>("-fx-font", FontConverter.getInstance(), Font.getDefault()) {
            @Override
            public boolean isSettable(Dial node) {
                return node.font == null || !node.font.isBound();
            }
            @Override
            public StyleableProperty<Font> getStyleableProperty(Dial node) {
                return (StyleableProperty<Font>) node.fontProperty();
            }
        };

    private static final List<CssMetaData<? extends Styleable, ?>> CLASS_CSS_META_DATA;
    static {
        final List<CssMetaData<? extends Styleable, ?>> styleables =
            new ArrayList<>(Control.getClassCssMetaData());
        styleables.add(FONT);
        CLASS_CSS_META_DATA = Collections.unmodifiableList(styleables);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CLASS_CSS_META_DATA;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DialSkin(this);
    }
}

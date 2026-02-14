package net.mikolas.lyra.util;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Optional;

public class FxUtils {

    /**
     * Traverses up the scene graph to find a parent of a specific type.
     */
    public static <T> Optional<T> findParent(javafx.scene.Node node, Class<T> type) {
        javafx.scene.Parent parent = node.getParent();
        while (parent != null) {
            if (type.isInstance(parent)) {
                return Optional.of(type.cast(parent));
            }
            parent = parent.getParent();
        }
        return Optional.empty();
    }

    /**
     * Configures a Slider's min, max, and initial value.
     * @param slider The slider to configure.
     * @param min The minimum value.
     * @param max The maximum value.
     * @param initial The initial value.
     */
    public static void setSliderRange(Slider slider, double min, double max, double initial) {
        slider.setMin(min);
        slider.setMax(max);
        slider.setValue(initial);
    }

    /**
     * Applies a formatter to a TextField to restrict input.
     * @param textField The TextField to format.
     * @param format The format to apply (e.g., NumberFormat).
     */
    public static void setTextFieldFormatter(TextField textField, Format format) {
        textField.setTextFormatter(new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }
            ParsePosition parsePosition = new ParsePosition(0);
            Object object = format.parseObject(c.getControlNewText(), parsePosition);
            if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                return null;
            } else {
                return c;
            }
        }));
    }
}

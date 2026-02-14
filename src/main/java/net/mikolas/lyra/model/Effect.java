package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Effect component wrapping a slice of the 385-byte parameter array.
 *
 * <p>Each effect (FX1, FX2) has parameters for type, mix, and several type-specific parameters.
 */
public class Effect {
  private final Sound parent;
  private final byte[] parameters;
  private final int effectNumber;
  private final int baseOffset;

  // Lazy-initialized properties
  private IntegerProperty type;
  private IntegerProperty mix;
  private final Map<Integer, IntegerProperty> properties = new HashMap<>();

  public Effect(Sound parent, byte[] parameters, int effectNumber, int baseOffset) {
    this.parent = parent;
    this.parameters = parameters;
    this.effectNumber = effectNumber;
    this.baseOffset = baseOffset;
  }

  public IntegerProperty typeProperty() {
    if (type == null) {
      int id = baseOffset;
      type = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      type.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return type;
  }

  public IntegerProperty mixProperty() {
    if (mix == null) {
      int id = baseOffset + 1;
      mix = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      mix.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return mix;
  }

  /**
   * Generic parameter property accessor for FX-specific parameters.
   * @param localId local parameter ID (1-based, where 1 is the first FX param after type/mix)
   * @return IntegerProperty bound to the parameter
   */
  public IntegerProperty parameterProperty(int localId) {
    return properties.computeIfAbsent(localId, id -> {
      int absoluteId = baseOffset + 1 + id; // local 1 maps to baseOffset + 2
      IntegerProperty prop = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(absoluteId)] & 0xFF);
      prop.addListener((_, _, newVal) -> {
        int val = newVal.intValue();
        parameters[parent.getMemoryIndex(absoluteId)] = (byte) val;
        parent.notifyParameterChanged(absoluteId, val);
      });
      return prop;
    });
  }

  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - baseOffset;
    if (localParam == 0) {
      if (type != null) type.set(value);
    } else if (localParam == 1) {
      if (mix != null) mix.set(value);
    } else if (localParam >= 2) {
      int id = localParam - 1;
      IntegerProperty prop = properties.get(id);
      if (prop != null) prop.set(value);
    }
  }

  public int getEffectNumber() {
    return effectNumber;
  }
}

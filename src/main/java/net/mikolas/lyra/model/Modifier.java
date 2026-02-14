package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Modifier component for sound modulation.
 *
 * <p>Each modifier has sourceA, sourceB, operation, and constant parameters. There are 4 modifiers
 * total (params 247-260).
 */
public class Modifier {
  private final Sound parent;
  private final byte[] parameters;
  private final int baseOffset;

  private IntegerProperty sourceA;
  private IntegerProperty sourceB;
  private IntegerProperty operation;
  private IntegerProperty constant;

  public Modifier(Sound parent, byte[] parameters, int modifierIndex) {
    this.parent = parent;
    this.parameters = parameters;
    this.baseOffset = 247 + (modifierIndex * 4); // Modifier 1: 247, Modifier 2: 251, etc.
  }

  public IntegerProperty sourceAProperty() {
    if (sourceA == null) {
      int id = baseOffset;
      sourceA = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      sourceA.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return sourceA;
  }

  public IntegerProperty sourceBProperty() {
    if (sourceB == null) {
      int id = baseOffset + 1;
      sourceB = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      sourceB.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return sourceB;
  }

  public IntegerProperty operationProperty() {
    if (operation == null) {
      int id = baseOffset + 2;
      operation = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      operation.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return operation;
  }

  public IntegerProperty constantProperty() {
    if (constant == null) {
      int id = baseOffset + 3;
      constant = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      constant.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return constant;
  }

  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - baseOffset;
    switch (localParam) {
      case 0 -> {
        if (sourceA != null) sourceA.set(value);
      }
      case 1 -> {
        if (sourceB != null) sourceB.set(value);
      }
      case 2 -> {
        if (operation != null) operation.set(value);
      }
      case 3 -> {
        if (constant != null) constant.set(value);
      }
    }
  }
}
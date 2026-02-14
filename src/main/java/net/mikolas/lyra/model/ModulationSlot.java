package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Modulation Matrix slot wrapping 3 parameters (source, destination, amount).
 *
 * <p>The Blofeld has 16 modulation slots starting at parameter 261.
 */
public class ModulationSlot {
  private final Sound parent;
  private final byte[] parameters;
  private final int slotNumber;
  private final int baseOffset;

  // Lazy-initialized properties
  private IntegerProperty source;
  private IntegerProperty destination;
  private IntegerProperty amount;

  public ModulationSlot(Sound parent, byte[] parameters, int slotNumber, int baseOffset) {
    this.parent = parent;
    this.parameters = parameters;
    this.slotNumber = slotNumber;
    this.baseOffset = baseOffset;
  }

  public IntegerProperty sourceProperty() {
    if (source == null) {
      int id = baseOffset;
      source = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      source.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return source;
  }

  public IntegerProperty destinationProperty() {
    if (destination == null) {
      int id = baseOffset + 1;
      destination = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      destination.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return destination;
  }

  public IntegerProperty amountProperty() {
    if (amount == null) {
      int id = baseOffset + 2;
      amount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      amount.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return amount;
  }

  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - baseOffset;
    switch (localParam) {
      case 0 -> {
        if (source != null) source.set(value);
      }
      case 1 -> {
        if (destination != null) destination.set(value);
      }
      case 2 -> {
        if (amount != null) amount.set(value);
      }
    }
  }

  public int getSlotNumber() {
    return slotNumber;
  }
}
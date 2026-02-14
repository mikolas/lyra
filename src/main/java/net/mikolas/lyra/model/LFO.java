package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * LFO component wrapping a slice of the 385-byte parameter array.
 */
public class LFO {
  private final Sound parent;
  private final byte[] parameters;
  private final int lfoNumber;
  private final int baseOffset;

  // Lazy-initialized properties
  private IntegerProperty shape;
  private IntegerProperty speed;
  private IntegerProperty sync;
  private IntegerProperty clocked;
  private IntegerProperty startPhase;
  private IntegerProperty delay;
  private IntegerProperty fade;
  private IntegerProperty keytrack;

  public LFO(Sound parent, byte[] parameters, int lfoNumber, int baseOffset) {
    this.parent = parent;
    this.parameters = parameters;
    this.lfoNumber = lfoNumber;
    this.baseOffset = baseOffset;
  }

  public IntegerProperty shapeProperty() {
    if (shape == null) {
      shape = new SimpleIntegerProperty(parameters[baseOffset] & 0xFF);
      shape.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[baseOffset] = (byte) val;
          parent.notifyParameterChanged(baseOffset, val);
      });
    }
    return shape;
  }

  public IntegerProperty speedProperty() {
    if (speed == null) {
      speed = new SimpleIntegerProperty(parameters[baseOffset + 1] & 0xFF);
      speed.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[baseOffset + 1] = (byte) val;
          parent.notifyParameterChanged(baseOffset + 1, val);
      });
    }
    return speed;
  }

  public IntegerProperty syncProperty() {
    if (sync == null) {
      sync = new SimpleIntegerProperty(parameters[baseOffset + 3] & 0xFF);
      sync.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[baseOffset + 3] = (byte) val;
          parent.notifyParameterChanged(baseOffset + 3, val);
      });
    }
    return sync;
  }

  public IntegerProperty clockedProperty() {
    if (clocked == null) {
      clocked = new SimpleIntegerProperty(parameters[baseOffset + 4] & 0xFF);
      clocked.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[baseOffset + 4] = (byte) val;
          parent.notifyParameterChanged(baseOffset + 4, val);
      });
    }
    return clocked;
  }

  public IntegerProperty startPhaseProperty() {
    if (startPhase == null) {
      startPhase = new SimpleIntegerProperty(parameters[baseOffset + 5] & 0xFF);
      startPhase.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[baseOffset + 5] = (byte) val;
              parent.notifyParameterChanged(baseOffset + 5, val);
          });
    }
    return startPhase;
  }

  public IntegerProperty delayProperty() {
    if (delay == null) {
      delay = new SimpleIntegerProperty(parameters[baseOffset + 6] & 0xFF);
      delay.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[baseOffset + 6] = (byte) val;
          parent.notifyParameterChanged(baseOffset + 6, val);
      });
    }
    return delay;
  }

  public IntegerProperty fadeProperty() {
    if (fade == null) {
      fade = new SimpleIntegerProperty(parameters[baseOffset + 7] & 0xFF);
      fade.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[baseOffset + 7] = (byte) val;
          parent.notifyParameterChanged(baseOffset + 7, val);
      });
    }
    return fade;
  }

  public IntegerProperty keytrackProperty() {
    if (keytrack == null) {
      keytrack = new SimpleIntegerProperty(parameters[baseOffset + 10] & 0xFF);
      keytrack.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[baseOffset + 10] = (byte) val;
              parent.notifyParameterChanged(baseOffset + 10, val);
          });
    }
    return keytrack;
  }

  public int getShape() { return shapeProperty().get(); }
  public void setShape(int value) { shapeProperty().set(value); }

  public void updateFromParameter(int paramId, int value) {
    parameters[paramId] = (byte) value;
    int localParam = paramId - baseOffset;
    switch (localParam) {
      case 0 -> { if (shape != null) shape.set(value); }
      case 1 -> { if (speed != null) speed.set(value); }
      case 3 -> { if (sync != null) sync.set(value); }
      case 4 -> { if (clocked != null) clocked.set(value); }
      case 5 -> { if (startPhase != null) startPhase.set(value); }
      case 6 -> { if (delay != null) delay.set(value); }
      case 7 -> { if (fade != null) fade.set(value); }
      case 10 -> { if (keytrack != null) keytrack.set(value); }
    }
  }

  public int getLfoNumber() {
    return lfoNumber;
  }
}
package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Envelope component wrapping a slice of the 385-byte parameter array.
 */
public class Envelope {
  private final Sound parent;
  private final byte[] parameters;
  private final int envNumber;
  private final int baseOffset;

  // Lazy-initialized properties
  private IntegerProperty mode;
  private IntegerProperty attack;
  private IntegerProperty attackLevel;
  private IntegerProperty decay;
  private IntegerProperty sustain;
  private IntegerProperty decay2;
  private IntegerProperty sustain2;
  private IntegerProperty release;

  public Envelope(Sound parent, byte[] parameters, int envNumber, int baseOffset) {
    this.parent = parent;
    this.parameters = parameters;
    this.envNumber = envNumber;
    this.baseOffset = baseOffset;
  }

  public IntegerProperty modeProperty() {
    if (mode == null) {
      int id = baseOffset;
      mode = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      mode.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return mode;
  }

  public IntegerProperty attackProperty() {
    if (attack == null) {
      int id = baseOffset + 3;
      attack = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      attack.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return attack;
  }

  public IntegerProperty attackLevelProperty() {
    if (attackLevel == null) {
      int id = baseOffset + 4;
      attackLevel = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      attackLevel.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return attackLevel;
  }

  public IntegerProperty decayProperty() {
    if (decay == null) {
      int id = baseOffset + 5;
      decay = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      decay.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return decay;
  }

  public IntegerProperty sustainProperty() {
    if (sustain == null) {
      int id = baseOffset + 6;
      sustain = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      sustain.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return sustain;
  }

  public IntegerProperty decay2Property() {
    if (decay2 == null) {
      int id = baseOffset + 7;
      decay2 = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      decay2.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return decay2;
  }

  public IntegerProperty sustain2Property() {
    if (sustain2 == null) {
      int id = baseOffset + 8;
      sustain2 = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      sustain2.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return sustain2;
  }

  public IntegerProperty releaseProperty() {
    if (release == null) {
      int id = baseOffset + 9;
      release = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      release.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return release;
  }

  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - baseOffset;
    switch (localParam) {
      case 0 -> { if (mode != null) mode.set(value); }
      case 3 -> { if (attack != null) attack.set(value); }
      case 4 -> { if (attackLevel != null) attackLevel.set(value); }
      case 5 -> { if (decay != null) decay.set(value); }
      case 6 -> { if (sustain != null) sustain.set(value); }
      case 7 -> { if (decay2 != null) decay2.set(value); }
      case 8 -> { if (sustain2 != null) sustain2.set(value); }
      case 9 -> { if (release != null) release.set(value); }
    }
  }

  public int getEnvNumber() {
    return envNumber;
  }
}
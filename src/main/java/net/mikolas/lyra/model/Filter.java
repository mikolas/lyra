package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Filter component wrapping a slice of the 385-byte parameter array.
 */
public class Filter {
  private final Sound parent;
  private final byte[] parameters;
  private final int filterNumber;
  private final int baseOffset;

  // Lazy-initialized properties
  private IntegerProperty type;
  private IntegerProperty cutoff;
  private IntegerProperty resonance;
  private IntegerProperty drive;
  private IntegerProperty driveCurve;
  private IntegerProperty keytrack;
  private IntegerProperty envAmount;
  private IntegerProperty envVelocity;
  private IntegerProperty modSource;
  private IntegerProperty modAmount;
  private IntegerProperty fmSource;
  private IntegerProperty fmAmount;
  private IntegerProperty pan;
  private IntegerProperty panSource;
  private IntegerProperty panAmount;

  public Filter(Sound parent, byte[] parameters, int filterNumber, int baseOffset) {
    this.parent = parent;
    this.parameters = parameters;
    this.filterNumber = filterNumber;
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

  public IntegerProperty cutoffProperty() {
    if (cutoff == null) {
      int id = baseOffset + 1;
      cutoff = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      cutoff.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return cutoff;
  }

  public IntegerProperty resonanceProperty() {
    if (resonance == null) {
      int id = baseOffset + 3;
      resonance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      resonance.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return resonance;
  }

  public IntegerProperty driveProperty() {
    if (drive == null) {
      int id = baseOffset + 4;
      drive = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      drive.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return drive;
  }

  public IntegerProperty driveCurveProperty() {
    if (driveCurve == null) {
      int id = baseOffset + 5;
      driveCurve = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      driveCurve.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return driveCurve;
  }

  public IntegerProperty keytrackProperty() {
    if (keytrack == null) {
      int id = baseOffset + 9;
      keytrack = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      keytrack.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return keytrack;
  }

  public IntegerProperty envAmountProperty() {
    if (envAmount == null) {
      int id = baseOffset + 10;
      envAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      envAmount.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return envAmount;
  }

  public IntegerProperty envVelocityProperty() {
    if (envVelocity == null) {
      int id = baseOffset + 11;
      envVelocity = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      envVelocity.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return envVelocity;
  }

  public IntegerProperty modSourceProperty() {
    if (modSource == null) {
      int id = baseOffset + 12;
      modSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      modSource.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return modSource;
  }

  public IntegerProperty modAmountProperty() {
    if (modAmount == null) {
      int id = baseOffset + 13;
      modAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      modAmount.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return modAmount;
  }

  public IntegerProperty fmSourceProperty() {
    if (fmSource == null) {
      int id = baseOffset + 14;
      fmSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      fmSource.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return fmSource;
  }

  public IntegerProperty fmAmountProperty() {
    if (fmAmount == null) {
      int id = baseOffset + 15;
      fmAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      fmAmount.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return fmAmount;
  }

  public IntegerProperty panProperty() {
    if (pan == null) {
      int id = baseOffset + 16;
      pan = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pan.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pan;
  }

  public IntegerProperty panSourceProperty() {
    if (panSource == null) {
      int id = baseOffset + 17;
      panSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      panSource.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return panSource;
  }

  public IntegerProperty panAmountProperty() {
    if (panAmount == null) {
      int id = baseOffset + 18;
      panAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      panAmount.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return panAmount;
  }

  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - baseOffset;
    switch (localParam) {
      case 0 -> { if (type != null) type.set(value); }
      case 1 -> { if (cutoff != null) cutoff.set(value); }
      case 3 -> { if (resonance != null) resonance.set(value); }
      case 4 -> { if (drive != null) drive.set(value); }
      case 5 -> { if (driveCurve != null) driveCurve.set(value); }
      case 9 -> { if (keytrack != null) keytrack.set(value); }
      case 10 -> { if (envAmount != null) envAmount.set(value); }
      case 11 -> { if (envVelocity != null) envVelocity.set(value); }
      case 12 -> { if (modSource != null) modSource.set(value); }
      case 13 -> { if (modAmount != null) modAmount.set(value); }
      case 14 -> { if (fmSource != null) fmSource.set(value); }
      case 15 -> { if (fmAmount != null) fmAmount.set(value); }
      case 16 -> { if (pan != null) pan.set(value); }
      case 17 -> { if (panSource != null) panSource.set(value); }
      case 18 -> { if (panAmount != null) panAmount.set(value); }
    }
  }

  public int getFilterNumber() {
    return filterNumber;
  }
}
package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Common parameters component for miscellaneous sound parameters.
 *
 * <p>Includes glide, allocation, unison, amplifier, filter routing, and pitch modulation
 * parameters scattered throughout the parameter array.
 */
public class CommonParameters {
  private final Sound parent;
  private final byte[] parameters;

  // Lazy-initialized properties
  private IntegerProperty osc2Sync; // 49
  private IntegerProperty pitchModSource; // 50
  private IntegerProperty pitchModAmount; // 51
  private IntegerProperty glideActive; // 53
  private IntegerProperty glideMode; // 56
  private IntegerProperty glideRate; // 57
  private IntegerProperty allocationMode; // 58
  private IntegerProperty unisonDetune; // 59
  private IntegerProperty filterRouting; // 117
  private IntegerProperty ampVolume; // 121
  private IntegerProperty ampVelocity; // 122
  private IntegerProperty ampModSource; // 123
  private IntegerProperty ampModAmount; // 124

  public CommonParameters(Sound parent, byte[] parameters) {
    this.parent = parent;
    this.parameters = parameters;
  }

  public IntegerProperty osc2SyncProperty() {
    if (osc2Sync == null) {
      int id = 49;
      osc2Sync = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc2Sync.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc2Sync;
  }

  public IntegerProperty pitchModSourceProperty() {
    if (pitchModSource == null) {
      int id = 50;
      pitchModSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pitchModSource.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pitchModSource;
  }

  public IntegerProperty pitchModAmountProperty() {
    if (pitchModAmount == null) {
      int id = 51;
      pitchModAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pitchModAmount.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pitchModAmount;
  }

  public IntegerProperty glideActiveProperty() {
    if (glideActive == null) {
      int id = 53;
      glideActive = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      glideActive.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return glideActive;
  }

  public IntegerProperty glideModeProperty() {
    if (glideMode == null) {
      int id = 56;
      glideMode = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      glideMode.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return glideMode;
  }

  public IntegerProperty glideRateProperty() {
    if (glideRate == null) {
      int id = 57;
      glideRate = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      glideRate.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return glideRate;
  }

  public IntegerProperty allocationModeProperty() {
    if (allocationMode == null) {
      int id = 58;
      allocationMode = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      allocationMode.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return allocationMode;
  }

  public IntegerProperty unisonDetuneProperty() {
    if (unisonDetune == null) {
      int id = 59;
      unisonDetune = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      unisonDetune.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return unisonDetune;
  }

  public IntegerProperty filterRoutingProperty() {
    if (filterRouting == null) {
      int id = 117;
      filterRouting = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      filterRouting.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return filterRouting;
  }

  public IntegerProperty ampVolumeProperty() {
    if (ampVolume == null) {
      int id = 121;
      ampVolume = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      ampVolume.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return ampVolume;
  }

  public IntegerProperty ampVelocityProperty() {
    if (ampVelocity == null) {
      int id = 122;
      ampVelocity = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      ampVelocity.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return ampVelocity;
  }

  public IntegerProperty ampModSourceProperty() {
    if (ampModSource == null) {
      int id = 123;
      ampModSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      ampModSource.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return ampModSource;
  }

  public IntegerProperty ampModAmountProperty() {
    if (ampModAmount == null) {
      int id = 124;
      ampModAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      ampModAmount.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return ampModAmount;
  }

  public void updateFromParameter(int paramId, int value) {
    switch (paramId) {
      case 49 -> {
        if (osc2Sync != null) osc2Sync.set(value);
      }
      case 50 -> {
        if (pitchModSource != null) pitchModSource.set(value);
      }
      case 51 -> {
        if (pitchModAmount != null) pitchModAmount.set(value);
      }
      case 53 -> {
        if (glideActive != null) glideActive.set(value);
      }
      case 56 -> {
        if (glideMode != null) glideMode.set(value);
      }
      case 57 -> {
        if (glideRate != null) glideRate.set(value);
      }
      case 58 -> {
        if (allocationMode != null) allocationMode.set(value);
      }
      case 59 -> {
        if (unisonDetune != null) unisonDetune.set(value);
      }
      case 117 -> {
        if (filterRouting != null) filterRouting.set(value);
      }
      case 121 -> {
        if (ampVolume != null) ampVolume.set(value);
      }
      case 122 -> {
        if (ampVelocity != null) ampVelocity.set(value);
      }
      case 123 -> {
        if (ampModSource != null) ampModSource.set(value);
      }
      case 124 -> {
        if (ampModAmount != null) ampModAmount.set(value);
      }
    }
  }
}

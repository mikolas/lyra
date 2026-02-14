package net.mikolas.lyra.model;

import lombok.Getter;

/**
 * Enum representing types of parameter values for UI mapping.
 * Matches keys in parameter-values.json.
 */
@Getter
public enum ParameterValueType {
  BANKS("banks"),
  MIDI_CHANNELS("midiChannels"),
  OSC_SHAPES("oscShapes"),
  FILTERS("filters"),
  LFO_SHAPES("lfoShapes"),
  MOD_SOURCE("modSource"),
  MOD_DESTINATION("modDest"),
  MODIFIER_OPERATIONS("modOperator"),
  DRIVE_CURVE("driveCurves"),
  ARP_MODE("arpMode"),
  ARP_PATTERN("arpPattern"),
  ARP_DIRECTION("arpDirection"),
  UNISON_MODE("unisonMode"),
  GLIDE_MODE("glideMode"),
  FILTER_ROUTING("filterRouting"),
  ENV_MODE("envMode"),
  ARP_STEPS("arpSteps"),
  ARP_ACCENTS("arpAccents"),
  ARP_LENGTHS("arpLengths"),
  ARP_TIMINGS("arpTimings"),
  CATEGORIES("categories"),
  EFFECT_TYPE("effectType"),
  ON_OFF("onOff"),
  OFF_ON("offOn");

  private final String jsonKey;

  ParameterValueType(String jsonKey) {
    this.jsonKey = jsonKey;
  }
}

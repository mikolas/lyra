package net.mikolas.lyra.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameter metadata for all 385 Blofeld parameters.
 *
 * <p>Provides complete parameter definitions including ranges, defaults, display names, and
 * value-to-string mappings.
 */
public class ParameterMetadata {
  private static final Map<Integer, ParamDef> metadata = new HashMap<>();
  private static boolean loaded = false;

  /** Parameter definition record. */
  public record ParamDef(
      int id,
      String attrName,
      int min,
      int max,
      int step,
      int defaultValue,
      String fullName,
      String shortName,
      String family,
      String values) {}

  /**
   * Get parameter definition by ID.
   *
   * @param paramId parameter ID (0-384)
   * @return parameter definition, or null if not found
   */
  public static ParamDef get(int paramId) {
    ensureLoaded();
    return metadata.get(paramId);
  }

  /**
   * Get parameter display name.
   *
   * @param paramId parameter ID
   * @return full display name, or "Parameter {id}" if not found
   */
  public static String getDisplayName(int paramId) {
    ParamDef def = get(paramId);
    return def != null ? def.fullName : "Parameter " + paramId;
  }

  /**
   * Get parameter short name.
   *
   * @param paramId parameter ID
   * @return short name, or "P{id}" if not found
   */
  public static String getShortName(int paramId) {
    ParamDef def = get(paramId);
    return def != null ? def.shortName : "P" + paramId;
  }

  /**
   * Get parameter value as display string.
   *
   * @param paramId parameter ID
   * @param value numeric value (0-127)
   * @return display string using value mappings
   */
  public static String getValueString(int paramId, int value) {
    ParamDef def = get(paramId);
    if (def == null) return String.valueOf(value);

    // For stepped parameters, convert value to index
    int index = value;
    if (def.step > 1) {
      index = (value - def.min) / def.step;
    }

    // Try to get value mapping
    String displayValue = ParameterValues.getDisplayValue(def.values, index);
    return displayValue;
  }

  /**
   * Check if parameter has value-to-string mapping.
   *
   * @param paramId parameter ID
   * @return true if parameter has value mapping
   */
  public static boolean hasValueMapping(int paramId) {
    ParamDef def = get(paramId);
    return def != null && ParameterValues.getValues(def.values) != null;
  }

  /**
   * Get all parameter definitions.
   *
   * @return map of parameter ID to definition
   */
  public static Map<Integer, ParamDef> getAll() {
    ensureLoaded();
    return Map.copyOf(metadata);
  }

  @SuppressWarnings("unchecked")
  private static synchronized void ensureLoaded() {
    if (loaded) return;

    try (InputStream is =
        ParameterMetadata.class.getResourceAsStream("/parameter_metadata.json")) {
      if (is == null) {
        throw new IOException("parameter_metadata.json not found");
      }

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> data = mapper.readValue(is, Map.class);
      List<Map<String, Object>> params = (List<Map<String, Object>>) data.get("parameters");

      for (Map<String, Object> p : params) {
        ParamDef def =
            new ParamDef(
                (Integer) p.get("id"),
                (String) p.get("attrName"),
                (Integer) p.get("min"),
                (Integer) p.get("max"),
                (Integer) p.get("step"),
                (Integer) p.get("default"),
                (String) p.get("fullName"),
                (String) p.get("shortName"),
                (String) p.get("family"),
                (String) p.get("values"));
        metadata.put(def.id, def);
      }

      loaded = true;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load parameter metadata", e);
    }
  }
}

package net.mikolas.lyra.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameter value mappings for UI display.
 *
 * <p>Provides human-readable strings for parameter values (e.g., value 0 = "Sine", value 1 =
 * "Triangle" for LFO shape).
 *
 * <p>Loaded from parameter-values.json resource file.
 */
public class ParameterValues {
  private static final Map<String, List<String>> mappings = new HashMap<>();
  private static boolean loaded = false;

  /**
   * Get value mapping for a parameter type (type-safe).
   *
   * @param type parameter value type
   * @return list of value strings, or null if not found
   */
  public static List<String> getValues(ParameterValueType type) {
    return getValues(type.getJsonKey());
  }

  /**
   * Get value mapping for a parameter type.
   *
   * @param type parameter type (e.g., "octave", "lfoShapes", "filters")
   * @return list of value strings, or null if not found
   */
  public static List<String> getValues(String type) {
    ensureLoaded();
    return mappings.get(type);
  }

  /**
   * Get display string for a parameter value (type-safe).
   *
   * @param type parameter value type
   * @param value numeric value (0-127)
   * @return display string, or String.valueOf(value) if not found
   */
  public static String getDisplayValue(ParameterValueType type, int value) {
    return getDisplayValue(type.getJsonKey(), value);
  }

  /**
   * Get display string for a parameter value.
   *
   * @param type parameter type
   * @param value numeric value (0-127)
   * @return display string, or String.valueOf(value) if not found
   */
  public static String getDisplayValue(String type, int value) {
    List<String> values = getValues(type);
    if (values != null && value >= 0 && value < values.size()) {
      return values.get(value);
    }
    return String.valueOf(value);
  }

  @SuppressWarnings("unchecked")
  private static synchronized void ensureLoaded() {
    if (loaded) return;

    try (InputStream is =
        ParameterValues.class.getResourceAsStream("/parameter-values.json")) {
      if (is == null) {
        throw new IOException("parameter-values.json not found");
      }
      ObjectMapper mapper = new ObjectMapper();
      Map<String, List<String>> data = mapper.readValue(is, Map.class);
      mappings.putAll(data);
      loaded = true;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load parameter values", e);
    }
  }
}

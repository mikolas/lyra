package net.mikolas.lyra.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParameterMetadataTest {

  @Test
  void testLoadMetadata() {
    var all = ParameterMetadata.getAll();
    assertNotNull(all);
    assertTrue(all.size() > 300, "Should have loaded 300+ parameters");
  }

  @Test
  void testGetParameter() {
    var osc1Octave = ParameterMetadata.get(1);
    assertNotNull(osc1Octave);
    assertEquals(1, osc1Octave.id());
    assertEquals("osc1Octave", osc1Octave.attrName());
    assertEquals("Osc 1 Octave", osc1Octave.fullName());
    assertEquals("Octave", osc1Octave.shortName());
    assertEquals("Osc 1", osc1Octave.family());
    assertEquals(16, osc1Octave.min());
    assertEquals(112, osc1Octave.max());
    assertEquals(64, osc1Octave.defaultValue());
  }

  @Test
  void testGetDisplayName() {
    assertEquals("Osc 1 Octave", ParameterMetadata.getDisplayName(1));
    assertEquals("Osc 1 Semitone", ParameterMetadata.getDisplayName(2));
    assertEquals("Filter 1 Type", ParameterMetadata.getDisplayName(77));
  }

  @Test
  void testGetShortName() {
    assertEquals("Octave", ParameterMetadata.getShortName(1));
    assertEquals("Semitone", ParameterMetadata.getShortName(2));
  }

  @Test
  void testGetValueString() {
    // Octave parameter (1) with octave mapping
    String octaveValue = ParameterMetadata.getValueString(1, 64);
    assertNotNull(octaveValue);
    assertTrue(octaveValue.contains("'"), "Octave value should contain foot mark");

    // Filter type parameter (77) with filter type mapping
    String filterType = ParameterMetadata.getValueString(77, 1);
    assertEquals("LP 24dB", filterType);
  }

  @Test
  void testHasValueMapping() {
    assertTrue(ParameterMetadata.hasValueMapping(1), "Octave should have mapping");
    assertTrue(ParameterMetadata.hasValueMapping(77), "Filter type should have mapping");
  }

  @Test
  void testMissingParameter() {
    var missing = ParameterMetadata.get(999);
    assertNull(missing);
    assertEquals("Parameter 999", ParameterMetadata.getDisplayName(999));
    assertEquals("P999", ParameterMetadata.getShortName(999));
  }
}

package net.mikolas.lyra.midi;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** TDD RED: Tests for CCMapper - write tests first, then implement. */
class CCMapperTest {
  private CCMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CCMapper();
  }

  @Test
  void shouldMapCCToParameter() {
    // CC 5 = Glide (param 57)
    assertEquals(57, mapper.ccToParameter(5));

    // CC 27 = OSC1 Octave (param 1)
    assertEquals(1, mapper.ccToParameter(27));

    // CC 74 = Filter1 Cutoff (param 88)
    assertEquals(88, mapper.ccToParameter(74));
  }

  @Test
  void shouldReturnNegativeForUnmappedCC() {
    // CC 1 (mod wheel) not mapped
    assertEquals(-1, mapper.ccToParameter(1));

    // CC 119 not mapped
    assertEquals(-1, mapper.ccToParameter(119));
  }

  @Test
  void shouldMapParameterToCC() {
    // Param 57 (Glide) = CC 5
    assertEquals(5, mapper.parameterToCC(57));

    // Param 1 (OSC1 Octave) = CC 27
    assertEquals(27, mapper.parameterToCC(1));

    // Param 88 (Filter1 Cutoff) = CC 74
    assertEquals(74, mapper.parameterToCC(88));
  }

  @Test
  void shouldReturnNegativeForUnmappedParameter() {
    // Param 0 not mapped
    assertEquals(-1, mapper.parameterToCC(0));

    // Param 300 not mapped
    assertEquals(-1, mapper.parameterToCC(300));
  }

  @Test
  void shouldCheckIfCCIsMapped() {
    assertTrue(mapper.isCCMapped(5)); // Glide
    assertTrue(mapper.isCCMapped(74)); // Filter cutoff
    assertFalse(mapper.isCCMapped(1)); // Mod wheel
    assertFalse(mapper.isCCMapped(127)); // Not mapped
  }

  @Test
  void shouldCheckIfParameterIsMapped() {
    assertTrue(mapper.isParameterMapped(57)); // Glide
    assertTrue(mapper.isParameterMapped(88)); // Filter cutoff
    assertFalse(mapper.isParameterMapped(0)); // Not mapped
    assertFalse(mapper.isParameterMapped(300)); // Not mapped
  }
}

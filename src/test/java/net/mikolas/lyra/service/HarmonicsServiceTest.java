package net.mikolas.lyra.service;

import net.mikolas.lyra.model.Harmonic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HarmonicsServiceTest {

  private HarmonicsService service;

  @BeforeEach
  void setUp() {
    service = new HarmonicsService();
  }

  @Test
  void testApplyHarmonicsReplace() {
    int[] samples = new int[128];
    List<Harmonic> harmonics = List.of(
        new Harmonic(1, Harmonic.WaveType.SINE, 1.0)
    );
    
    int[] result = service.applyHarmonics(samples, harmonics, false);
    
    assertNotNull(result);
    assertEquals(128, result.length);
    // First harmonic should create a sine wave
    assertTrue(result[0] != 0 || result[32] != 0);
  }

  @Test
  void testApplyHarmonicsAdditive() {
    int[] samples = new int[128];
    samples[0] = 100000;
    List<Harmonic> harmonics = List.of(
        new Harmonic(1, Harmonic.WaveType.SINE, 0.5)
    );
    
    int[] result = service.applyHarmonics(samples, harmonics, true);
    
    // Should add to existing wave
    assertTrue(Math.abs(result[0]) >= 100000);
  }

  @Test
  void testApplyMultipleHarmonics() {
    int[] samples = new int[128];
    List<Harmonic> harmonics = List.of(
        new Harmonic(1, Harmonic.WaveType.SINE, 0.5),
        new Harmonic(2, Harmonic.WaveType.SINE, 0.3)
    );
    
    int[] result = service.applyHarmonics(samples, harmonics, false);
    
    assertNotNull(result);
    assertEquals(128, result.length);
  }

  @Test
  void testApplyHarmonicsClamp() {
    int[] samples = new int[128];
    List<Harmonic> harmonics = List.of(
        new Harmonic(1, Harmonic.WaveType.SINE, 1.0),
        new Harmonic(2, Harmonic.WaveType.SINE, 1.0),
        new Harmonic(3, Harmonic.WaveType.SINE, 1.0)
    );
    
    int[] result = service.applyHarmonics(samples, harmonics, false);
    
    // Should clamp to valid range
    for (int val : result) {
      assertTrue(val >= -1048576 && val <= 1048575);
    }
  }

  @Test
  void testApplyNoHarmonics() {
    int[] samples = {100, 200, 300};
    List<Harmonic> harmonics = List.of();
    
    int[] result = service.applyHarmonics(samples, harmonics, false);
    
    assertArrayEquals(new int[3], result);
  }

  @Test
  void testApplyNoHarmonicsAdditive() {
    int[] samples = {100, 200, 300};
    List<Harmonic> harmonics = List.of();
    
    int[] result = service.applyHarmonics(samples, harmonics, true);
    
    assertArrayEquals(samples, result);
  }

  @Test
  void testParseWaveType() {
    assertEquals(Harmonic.WaveType.SINE, service.parseWaveType("Sine"));
    assertEquals(Harmonic.WaveType.SQUARE, service.parseWaveType("Square"));
    assertEquals(Harmonic.WaveType.TRIANGLE, service.parseWaveType("Triangle"));
    assertEquals(Harmonic.WaveType.SAWTOOTH, service.parseWaveType("Saw"));
    assertEquals(Harmonic.WaveType.INV_SAWTOOTH, service.parseWaveType("InvSaw"));
    assertEquals(Harmonic.WaveType.SINE, service.parseWaveType("Unknown"));
  }
}

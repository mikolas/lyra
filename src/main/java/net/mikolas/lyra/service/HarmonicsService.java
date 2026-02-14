package net.mikolas.lyra.service;

import net.mikolas.lyra.model.Harmonic;

import java.util.List;

/**
 * Service for additive synthesis using harmonics.
 */
public class HarmonicsService {

  /**
   * Apply harmonics to waveform samples.
   *
   * @param samples input samples
   * @param harmonics list of harmonics to apply
   * @param additive if true, add to existing samples; if false, replace
   * @return result samples with harmonics applied
   */
  public int[] applyHarmonics(int[] samples, List<Harmonic> harmonics, boolean additive) {
    int[] result = new int[samples.length];
    
    // Start with existing samples if additive
    if (additive) {
      System.arraycopy(samples, 0, result, 0, samples.length);
    }
    
    // Add each harmonic
    for (Harmonic harmonic : harmonics) {
      int[] harmonicWave = harmonic.generateWave();
      for (int i = 0; i < samples.length; i++) {
        result[i] = Math.clamp(result[i] + harmonicWave[i], -1048576, 1048575);
      }
    }
    
    return result;
  }

  /**
   * Parse wave type string to enum.
   *
   * @param waveTypeStr wave type string
   * @return WaveType enum
   */
  public Harmonic.WaveType parseWaveType(String waveTypeStr) {
    return switch (waveTypeStr) {
      case "Square" -> Harmonic.WaveType.SQUARE;
      case "Triangle" -> Harmonic.WaveType.TRIANGLE;
      case "Saw" -> Harmonic.WaveType.SAWTOOTH;
      case "InvSaw" -> Harmonic.WaveType.INV_SAWTOOTH;
      default -> Harmonic.WaveType.SINE;
    };
  }
}

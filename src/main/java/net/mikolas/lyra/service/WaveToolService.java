package net.mikolas.lyra.service;

/**
 * Service for wave manipulation tools.
 * Provides normalize, smooth, invert, and reverse operations on waveform samples.
 */
public class WaveToolService {

  /**
   * Normalize samples to maximum amplitude.
   *
   * @param samples input samples
   * @param maxAmplitude target maximum amplitude
   * @return normalized samples
   */
  public int[] normalize(int[] samples, int maxAmplitude) {
    int max = 0;
    for (int s : samples) {
      max = Math.max(max, Math.abs(s));
    }
    
    if (max == 0) {
      return samples.clone();
    }
    
    double factor = (double) maxAmplitude / max;
    int[] result = new int[samples.length];
    for (int i = 0; i < samples.length; i++) {
      result[i] = (int) (samples[i] * factor);
    }
    return result;
  }

  /**
   * Smooth samples using 3-point moving average.
   *
   * @param samples input samples
   * @return smoothed samples
   */
  public int[] smooth(int[] samples) {
    int[] result = new int[samples.length];
    for (int i = 0; i < samples.length; i++) {
      int prev = samples[(i - 1 + samples.length) % samples.length];
      int curr = samples[i];
      int next = samples[(i + 1) % samples.length];
      result[i] = (prev + curr + next) / 3;
    }
    return result;
  }

  /**
   * Invert samples (flip vertically).
   *
   * @param samples input samples
   * @return inverted samples
   */
  public int[] invert(int[] samples) {
    int[] result = new int[samples.length];
    for (int i = 0; i < samples.length; i++) {
      result[i] = -samples[i];
    }
    return result;
  }

  /**
   * Reverse samples (flip horizontally).
   *
   * @param samples input samples
   * @return reversed samples
   */
  public int[] reverse(int[] samples) {
    int[] result = new int[samples.length];
    for (int i = 0; i < samples.length; i++) {
      result[i] = samples[samples.length - 1 - i];
    }
    return result;
  }
}

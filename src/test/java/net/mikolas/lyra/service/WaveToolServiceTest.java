package net.mikolas.lyra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaveToolServiceTest {

  private WaveToolService service;
  private static final int NORM_Y = 1048576;

  @BeforeEach
  void setUp() {
    service = new WaveToolService();
  }

  @Test
  void testNormalize() {
    int[] samples = {500000, -500000, 250000, -250000};
    
    int[] result = service.normalize(samples, NORM_Y);
    
    assertEquals(NORM_Y, result[0]);
    assertEquals(-NORM_Y, result[1]);
    assertEquals(NORM_Y / 2, result[2]);
    assertEquals(-NORM_Y / 2, result[3]);
  }

  @Test
  void testNormalizeZero() {
    int[] samples = {0, 0, 0, 0};
    
    int[] result = service.normalize(samples, NORM_Y);
    
    assertArrayEquals(samples, result);
  }

  @Test
  void testSmooth() {
    int[] samples = {0, 100, 0, 100, 0};
    
    int[] result = service.smooth(samples);
    
    // (prev + curr + next) / 3
    assertEquals(33, result[0]); // (0 + 0 + 100) / 3 = 33
    assertEquals(33, result[1]); // (0 + 100 + 0) / 3 = 33
    assertEquals(66, result[2]); // (100 + 0 + 100) / 3 = 66
    assertEquals(33, result[3]); // (0 + 100 + 0) / 3 = 33
    assertEquals(33, result[4]); // (100 + 0 + 0) / 3 = 33
  }

  @Test
  void testInvert() {
    int[] samples = {100, -200, 0, 500};
    
    int[] result = service.invert(samples);
    
    assertEquals(-100, result[0]);
    assertEquals(200, result[1]);
    assertEquals(0, result[2]);
    assertEquals(-500, result[3]);
  }

  @Test
  void testReverse() {
    int[] samples = {1, 2, 3, 4, 5};
    
    int[] result = service.reverse(samples);
    
    assertArrayEquals(new int[]{5, 4, 3, 2, 1}, result);
  }

  @Test
  void testReverseSymmetric() {
    int[] samples = {1, 2, 3, 2, 1};
    
    int[] result = service.reverse(samples);
    
    assertArrayEquals(samples, result);
  }

  @Test
  void testNormalizePreservesShape() {
    int[] samples = {100, 200, 300, 200, 100};
    
    int[] result = service.normalize(samples, NORM_Y);
    
    // Should preserve relative proportions
    assertTrue(result[0] < result[1]);
    assertTrue(result[1] < result[2]);
    assertTrue(result[2] > result[3]);
    assertTrue(result[3] > result[4]);
  }

  @Test
  void testSmoothReducesPeaks() {
    int[] samples = {0, 1000, 0};
    
    int[] result = service.smooth(samples);
    
    // Middle value should be reduced
    assertTrue(result[1] < samples[1]);
  }
}

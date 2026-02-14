package net.mikolas.lyra.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

/** Tests for Sound model parameter conversion and component integration. */
class SoundTest {

  @Test
  void testInitSoundCreation() {
    Sound sound = SoundTestData.createInitSound();
    assertNotNull(sound);
    assertEquals("Init", sound.getName());
    assertEquals(385, sound.getParameters().length);
  }

  @Test
  void testParameterArrayNotModified() {
    Sound sound = SoundTestData.createInitSound();
    byte[] original = SoundTestData.INIT_SOUND;
    byte[] soundParams = sound.getParameters();

    // Verify arrays are independent
    assertNotSame(original, soundParams);
    assertArrayEquals(original, soundParams);
  }

  @Test
  void testOscillatorReadFromBytes() {
    Sound sound = SoundTestData.createInitSound();
    Oscillator osc1 = sound.getOscillators()[0];

    // Verify Osc1 reads correct values from byte array (using +2 offset)
    // ID 1 (Octave) at Index 3 = 64
    assertEquals(64, osc1.octaveProperty().get()); 
    // ID 2 (Semitone) at Index 4 = 64
    assertEquals(64, osc1.semitoneProperty().get()); 
    // ID 3 (Detune) at Index 5 = 64
    assertEquals(64, osc1.detuneProperty().get()); 
  }

  @Test
  void testOscillatorWriteToBytes() {
    Sound sound = SoundTestData.createInitSound();
    Oscillator osc1 = sound.getOscillators()[0];

    // Modify property
    osc1.octaveProperty().set(100);

    // Verify byte array updated at Index 1 (ID 1)
    assertEquals(100, sound.getParameters()[1] & 0xFF);
  }

  @Test
  void testMixerReadFromBytes() {
    Sound sound = SoundTestData.createInitSound();
    Mixer mixer = sound.getMixer();

    // Verify mixer reads correct values (params 61-72 at indices 61-72)
    // Index 61 (ID 61) = 127
    assertEquals(127, mixer.osc1LevelProperty().get()); 
    // Index 62 (ID 62) = 0
    assertEquals(0, mixer.osc1BalanceProperty().get()); 
    // Index 63 (ID 63) = 127
    assertEquals(127, mixer.osc2LevelProperty().get()); 
  }

  @Test
  void testMixerWriteToBytes() {
    Sound sound = SoundTestData.createInitSound();
    Mixer mixer = sound.getMixer();

    // Modify property
    mixer.osc1LevelProperty().set(80);

    // Verify byte array updated at Index 61 (ID 61)
    assertEquals(80, sound.getParameters()[61] & 0xFF);
  }

  @Test
  void testUpdateParameterRoutesToComponent() {
    Sound sound = SoundTestData.createInitSound();
    Oscillator osc1 = sound.getOscillators()[0];

    // Initialize property by accessing it
    osc1.octaveProperty().get();

    // Update via updateParameter (simulates MIDI input)
    sound.updateParameter(1, 75);

    // Verify both byte array and property updated (ID 1 at Index 1)
    assertEquals(75, sound.getParameters()[1] & 0xFF);
    assertEquals(75, osc1.octaveProperty().get());
  }

  @Test
  void testRoundTripConversion() {
    Sound sound = SoundTestData.createInitSound();

    // Access all components to initialize properties
    sound.getOscillators()[0].octaveProperty().get();
    sound.getMixer().osc1LevelProperty().get();
    sound.getCommon().glideRateProperty().get();

    // Modify via properties
    sound.getOscillators()[0].octaveProperty().set(88);
    sound.getMixer().osc1LevelProperty().set(100);
    sound.getCommon().glideRateProperty().set(64);

    // Verify byte array reflects changes (direct mapping)
    assertEquals(88, sound.getParameters()[1] & 0xFF); // ID 1
    assertEquals(100, sound.getParameters()[61] & 0xFF); // ID 61
    assertEquals(64, sound.getParameters()[57] & 0xFF); // ID 57

    // Modify via updateParameter
    sound.updateParameter(1, 50);
    sound.updateParameter(61, 75);
    sound.updateParameter(57, 32);

    // Verify properties reflect changes
    assertEquals(50, sound.getOscillators()[0].octaveProperty().get());
    assertEquals(75, sound.getMixer().osc1LevelProperty().get());
    assertEquals(32, sound.getCommon().glideRateProperty().get());
  }

  @Test
  void testSilentUpdateBreaksFeedbackLoop() {
    Sound sound = SoundTestData.createInitSound();
    AtomicInteger changeCount = new AtomicInteger(0);
    
    sound.setParameterChangeListener((id, val) -> changeCount.incrementAndGet());
    
    // Normal update triggers listener
    sound.updateParameter(1, 10);
    assertEquals(1, changeCount.get());
    
    // Silent update DOES NOT trigger listener
    sound.updateParameterSilently(1, 20);
    assertEquals(1, changeCount.get(), "Silent update should not trigger listener");
    assertEquals(20, sound.getOscillators()[0].octaveProperty().get());
  }

  @Test
  void testNameSynchronization() {
    Sound sound = Sound.builder().parameters(new byte[385]).build();
    
    // Test short name
    sound.setNameAndSyncParameters("Short");
    assertEquals("Short", sound.getName());
    assertEquals((byte) 'S', sound.getParameters()[363]); // ID 363 at Index 363
    assertEquals((byte) 'h', sound.getParameters()[364]);
    assertEquals((byte) 'o', sound.getParameters()[365]);
    assertEquals((byte) 'r', sound.getParameters()[366]);
    assertEquals((byte) 't', sound.getParameters()[367]);
    assertEquals((byte) ' ', sound.getParameters()[368]); // Padded with space
    
    // Test exact 16 chars
    sound.setNameAndSyncParameters("1234567890123456");
    assertEquals("1234567890123456", sound.getName());
    assertEquals((byte) '6', sound.getParameters()[378]); // ID 378 at Index 378
  }

  @Test
  void testCloneSoundDeepCopy() {
    Sound original = SoundTestData.createInitSound();
    original.setNameAndSyncParameters("Original");
    
    Sound clone = original.cloneSound();
    
    assertEquals(original.getName(), clone.getName());
    assertArrayEquals(original.getParameters(), clone.getParameters());
    assertNotSame(original.getParameters(), clone.getParameters());
    
    // Modify clone and ensure original is not affected
    clone.setNameAndSyncParameters("Modified");
    clone.getParameters()[1] = 99;
    
    assertEquals("Original", original.getName());
    assertNotEquals(99, original.getParameters()[1]);
  }
}
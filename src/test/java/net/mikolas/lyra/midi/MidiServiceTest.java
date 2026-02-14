package net.mikolas.lyra.midi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.sound.midi.MidiDevice;
import net.mikolas.lyra.exception.MidiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** TDD RED: Tests for MidiService - write tests first, then implement. */
class MidiServiceTest {
  private MidiService midiService;
  private BlofeldProtocol protocol;

  @BeforeEach
  void setUp() {
    protocol = new BlofeldProtocol();
    midiService = new MidiService(protocol);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (midiService.isConnected()) {
      midiService.disconnect();
    }
  }

  @Test
  void shouldEnumerateDevices() {
    List<MidiDevice.Info> devices = midiService.listDevices();
    assertNotNull(devices);
  }

  @Test
  void shouldStartDisconnected() {
    assertFalse(midiService.isConnected());
  }

  @Test
  void shouldThrowWhenSendingWhileDisconnected() {
    SoundParameterChange msg = new SoundParameterChange(0, 0, 72, 64);
    assertThrows(MidiException.class, () -> midiService.send(msg));
  }

  @Test
  void shouldThrowWhenConnectingToInvalidDevice() {
    assertThrows(MidiException.class, () -> midiService.connect(null));
  }

  @Test
  void testConnectionState() {
    assertFalse(midiService.isConnected());
    assertFalse(midiService.isInputConnected());
    assertFalse(midiService.isOutputConnected());
    assertEquals("None", midiService.getInputDeviceName());
    assertEquals("None", midiService.getOutputDeviceName());
  }

  @Test
  void testAuditionSoundRequiresConnection() {
    net.mikolas.lyra.model.Sound sound = net.mikolas.lyra.model.SoundTestData.createInitSound();
    assertThrows(MidiException.class, () -> midiService.auditionSound(sound));
  }
}

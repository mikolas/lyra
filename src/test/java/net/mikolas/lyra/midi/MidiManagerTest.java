package net.mikolas.lyra.midi;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for MidiManager singleton. */
class MidiManagerTest {

  @BeforeEach
  void setUp() {
    MidiManager.resetForTesting();
  }

  @Test
  void testSingletonInstance() {
    MidiManager instance1 = MidiManager.getInstance();
    MidiManager instance2 = MidiManager.getInstance();

    assertNotNull(instance1);
    assertSame(instance1, instance2, "Should return same instance");
  }

  @Test
  void testInitializeIdempotent() {
    MidiManager manager = MidiManager.getInstance();

    // Initialize multiple times - should be safe
    manager.initialize();
    manager.initialize();
    manager.initialize();

    // Should not throw exception
    assertNotNull(manager);
  }

  @Test
  void testGetServiceBeforeInitialize() {
    MidiManager manager = MidiManager.getInstance();
    
    // Service may be null if no Blofeld connected
    // This is expected behavior, not an error
    assertDoesNotThrow(() -> manager.getService());
  }

  @Test
  void testShutdown() {
    MidiManager manager = MidiManager.getInstance();
    manager.initialize();

    // Should not throw exception
    assertDoesNotThrow(() -> manager.shutdown());
  }

  @Test
  void testIsConnected() {
    MidiManager manager = MidiManager.getInstance();
    manager.initialize();

    // May be true or false depending on hardware
    // Just verify it doesn't throw
    assertDoesNotThrow(() -> manager.isConnected());
  }
}

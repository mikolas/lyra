package net.mikolas.lyra.midi;

import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiDevice;
import net.mikolas.lyra.exception.MidiException;

/**
 * Singleton manager for MIDI connectivity.
 *
 * <p>Provides global access to MidiService and handles automatic Blofeld detection. Thread-safe
 * singleton ensures all windows share the same MIDI connection and configuration.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * MidiManager.getInstance().initialize();
 * MidiService midi = MidiManager.getInstance().getService();
 * }</pre>
 */
public class MidiManager {
  private static volatile MidiManager instance;
  private MidiService midiService;
  private boolean initialized = false;
  private final List<ConnectionListener> listeners = new ArrayList<>();

  /** Listener for MIDI connection changes. */
  public interface ConnectionListener {
    void onConnectionChanged();
  }

  private MidiManager() {
    // Private constructor for singleton
  }

  /**
   * Get the singleton instance.
   *
   * @return MidiManager instance
   */
  public static MidiManager getInstance() {
    if (instance == null) {
      synchronized (MidiManager.class) {
        if (instance == null) {
          instance = new MidiManager();
        }
      }
    }
    return instance;
  }

  /**
   * Reset the singleton state for testing purposes.
   */
  public static void resetForTesting() {
    synchronized (MidiManager.class) {
      if (instance != null) {
        instance.shutdown();
        instance = null;
      }
    }
  }

  /**
   * Add a connection listener.
   *
   * @param listener listener to add
   */
  public synchronized void addConnectionListener(ConnectionListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Remove a connection listener.
   *
   * @param listener listener to remove
   */
  public synchronized void removeConnectionListener(ConnectionListener listener) {
    listeners.remove(listener);
  }

  private synchronized void notifyConnectionChanged() {
    for (ConnectionListener listener : listeners) {
      listener.onConnectionChanged();
    }
  }

  /**
   * Initialize MIDI service and auto-detect Blofeld.
   *
   * <p>Safe to call multiple times - only initializes once.
   */
  public synchronized void initialize() {
    if (initialized) {
      return;
    }

    try {
      midiService = new MidiService(new BlofeldProtocol());

      // Auto-detect and connect to Blofeld
      List<MidiDevice.Info> devices = midiService.listDevices();
      for (MidiDevice.Info device : devices) {
        String name = device.getName().toLowerCase();
        if (name.contains("blofeld")) {
          System.out.println("Found Blofeld: " + device.getName());
          midiService.connect(device);
          System.out.println("Connected to Blofeld - Performing Identity Handshake...");
          midiService.sendIdentityRequest();
          break;
        }
      }

      if (!midiService.isConnected()) {
        System.out.println("No Blofeld device found - MIDI features disabled");
      }

      initialized = true;
      notifyConnectionChanged();
    } catch (Exception e) {
      System.err.println("Failed to initialize MIDI: " + e.getMessage());
      midiService = null;
    }
  }

  /**
   * Get the MIDI service instance.
   *
   * @return MidiService, or null if not initialized or failed
   */
  public MidiService getService() {
    return midiService;
  }

  /**
   * Check if MIDI is initialized and connected.
   *
   * @return true if connected to Blofeld
   */
  public boolean isConnected() {
    return midiService != null && midiService.isConnected();
  }

  /**
   * Reconnect to a specific device.
   *
   * @param deviceInfo device to connect to
   * @throws MidiException if connection fails
   */
  public synchronized void reconnect(MidiDevice.Info deviceInfo) throws MidiException {
    if (midiService != null) {
      midiService.disconnect();
      midiService.connect(deviceInfo);
      notifyConnectionChanged();
    }
  }

  /**
   * Shutdown MIDI service.
   *
   * <p>Should be called on application exit.
   */
  public synchronized void shutdown() {
    if (midiService != null) {
      try {
        midiService.close();
      } catch (Exception e) {
        System.err.println("Error closing MIDI service: " + e.getMessage());
      }
      midiService = null;
      initialized = false;
    }
    notifyConnectionChanged();
  }
}

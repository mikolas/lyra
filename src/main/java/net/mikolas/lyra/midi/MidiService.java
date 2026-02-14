package net.mikolas.lyra.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.sound.midi.*;
import net.mikolas.lyra.exception.MidiException;
import net.mikolas.lyra.model.MultiPatch;
import net.mikolas.lyra.model.Sound;

/**
 * MIDI service for bidirectional communication with Blofeld synthesizer.
 */
public class MidiService implements AutoCloseable {
  private final BlofeldProtocol protocol;
  private MidiDevice outputDevice;
  private MidiDevice inputDevice;
  private Receiver receiver;
  private Transmitter transmitter;
  private Consumer<MidiMessage> messageCallback;
  private Consumer<Sound> soundDumpCallback;
  private Consumer<MultiPatch> multiDumpCallback;
  private Consumer<GlobalParametersData> globalDumpCallback;
  private final ExecutorService dumpExecutor = Executors.newSingleThreadExecutor();
  
  private volatile boolean dumping = false;
  private int dumpBank;
  private int dumpStartProgram;
  private int dumpEndProgram;
  private int dumpCurrentProgram;
  private Consumer<Integer> dumpProgressCallback;
  private static final int DUMP_DELAY_MS = 150;

  public MidiService(BlofeldProtocol protocol) {
    this.protocol = protocol;
  }

  public List<MidiDevice.Info> listDevices() {
    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
    List<MidiDevice.Info> devices = new ArrayList<>();
    for (MidiDevice.Info info : infos) {
      try {
        MidiDevice device = MidiSystem.getMidiDevice(info);
        if (device.getMaxReceivers() != 0 || device.getMaxTransmitters() != 0) {
          devices.add(info);
        }
      } catch (MidiUnavailableException e) {}
    }
    return devices;
  }

  public void connect(MidiDevice.Info deviceInfo) throws MidiException {
    if (deviceInfo == null) throw new MidiException("Device info cannot be null");
    try {
      disconnect();
      String deviceName = deviceInfo.getName();
      MidiDevice.Info[] allDevices = MidiSystem.getMidiDeviceInfo();
      for (MidiDevice.Info info : allDevices) {
        if (!info.getName().equals(deviceName)) continue;
        MidiDevice device = MidiSystem.getMidiDevice(info);
        if (device.getMaxReceivers() != 0 && outputDevice == null) {
          device.open();
          receiver = device.getReceiver();
          outputDevice = device;
        }
        if (device.getMaxTransmitters() != 0 && inputDevice == null) {
          device.open();
          if (transmitter != null) {
            transmitter.close();
          }
          transmitter = device.getTransmitter();
          transmitter.setReceiver(new Receiver() {
            @Override public void send(javax.sound.midi.MidiMessage message, long timeStamp) { handleIncomingMessage(message); }
            @Override public void close() {}
          });
          inputDevice = device;
        }
      }
    } catch (MidiUnavailableException e) {
      throw new MidiException("Failed to connect to MIDI device", e);
    }
  }

  public void disconnect() throws MidiException {
    try {
      if (transmitter != null) transmitter.close();
      if (receiver != null) receiver.close();
      if (outputDevice != null && outputDevice.isOpen()) outputDevice.close();
      if (inputDevice != null && inputDevice.isOpen()) inputDevice.close();
      transmitter = null; receiver = null; outputDevice = null; inputDevice = null;
    } catch (Exception e) {
      throw new MidiException("Failed to disconnect", e);
    }
  }

  public boolean isConnected() { return isOutputConnected() && isInputConnected(); }
  public boolean isOutputConnected() { return receiver != null && outputDevice != null && outputDevice.isOpen(); }
  public boolean isInputConnected() { return inputDevice != null && inputDevice.isOpen(); }

  public String getOutputDeviceName() { return isOutputConnected() ? outputDevice.getDeviceInfo().getName() : "None"; }
  public String getInputDeviceName() { return isInputConnected() ? inputDevice.getDeviceInfo().getName() : "None"; }

  public void send(MidiMessage message) throws MidiException {
    if (!isConnected()) throw new MidiException("Not connected");
    byte[] sysex = protocol.encode(message);
    sendMessage(sysex);
  }

  public void setMessageCallback(Consumer<MidiMessage> callback) { this.messageCallback = callback; }
  public void setSoundDumpCallback(Consumer<Sound> callback) { this.soundDumpCallback = callback; }
  public void setMultiDumpCallback(Consumer<MultiPatch> callback) { this.multiDumpCallback = callback; }
  public void setGlobalDumpCallback(Consumer<GlobalParametersData> callback) { this.globalDumpCallback = callback; }

  /**
   * Sends a Universal Identity Request (F0 7E 7F 06 01 F7).
   */
  public void sendIdentityRequest() throws MidiException {
    byte[] request = {(byte) 0xF0, 0x7E, 0x7F, 0x06, 0x01, (byte) 0xF7};
    try {
      SysexMessage sysex = new SysexMessage(request, request.length);
      receiver.send(sysex, -1);
    } catch (InvalidMidiDataException e) {
      throw new MidiException("Identity Request fail", e);
    }
  }

  public void requestSoundDump(int bank, int program) throws MidiException {
    if (!isConnected()) throw new MidiException("Not connected");
    byte[] sndr = SysExGenerator.generateSoundRequest(bank, program);
    sendMessage(sndr);
  }

  public void requestMultiDump(int bank, int multi) throws MidiException {
    if (!isConnected()) throw new MidiException("Not connected");
    MultiDumpRequest req = new MultiDumpRequest(protocol.getDeviceId(), bank, multi);
    byte[] mulr = protocol.encode(req);
    sendMessage(mulr);
  }

  public void forceMultiMode() throws MidiException {
    // Requesting the Multi Edit Buffer (Bank 127) forces the hardware into Multi mode.
    requestMultiDump(BlofeldProtocol.MULTI_MODE_BANK, 0);
  }

  public void requestGlobalDump() throws MidiException {
    if (!isConnected()) throw new MidiException("Not connected");
    GlobalParametersRequest req = new GlobalParametersRequest(protocol.getDeviceId());
    sendMessage(protocol.encode(req));
  }

  public void sendGlobalDump(GlobalParametersData data) throws MidiException {
    if (!isConnected()) throw new MidiException("Not connected");
    sendMessage(protocol.encode(data));
  }

  public CompletableFuture<Void> switchToMultiMode() {
    try {
      forceMultiMode();
      // Give the hardware a moment to process the mode switch
      return CompletableFuture.runAsync(() -> {
          try { Thread.sleep(150); } catch (InterruptedException ignored) {}
      });
    } catch (MidiException e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<Void> switchToSoundMode() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    Consumer<GlobalParametersData> oneTimeCallback = globalData -> {
      try {
        byte[] modifiedData = globalData.data().clone();
        if (modifiedData.length > 1) modifiedData[1] = 0x00;
        GlobalParametersData modified = new GlobalParametersData(protocol.getDeviceId(), modifiedData);
        sendGlobalDump(modified);
        Thread.sleep(300);
        future.complete(null);
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
      setGlobalDumpCallback(null);
    };
    setGlobalDumpCallback(oneTimeCallback);
    try {
      requestGlobalDump();
      CompletableFuture.delayedExecutor(3, java.util.concurrent.TimeUnit.SECONDS).execute(() -> {
        if (!future.isDone()) future.completeExceptionally(new MidiException("Mode switch timeout"));
      });
    } catch (MidiException e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  public CompletableFuture<Integer> requestBankDump(int bank, Consumer<Sound> soundCallback, Consumer<Integer> progressCallback) {
    dumpBank = bank; dumpStartProgram = 0; dumpEndProgram = 127; dumpCurrentProgram = 0;
    dumpProgressCallback = progressCallback; dumping = true;
    try { requestSoundDump(bank, 0); } catch (MidiException e) { dumping = false; return CompletableFuture.failedFuture(e); }
    return CompletableFuture.supplyAsync(() -> {
      long startTime = System.currentTimeMillis();
      while (dumping && (System.currentTimeMillis() - startTime) < 130000) {
        try { Thread.sleep(100); } catch (InterruptedException e) { dumping = false; break; }
      }
      dumping = false; return dumpCurrentProgram;
    });
  }

  private void handleIncomingMessage(javax.sound.midi.MidiMessage message) {
    if (!(message instanceof SysexMessage sysex)) return;
    try {
      byte[] data = sysex.getMessage();
      if (data.length < 5) return;

      // 1. Check for Universal Identity Reply (F0 7E <ID> 06 02 ...)
      if (data.length >= 10 && (data[1] & 0xFF) == 0x7E && (data[3] & 0xFF) == 0x06 && (data[4] & 0xFF) == 0x02) {
        int detectedId = data[2] & 0xFF;
        System.out.println("[MIDI] Detected Blofeld Device ID: " + detectedId);
        protocol.setDeviceId(detectedId);
        return;
      }

      // 2. Sniff Device ID from Blofeld messages if not yet correctly set
      if (data.length > 3 && data[1] == 0x3E && data[2] == 0x13) {
        int sniffedId = data[3] & 0xFF;
        if (protocol.getDeviceId() == 127 && sniffedId != 127) {
            System.out.println("[MIDI] Sniffed Device ID from traffic: " + sniffedId);
            protocol.setDeviceId(sniffedId);
        }
      }
      
      byte command = data[4];
      // TACTICAL DEBUG
      System.out.printf("[MIDI IN] Len: %d | Cmd: 0x%02X | Dev: %d%n", data.length, command, data[3] & 0xFF);

      if (command == BlofeldProtocol.CMD_SOUND_DUMP) {
        Sound sound = SysExParser.parseSoundDump(data);
        if (sound != null) {
          if (soundDumpCallback != null) soundDumpCallback.accept(sound);
          if (dumping) handleDumpResponse(sound);
          return;
        }
      }
      if (command == BlofeldProtocol.CMD_MULTI_DUMP) {
        MultiPatch multi = SysExParser.parseMultiDump(data);
        if (multi != null) {
          if (multiDumpCallback != null) multiDumpCallback.accept(multi);
          return;
        }
      }
      if (command == BlofeldProtocol.CMD_GLOBAL_DUMP) {
        GlobalParametersData globalData = (GlobalParametersData) protocol.decode(data);
        if (globalData != null && globalDumpCallback != null) {
          globalDumpCallback.accept(globalData);
        }
        return;
      }
      if (messageCallback != null) messageCallback.accept(protocol.decode(data));
    } catch (MidiException e) { System.err.println("MIDI decode error: " + e.getMessage()); }
  }

  private void handleDumpResponse(Sound sound) {
    if (dumpProgressCallback != null) dumpProgressCallback.accept((dumpBank * 128) + sound.getProgram() + 1);
    final int nextBank, nextProgram;
    if (dumpEndProgram >= 128 && sound.getProgram() + 1 >= 128) { nextBank = sound.getBank() + 1; nextProgram = 0; }
    else { nextBank = sound.getBank(); nextProgram = sound.getProgram() + 1; }
    int currentIndex = (sound.getBank() * 128) + sound.getProgram();
    if (currentIndex >= dumpEndProgram) { dumping = false; return; }
    dumpCurrentProgram = currentIndex + 1;
    dumpExecutor.submit(() -> {
      try { Thread.sleep(DUMP_DELAY_MS); if (dumping) requestSoundDump(nextBank, nextProgram); }
      catch (Exception e) { dumping = false; }
    });
  }

  public void sendSoundDump(Sound sound) throws MidiException {
    sendSoundDump(sound, sound.getBank(), sound.getProgram());
  }

  public void sendSoundDump(Sound sound, int bank, int program) throws MidiException {
    SoundDumpData msg = new SoundDumpData(protocol.getDeviceId(), bank, program, sound.getParameters());
    sendMessage(protocol.encode(msg));
  }

  public void sendMultiDump(MultiPatch multi) throws MidiException {
    sendMultiDump(multi, 127, 0);
  }

  public void sendMultiDump(MultiPatch multi, int bank, int prog) throws MidiException {
    MultiDumpData msg = new MultiDumpData(protocol.getDeviceId(), bank, prog, multi.getData());
    sendMessage(protocol.encode(msg));
  }

  public CompletableFuture<Integer> requestAllDumps(Consumer<Sound> soundCallback, Consumer<Integer> progressCallback) {
    dumpBank = 0; dumpStartProgram = 0; dumpEndProgram = 1023; dumpCurrentProgram = 0;
    dumpProgressCallback = progressCallback; dumping = true;
    try { requestSoundDump(0, 0); } catch (MidiException e) { dumping = false; return CompletableFuture.failedFuture(e); }
    return CompletableFuture.supplyAsync(() -> {
      long startTime = System.currentTimeMillis();
      while (dumping && (System.currentTimeMillis() - startTime) < 600000) {
        try { Thread.sleep(100); } catch (InterruptedException e) { dumping = false; break; }
      }
      dumping = false; return dumpCurrentProgram;
    });
  }

  public void auditionSound(Sound sound) throws MidiException {
    sendSoundDump(sound, BlofeldProtocol.EDIT_BUFFER_BANK, 0);
    if (sound.getBank() != null && sound.getProgram() != null) sendProgramChange(sound.getBank(), sound.getProgram());
  }

  public void sendProgramChange(int bank, int program) throws MidiException {
    if (!isConnected()) throw new MidiException("Not connected");
    try {
      ShortMessage bankMsb = new ShortMessage();
      bankMsb.setMessage(ShortMessage.CONTROL_CHANGE, 0, 0, bank);
      receiver.send(bankMsb, -1);
      ShortMessage pc = new ShortMessage();
      pc.setMessage(ShortMessage.PROGRAM_CHANGE, 0, program, 0);
      receiver.send(pc, -1);
    } catch (InvalidMidiDataException e) { throw new MidiException("PC fail", e); }
  }

  public void sendParameterChange(int location, int paramId, int value) throws MidiException {
    if (!isConnected()) return;
    SoundParameterChange msg = new SoundParameterChange(getDeviceId(), location, paramId, value);
    sendMessage(protocol.encode(msg));
  }

  public void sendParameterChange(int paramId, int value) throws MidiException {
    sendParameterChange(0x00, paramId, value);
  }

  public void sendGlobalParameterChange(int paramId, int value) throws MidiException {
    if (!isConnected()) return;
    GlobalParameterChange msg = new GlobalParameterChange(getDeviceId(), paramId, value);
    sendMessage(protocol.encode(msg));
  }

  /**
   * Sends a full 64-wave dump to the hardware.
   *
   * @param messages List of 64 WTBD messages
   * @throws MidiException if hardware sync fails
   */
  public void sendWavetableDump(java.util.List<WavetableDump> messages) throws MidiException {
    if (!isConnected()) return;
    for (WavetableDump msg : messages) {
        sendMessage(protocol.encode(msg));
        try {
            // Small pause to avoid hardware buffer overflow
            Thread.sleep(20);
        } catch (InterruptedException ignored) {}
    }
  }

  private void sendMessage(byte[] data) throws MidiException {
    if (!isOutputConnected()) throw new MidiException("Not connected to output device");
    try {
      System.out.printf("[MIDI OUT] Len: %d | Cmd: 0x%02X | Bytes: ", data.length, data.length > 4 ? data[4] : -1);
      for(int i=0; i<Math.min(data.length, 10); i++) System.out.printf("%02X ", data[i]);
      System.out.println("...");

      SysexMessage sysex = new SysexMessage(data, data.length);
      receiver.send(sysex, -1);
    } catch (InvalidMidiDataException e) { throw new MidiException("Send fail", e); }
  }

  public int getDeviceId() { return protocol.getDeviceId(); }
  
  @Override 
  public void close() throws Exception { 
    dumping = false;
    dumpExecutor.shutdown();
    try {
      if (!dumpExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
        dumpExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      dumpExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
    disconnect(); 
  }
}

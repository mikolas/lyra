package net.mikolas.lyra.model;

import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Application settings using Java Preferences API.
 */
public class AppSettings {

    private final Preferences prefs;

    public AppSettings() {
        this(Preferences.userNodeForPackage(AppSettings.class));
    }

    public AppSettings(Preferences prefs) {
        this.prefs = prefs;
    }

    // Database settings

    public String getDatabasePath() {
        String home = System.getProperty("user.home");
        String defaultPath = home + "/.lyra/lyra.db";
        return prefs.get("database.path", defaultPath);
    }

    public void setDatabasePath(String path) {
        prefs.put("database.path", path);
    }

    public boolean isDatabaseBackupEnabled() {
        return prefs.getBoolean("database.backup.enabled", true);
    }

    public void setDatabaseBackupEnabled(boolean enabled) {
        prefs.putBoolean("database.backup.enabled", enabled);
    }

    public int getBackupIntervalMinutes() {
        return prefs.getInt("database.backup.interval", 15);
    }

    public void setBackupIntervalMinutes(int minutes) {
        if (minutes < 5 || minutes > 30) {
            throw new IllegalArgumentException("Backup interval must be between 5 and 30 minutes");
        }
        prefs.putInt("database.backup.interval", minutes);
    }

    // MIDI settings

    public int getDeviceId() {
        return prefs.getInt("midi.deviceId", 0);
    }

    public void setDeviceId(int id) {
        if (id < 0 || id > 127) {
            throw new IllegalArgumentException("Device ID must be between 0 and 127");
        }
        prefs.putInt("midi.deviceId", id);
    }

    public boolean isAutoConnectEnabled() {
        return prefs.getBoolean("midi.autoConnect", true);
    }

    public void setAutoConnectEnabled(boolean enabled) {
        prefs.putBoolean("midi.autoConnect", enabled);
    }

    public Set<Integer> getSendChannels() {
        String channels = prefs.get("midi.sendChannels", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        return parseChannels(channels);
    }

    public void setSendChannels(Set<Integer> channels) {
        String channelsStr = channels.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        prefs.put("midi.sendChannels", channelsStr);
    }

    public Set<Integer> getReceiveChannels() {
        String channels = prefs.get("midi.receiveChannels", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        return parseChannels(channels);
    }

    public void setReceiveChannels(Set<Integer> channels) {
        String channelsStr = channels.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        prefs.put("midi.receiveChannels", channelsStr);
    }

    private Set<Integer> parseChannels(String channels) {
        Set<Integer> result = new HashSet<>();
        for (String ch : channels.split(",")) {
            try {
                result.add(Integer.parseInt(ch.trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid channels
            }
        }
        return result;
    }

    // Editor settings

    public AutosaveMode getAutosaveMode() {
        String mode = prefs.get("editor.autosave", AutosaveMode.REMEMBER_LAST.name());
        try {
            return AutosaveMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return AutosaveMode.REMEMBER_LAST;
        }
    }

    public void setAutosaveMode(AutosaveMode mode) {
        prefs.put("editor.autosave", mode.name());
    }

    public MidiFilterMode getMidiReceiveMode() {
        String mode = prefs.get("editor.midiReceive", MidiFilterMode.ALL_EVENTS.name());
        try {
            return MidiFilterMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return MidiFilterMode.ALL_EVENTS;
        }
    }

    public void setMidiReceiveMode(MidiFilterMode mode) {
        prefs.put("editor.midiReceive", mode.name());
    }

    public MidiFilterMode getMidiSendMode() {
        String mode = prefs.get("editor.midiSend", MidiFilterMode.ALL_EVENTS.name());
        try {
            return MidiFilterMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return MidiFilterMode.ALL_EVENTS;
        }
    }

    public void setMidiSendMode(MidiFilterMode mode) {
        prefs.put("editor.midiSend", mode.name());
    }

    // Miscellaneous settings

    public boolean isSingleClickProgramChange() {
        return prefs.getBoolean("misc.singleClickProgramChange", false);
    }

    public void setSingleClickProgramChange(boolean enabled) {
        prefs.putBoolean("misc.singleClickProgramChange", enabled);
    }

    public boolean isUpdateCheckEnabled() {
        return prefs.getBoolean("misc.updateCheck", true);
    }

    public void setUpdateCheckEnabled(boolean enabled) {
        prefs.putBoolean("misc.updateCheck", enabled);
    }
}

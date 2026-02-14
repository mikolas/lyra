package net.mikolas.lyra.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class AppSettingsTest {

    private AppSettings settings;
    private Preferences testPrefs;

    @BeforeEach
    void setUp() {
        // Use test node to avoid polluting real preferences
        testPrefs = Preferences.userRoot().node("lyra-test-" + System.currentTimeMillis());
        settings = new AppSettings(testPrefs);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        // Clean up test preferences
        testPrefs.removeNode();
    }

    @Test
    void testDefaultDatabasePath() {
        String path = settings.getDatabasePath();
        assertNotNull(path);
        assertTrue(path.contains(".lyra"));
        assertTrue(path.endsWith("lyra.db"));
    }

    @Test
    void testSetDatabasePath() {
        settings.setDatabasePath("/custom/path/db.db");
        assertEquals("/custom/path/db.db", settings.getDatabasePath());
    }

    @Test
    void testDefaultBackupEnabled() {
        assertTrue(settings.isDatabaseBackupEnabled());
    }

    @Test
    void testSetBackupEnabled() {
        settings.setDatabaseBackupEnabled(false);
        assertFalse(settings.isDatabaseBackupEnabled());
    }

    @Test
    void testDefaultBackupInterval() {
        assertEquals(15, settings.getBackupIntervalMinutes());
    }

    @Test
    void testSetBackupInterval() {
        settings.setBackupIntervalMinutes(30);
        assertEquals(30, settings.getBackupIntervalMinutes());
    }

    @Test
    void testBackupIntervalValidation() {
        assertThrows(IllegalArgumentException.class, () -> settings.setBackupIntervalMinutes(4));
        assertThrows(IllegalArgumentException.class, () -> settings.setBackupIntervalMinutes(31));
    }

    @Test
    void testDefaultDeviceId() {
        assertEquals(0, settings.getDeviceId());
    }

    @Test
    void testSetDeviceId() {
        settings.setDeviceId(127);
        assertEquals(127, settings.getDeviceId());
    }

    @Test
    void testDeviceIdValidation() {
        assertThrows(IllegalArgumentException.class, () -> settings.setDeviceId(-1));
        assertThrows(IllegalArgumentException.class, () -> settings.setDeviceId(128));
    }

    @Test
    void testDefaultAutoConnect() {
        assertTrue(settings.isAutoConnectEnabled());
    }

    @Test
    void testSetAutoConnect() {
        settings.setAutoConnectEnabled(false);
        assertFalse(settings.isAutoConnectEnabled());
    }

    @Test
    void testDefaultSendChannels() {
        Set<Integer> channels = settings.getSendChannels();
        assertEquals(16, channels.size()); // All channels by default
    }

    @Test
    void testSetSendChannels() {
        Set<Integer> channels = Set.of(1, 2, 3);
        settings.setSendChannels(channels);
        assertEquals(channels, settings.getSendChannels());
    }

    @Test
    void testDefaultReceiveChannels() {
        Set<Integer> channels = settings.getReceiveChannels();
        assertEquals(16, channels.size()); // All channels by default
    }

    @Test
    void testSetReceiveChannels() {
        Set<Integer> channels = Set.of(4, 5, 6);
        settings.setReceiveChannels(channels);
        assertEquals(channels, settings.getReceiveChannels());
    }

    @Test
    void testDefaultAutosaveMode() {
        assertEquals(AutosaveMode.REMEMBER_LAST, settings.getAutosaveMode());
    }

    @Test
    void testSetAutosaveMode() {
        settings.setAutosaveMode(AutosaveMode.ALWAYS_ON);
        assertEquals(AutosaveMode.ALWAYS_ON, settings.getAutosaveMode());
    }

    @Test
    void testDefaultMidiReceiveMode() {
        assertEquals(MidiFilterMode.ALL_EVENTS, settings.getMidiReceiveMode());
    }

    @Test
    void testSetMidiReceiveMode() {
        settings.setMidiReceiveMode(MidiFilterMode.CTRL_SYSEX);
        assertEquals(MidiFilterMode.CTRL_SYSEX, settings.getMidiReceiveMode());
    }

    @Test
    void testDefaultMidiSendMode() {
        assertEquals(MidiFilterMode.ALL_EVENTS, settings.getMidiSendMode());
    }

    @Test
    void testSetMidiSendMode() {
        settings.setMidiSendMode(MidiFilterMode.PROGRAM_CHANGES);
        assertEquals(MidiFilterMode.PROGRAM_CHANGES, settings.getMidiSendMode());
    }

    @Test
    void testDefaultSingleClickProgramChange() {
        assertFalse(settings.isSingleClickProgramChange());
    }

    @Test
    void testSetSingleClickProgramChange() {
        settings.setSingleClickProgramChange(true);
        assertTrue(settings.isSingleClickProgramChange());
    }

    @Test
    void testDefaultUpdateCheck() {
        assertTrue(settings.isUpdateCheckEnabled());
    }

    @Test
    void testSetUpdateCheck() {
        settings.setUpdateCheckEnabled(false);
        assertFalse(settings.isUpdateCheckEnabled());
    }

    @Test
    void testPersistence() {
        settings.setDatabasePath("/test/path.db");
        settings.setDeviceId(42);
        settings.setAutosaveMode(AutosaveMode.ALWAYS_OFF);
        
        // Create new instance with same prefs
        AppSettings newSettings = new AppSettings(testPrefs);
        
        assertEquals("/test/path.db", newSettings.getDatabasePath());
        assertEquals(42, newSettings.getDeviceId());
        assertEquals(AutosaveMode.ALWAYS_OFF, newSettings.getAutosaveMode());
    }
}

package net.mikolas.lyra.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.mikolas.lyra.model.Sound;

import java.sql.SQLException;
import java.util.List;

/**
 * Single source of truth for the Sound library.
 * Provides observable data for UI components.
 */
public class SoundRepository {
    private static SoundRepository instance;
    
    private Database database;
    private final ObservableList<Sound> allSounds = FXCollections.observableArrayList();

    private SoundRepository() {
        database = Database.getInstance();
        refresh();
    }

    public static synchronized SoundRepository getInstance() {
        if (instance == null) {
            instance = new SoundRepository();
        }
        return instance;
    }

    public Database getDatabase() {
        return database;
    }

    public ObservableList<Sound> getAllSounds() {
        return allSounds;
    }

    /**
     * Re-queries the database and updates the observable list.
     */
    public void refresh() {
        if (database == null) return;
        try {
            List<Sound> sounds = database.sounds.queryForAll();
            allSounds.setAll(sounds);
        } catch (SQLException e) {
            System.err.println("Failed to refresh sounds: " + e.getMessage());
        }
    }

    /**
     * Returns a filtered list of sounds for a specific bank.
     * @param bankIndex 0-7 (Bank A-H)
     * @return FilteredList of sounds
     */
    public FilteredList<Sound> getSoundsByBank(int bankIndex) {
        return new FilteredList<>(allSounds, sound -> 
            sound.getBank() != null && sound.getBank() == bankIndex);
    }

    /**
     * Finds a sound in the repository by its bank and program index.
     */
    public Sound getSoundByBankAndProgram(int bank, int program) {
        return allSounds.stream()
            .filter(s -> s.getBank() != null && s.getBank() == bank && s.getProgram() != null && s.getProgram() == program)
            .findFirst()
            .orElse(null);
    }
    
    public void shutdown() {
        if (database != null) {
            try {
                database.close();
            } catch (Exception e) {
                System.err.println("Error closing database: " + e.getMessage());
            }
        }
    }
}

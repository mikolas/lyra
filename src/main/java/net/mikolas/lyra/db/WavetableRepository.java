package net.mikolas.lyra.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.mikolas.lyra.model.Wavetable;

import java.sql.SQLException;
import java.util.List;

/**
 * Single source of truth for the Wavetable library.
 */
public class WavetableRepository {
    private static WavetableRepository instance;
    
    private Database database;
    private final ObservableList<Wavetable> allWavetables = FXCollections.observableArrayList();

    private WavetableRepository() {
        database = Database.getInstance();
        refresh();
        // Load factory wavetables from binary resources if not present in DB
        net.mikolas.lyra.service.PresetLoader.loadFactoryPresets(this);
    }

    public static synchronized WavetableRepository getInstance() {
        if (instance == null) {
            instance = new WavetableRepository();
        }
        return instance;
    }

    public void refresh() {
        if (database == null) return;
        try {
            List<Wavetable> wavetables = database.wavetables.queryForAll();
            allWavetables.setAll(wavetables);
        } catch (SQLException e) {
            System.err.println("Failed to refresh wavetables: " + e.getMessage());
        }
    }

    public ObservableList<Wavetable> getAllWavetables() {
        return allWavetables;
    }

    /**
     * Finds the next available Blofeld User Wavetable slot (80-118).
     * @return slot number, or null if all 39 user slots are full.
     */
    public Integer findNextAvailableSlot() {
        List<Integer> usedSlots = allWavetables.stream()
            .map(Wavetable::getSlot)
            .filter(s -> s != null && s >= 80 && s <= 118)
            .toList();
        
        for (int i = 80; i <= 118; i++) {
            if (!usedSlots.contains(i)) {
                return i;
            }
        }
        return null;
    }

    public void save(Wavetable wavetable) throws SQLException {
        if (database == null) return;
        database.wavetables.createOrUpdate(wavetable);
        refresh();
    }

    public void delete(Wavetable wavetable) throws SQLException {
        if (database == null) return;
        database.wavetables.delete(wavetable);
        refresh();
    }
}

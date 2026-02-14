package net.mikolas.lyra.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.mikolas.lyra.model.MultiPatch;

import java.sql.SQLException;
import java.util.List;

public class MultiRepository {
    private static MultiRepository instance;
    private Database database;
    private final ObservableList<MultiPatch> allMultis = FXCollections.observableArrayList();

    private MultiRepository() {
        database = Database.getInstance();
        refresh();
    }

    public static synchronized MultiRepository getInstance() {
        if (instance == null) {
            instance = new MultiRepository();
        }
        return instance;
    }

    public void refresh() {
        if (database == null) return;
        try {
            List<MultiPatch> multis = database.multis.queryForAll();
            allMultis.setAll(multis);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(MultiPatch patch) {
        if (database == null) return;
        try {
            database.multis.createOrUpdate(patch);
            refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<MultiPatch> getAllMultis() {
        return allMultis;
    }
}
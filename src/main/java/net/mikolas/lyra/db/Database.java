package net.mikolas.lyra.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import net.mikolas.lyra.model.Collection;
import net.mikolas.lyra.model.MultiPatch;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundCollection;
import net.mikolas.lyra.model.SoundTag;
import net.mikolas.lyra.model.Tag;
import net.mikolas.lyra.model.Wavetable;

/**
 * Database facade providing access to all DAOs.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * Database db = new Database("lyra.db");
 * db.sounds.create(sound);
 * Sound loaded = db.sounds.queryForId(42);
 * db.close();
 * }</pre>
 */
public class Database implements AutoCloseable {
  private static Database instance;
  private final DatabaseManager dbManager;

  public final Dao<Sound, Integer> sounds;
  public final Dao<MultiPatch, Integer> multis;
  public final Dao<Wavetable, Integer> wavetables;
  public final Dao<Collection, Integer> collections;
  public final Dao<Tag, Integer> tags;
  public final Dao<SoundCollection, Integer> soundCollections;
  public final Dao<SoundTag, Integer> soundTags;

  private Database() throws SQLException {
    this(DatabaseManager.getDefaultDatabasePath());
  }

  public Database(String dbPath) throws SQLException {
    dbManager = new DatabaseManager(dbPath);

    sounds = DaoManager.createDao(dbManager.getConnectionSource(), Sound.class);
    multis = DaoManager.createDao(dbManager.getConnectionSource(), MultiPatch.class);
    wavetables = DaoManager.createDao(dbManager.getConnectionSource(), Wavetable.class);
    collections = DaoManager.createDao(dbManager.getConnectionSource(), Collection.class);
    tags = DaoManager.createDao(dbManager.getConnectionSource(), Tag.class);
    soundCollections =
        DaoManager.createDao(dbManager.getConnectionSource(), SoundCollection.class);
    soundTags = DaoManager.createDao(dbManager.getConnectionSource(), SoundTag.class);

    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), Sound.class);
    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), MultiPatch.class);
    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), Wavetable.class);
    
    // Manual migration for isFactory column
    try (var results = wavetables.queryRaw("PRAGMA table_info(wavetables)")) {
        boolean exists = false;
        for (String[] row : results) {
            if (row.length > 1 && "isFactory".equalsIgnoreCase(row[1])) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            wavetables.executeRaw("ALTER TABLE wavetables ADD COLUMN isFactory BOOLEAN DEFAULT 0");
        }
    } catch (Exception ignored) {
    }

    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), Collection.class);
    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), Tag.class);
    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), SoundCollection.class);
    TableUtils.createTableIfNotExists(dbManager.getConnectionSource(), SoundTag.class);
  }

  public static synchronized Database getInstance() {
    if (instance == null) {
      try {
        instance = new Database();
      } catch (SQLException e) {
        throw new RuntimeException("Failed to initialize database", e);
      }
    }
    return instance;
  }

  /**
   * Shutdown the singleton instance and close all database connections.
   * Should be called on application exit.
   */
  public static synchronized void shutdown() {
    if (instance != null) {
      try {
        instance.close();
      } catch (Exception e) {
        System.err.println("Error closing database: " + e.getMessage());
      } finally {
        instance = null;
      }
    }
  }

  @Override
  public void close() throws Exception {
    dbManager.close();
  }
}

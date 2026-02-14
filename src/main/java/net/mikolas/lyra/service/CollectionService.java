package net.mikolas.lyra.service;

import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.model.Collection;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundCollection;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing sound collections.
 */
public class CollectionService {

  private final Database database;

  public CollectionService(Database database) {
    this.database = database;
  }

  /**
   * Add sounds to a collection.
   *
   * @param sounds sounds to add
   * @param collection target collection
   * @return number of sounds added
   * @throws SQLException if database operation fails
   */
  public int addSoundsToCollection(List<Sound> sounds, Collection collection) throws SQLException {
    int added = 0;
    for (Sound sound : sounds) {
      SoundCollection sc = SoundCollection.builder()
          .sound(sound)
          .collection(collection)
          .build();
      
      try {
        database.soundCollections.create(sc);
        database.sounds.refresh(sound);
        added++;
      } catch (SQLException e) {
        // Ignore duplicate constraint violations
        if (!e.getMessage().contains("UNIQUE") && !e.getMessage().contains("unique")) {
          throw e;
        }
      }
    }
    return added;
  }

  /**
   * Remove sounds from a collection.
   *
   * @param sounds sounds to remove
   * @param collection target collection
   * @return number of sounds removed
   * @throws SQLException if database operation fails
   */
  public int removeSoundsFromCollection(List<Sound> sounds, Collection collection) throws SQLException {
    int removed = 0;
    for (Sound sound : sounds) {
      List<SoundCollection> existing = database.soundCollections.queryBuilder()
          .where()
          .eq("sound_id", sound.getId())
          .and()
          .eq("collection_id", collection.getId())
          .query();
      
      for (SoundCollection sc : existing) {
        database.soundCollections.delete(sc);
        removed++;
      }
      
      if (!existing.isEmpty()) {
        database.sounds.refresh(sound);
      }
    }
    return removed;
  }
}

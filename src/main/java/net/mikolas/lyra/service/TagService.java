package net.mikolas.lyra.service;

import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundTag;
import net.mikolas.lyra.model.Tag;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing sound tags.
 */
public class TagService {

  private final Database database;

  public TagService(Database database) {
    this.database = database;
  }

  /**
   * Add tag to sounds.
   *
   * @param sounds sounds to tag
   * @param tag tag to add
   * @return number of sounds tagged
   * @throws SQLException if database operation fails
   */
  public int addTagsToSounds(List<Sound> sounds, Tag tag) throws SQLException {
    int added = 0;
    for (Sound sound : sounds) {
      SoundTag st = SoundTag.builder()
          .sound(sound)
          .tag(tag)
          .build();
      
      try {
        database.soundTags.create(st);
        database.sounds.refresh(sound);
        added++;
      } catch (SQLException e) {
        if (!e.getMessage().contains("UNIQUE") && !e.getMessage().contains("unique")) {
          throw e;
        }
      }
    }
    return added;
  }

  /**
   * Remove tag from sounds.
   *
   * @param sounds sounds to untag
   * @param tag tag to remove
   * @return number of sounds untagged
   * @throws SQLException if database operation fails
   */
  public int removeTagsFromSounds(List<Sound> sounds, Tag tag) throws SQLException {
    int removed = 0;
    for (Sound sound : sounds) {
      List<SoundTag> existing = database.soundTags.queryBuilder()
          .where()
          .eq("sound_id", sound.getId())
          .and()
          .eq("tag_id", tag.getId())
          .query();
      
      for (SoundTag st : existing) {
        database.soundTags.delete(st);
        removed++;
      }
      
      if (!existing.isEmpty()) {
        database.sounds.refresh(sound);
      }
    }
    return removed;
  }
}

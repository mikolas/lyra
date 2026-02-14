package net.mikolas.lyra.service;

import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundTag;
import net.mikolas.lyra.model.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagServiceTest {

  private TagService service;
  private Database database;

  @BeforeEach
  void setUp() throws SQLException {
    database = new Database(":memory:");
    service = new TagService(database);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (database != null) {
      database.close();
    }
  }

  @Test
  void testAddTagsToSounds() throws SQLException {
    Tag tag = Tag.builder().name("Test Tag").build();
    database.tags.create(tag);

    Sound sound1 = Sound.builder().name("Sound 1").parameters(new byte[385]).build();
    Sound sound2 = Sound.builder().name("Sound 2").parameters(new byte[385]).build();
    database.sounds.create(sound1);
    database.sounds.create(sound2);

    int added = service.addTagsToSounds(List.of(sound1, sound2), tag);

    assertEquals(2, added);
  }

  @Test
  void testAddTagsToSoundsEmpty() throws SQLException {
    Tag tag = Tag.builder().name("Test Tag").build();
    database.tags.create(tag);

    int added = service.addTagsToSounds(List.of(), tag);

    assertEquals(0, added);
  }

  @Test
  void testRemoveTagsFromSounds() throws SQLException {
    Tag tag = Tag.builder().name("Test Tag").build();
    database.tags.create(tag);

    Sound sound = Sound.builder().name("Sound 1").parameters(new byte[385]).build();
    database.sounds.create(sound);

    service.addTagsToSounds(List.of(sound), tag);
    int removed = service.removeTagsFromSounds(List.of(sound), tag);

    assertEquals(1, removed);
  }
}

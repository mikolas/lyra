package net.mikolas.lyra.service;

import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.model.Collection;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundCollection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionServiceTest {

  private CollectionService service;
  private Database database;

  @BeforeEach
  void setUp() throws SQLException {
    database = new Database(":memory:");
    service = new CollectionService(database);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (database != null) {
      database.close();
    }
  }

  @Test
  void testAddSoundsToCollection() throws SQLException {
    Collection collection = Collection.builder().name("Test Collection").build();
    database.collections.create(collection);

    Sound sound1 = Sound.builder().name("Sound 1").parameters(new byte[385]).build();
    Sound sound2 = Sound.builder().name("Sound 2").parameters(new byte[385]).build();
    database.sounds.create(sound1);
    database.sounds.create(sound2);

    int added = service.addSoundsToCollection(List.of(sound1, sound2), collection);

    assertEquals(2, added);
  }

  @Test
  void testAddSoundsToCollectionEmpty() throws SQLException {
    Collection collection = Collection.builder().name("Test Collection").build();
    database.collections.create(collection);

    int added = service.addSoundsToCollection(List.of(), collection);

    assertEquals(0, added);
  }

  @Test
  void testRemoveSoundsFromCollection() throws SQLException {
    Collection collection = Collection.builder().name("Test Collection").build();
    database.collections.create(collection);

    Sound sound = Sound.builder().name("Sound 1").parameters(new byte[385]).build();
    database.sounds.create(sound);

    service.addSoundsToCollection(List.of(sound), collection);
    int removed = service.removeSoundsFromCollection(List.of(sound), collection);

    assertEquals(1, removed);
  }
}

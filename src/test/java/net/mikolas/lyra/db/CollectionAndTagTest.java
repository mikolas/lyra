package net.mikolas.lyra.db;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import net.mikolas.lyra.model.Collection;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundCollection;
import net.mikolas.lyra.model.SoundTag;
import net.mikolas.lyra.model.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionAndTagTest {
  private Database db;

  @BeforeEach
  void setUp() throws Exception {
    db = new Database(":memory:");
  }

  @AfterEach
  void tearDown() throws Exception {
    db.close();
  }

  @Test
  void testSoundCollectionRelationship() throws SQLException {
    Sound sound = Sound.builder().name("Bass Sound").parameters(new byte[385]).build();
    db.sounds.create(sound);

    Collection collection = Collection.builder().name("Favorites").build();
    db.collections.create(collection);

    SoundCollection join = SoundCollection.builder()
        .sound(sound)
        .collection(collection)
        .build();
    db.soundCollections.create(join);

    // Refresh sound to load foreign collection
    db.sounds.refresh(sound);

    assertNotNull(sound.getSoundCollections());
    assertEquals(1, sound.getSoundCollections().size());
    
    SoundCollection resultJoin = sound.getSoundCollections().iterator().next();
    assertEquals("Favorites", resultJoin.getCollection().getName());
  }

  @Test
  void testSoundTagRelationship() throws SQLException {
    Sound sound = Sound.builder().name("Lead Sound").parameters(new byte[385]).build();
    db.sounds.create(sound);

    Tag tag = Tag.builder().name("Bright").color("#FFFF00").build();
    db.tags.create(tag);

    SoundTag join = SoundTag.builder()
        .sound(sound)
        .tag(tag)
        .build();
    db.soundTags.create(join);

    // Refresh sound to load foreign collection
    db.sounds.refresh(sound);

    assertNotNull(sound.getSoundTags());
    assertEquals(1, sound.getSoundTags().size());
    
    SoundTag resultJoin = sound.getSoundTags().iterator().next();
    assertEquals("Bright", resultJoin.getTag().getName());
  }

  @Test
  void testMultipleTagsForOneSound() throws SQLException {
    Sound sound = Sound.builder().name("Multi Sound").parameters(new byte[385]).build();
    db.sounds.create(sound);

    Tag tag1 = Tag.builder().name("Analog").build();
    Tag tag2 = Tag.builder().name("Warm").build();
    db.tags.create(tag1);
    db.tags.create(tag2);

    db.soundTags.create(SoundTag.builder().sound(sound).tag(tag1).build());
    db.soundTags.create(SoundTag.builder().sound(sound).tag(tag2).build());

    db.sounds.refresh(sound);
    assertEquals(2, sound.getSoundTags().size());
  }

  @Test
  void testMultipleSoundsInCollection() throws SQLException {
    Collection collection = Collection.builder().name("Live Set").build();
    db.collections.create(collection);

    Sound s1 = Sound.builder().name("S1").parameters(new byte[385]).build();
    Sound s2 = Sound.builder().name("S2").parameters(new byte[385]).build();
    db.sounds.create(s1);
    db.sounds.create(s2);

    db.soundCollections.create(SoundCollection.builder().sound(s1).collection(collection).build());
    db.soundCollections.create(SoundCollection.builder().sound(s2).collection(collection).build());

    // Verify through soundCollections DAO
    List<SoundCollection> joins = db.soundCollections.queryBuilder()
        .where().eq("collection_id", collection.getId()).query();
    assertEquals(2, joins.size());
  }

  @Test
  void testDeleteCollectionRemovesAssociations() throws SQLException {
    Sound sound = Sound.builder().name("S1").parameters(new byte[385]).build();
    db.sounds.create(sound);

    Collection collection = Collection.builder().name("To Delete").build();
    db.collections.create(collection);

    db.soundCollections.create(SoundCollection.builder().sound(sound).collection(collection).build());

    // Manually delete associations (simulating what the controller should do)
    var dbldr = db.soundCollections.deleteBuilder();
    dbldr.where().eq("collection_id", collection.getId());
    dbldr.delete();
    db.collections.delete(collection);

    assertEquals(0, db.soundCollections.queryForAll().size());
    assertEquals(0, db.collections.queryForAll().size());
    assertNotNull(db.sounds.queryForId(sound.getId())); // Sound should still exist
  }

  @Test
  void testSoundDeletionCleanup() throws SQLException {
    Sound sound = Sound.builder().name("S1").parameters(new byte[385]).build();
    db.sounds.create(sound);

    Collection collection = Collection.builder().name("C1").build();
    db.collections.create(collection);

    Tag tag = Tag.builder().name("T1").build();
    db.tags.create(tag);

    db.soundCollections.create(SoundCollection.builder().sound(sound).collection(collection).build());
    db.soundTags.create(SoundTag.builder().sound(sound).tag(tag).build());

    // Manually delete associations (simulating what the controller should do)
    var scBldr = db.soundCollections.deleteBuilder();
    scBldr.where().eq("sound_id", sound.getId());
    scBldr.delete();

    var stBldr = db.soundTags.deleteBuilder();
    stBldr.where().eq("sound_id", sound.getId());
    stBldr.delete();

    db.sounds.delete(sound);

    assertEquals(0, db.soundCollections.queryForAll().size(), "Sound-Collection associations should be gone");
    assertEquals(0, db.soundTags.queryForAll().size(), "Sound-Tag associations should be gone");
    assertEquals(0, db.sounds.queryForAll().size(), "Sound should be gone");
  }
}
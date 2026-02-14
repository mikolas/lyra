package net.mikolas.lyra.db;

import static org.junit.jupiter.api.Assertions.*;

import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import java.util.List;
import net.mikolas.lyra.model.Sound;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for Sound database operations using ORMLite directly.
 */
class SoundDatabaseTest {
  private Dao<Sound, Integer> soundDao;

  @BeforeEach
  void setUp() throws Exception {
    Database db = new Database(":memory:");
    soundDao = db.sounds;
  }

  @AfterEach
  void tearDown() throws Exception {
    // Database auto-closes
  }

  @Test
  void testSaveSound() throws Exception {
    Sound sound =
        Sound.builder().name("Test Sound").category(3).parameters(new byte[385]).build(); // Bass = 3

    soundDao.create(sound);

    assertNotNull(sound.getId());
    assertEquals("Test Sound", sound.getName());
    assertEquals(Integer.valueOf(3), sound.getCategory());
  }

  @Test
  void testFindById() throws Exception {
    Sound sound = Sound.builder().name("Test").parameters(new byte[385]).build();
    soundDao.create(sound);

    Sound found = soundDao.queryForId(sound.getId());

    assertNotNull(found);
    assertEquals(sound.getId(), found.getId());
    assertEquals("Test", found.getName());
  }

  @Test
  void testFindByIdNotFound() throws Exception {
    Sound found = soundDao.queryForId(999);
    assertNull(found);
  }

  @Test
  void testFindByBankProgram() throws Exception {
    Sound sound =
        Sound.builder().name("Test").bank(0).program(14).parameters(new byte[385]).build();
    soundDao.create(sound);

    List<Sound> results =
        soundDao.queryBuilder().where().eq("bank", 0).and().eq("program", 14).query();

    assertEquals(1, results.size());
    assertEquals("Test", results.get(0).getName());
    assertEquals(0, results.get(0).getBank());
    assertEquals(14, results.get(0).getProgram());
  }

  @Test
  void testFindByName() throws Exception {
    soundDao.create(Sound.builder().name("Bass Sound").parameters(new byte[385]).build());
    soundDao.create(Sound.builder().name("Lead Sound").parameters(new byte[385]).build());
    soundDao.create(Sound.builder().name("Bass Pad").parameters(new byte[385]).build());

    List<Sound> results = soundDao.queryBuilder().where().like("name", "%Bass%").query();

    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(s -> s.getName().contains("Bass")));
  }

  @Test
  void testFindByCategory() throws Exception {
    soundDao.create(
        Sound.builder().name("Sound1").category(3).parameters(new byte[385]).build()); // Bass = 3
    soundDao.create(
        Sound.builder().name("Sound2").category(7).parameters(new byte[385]).build()); // Lead = 7
    soundDao.create(
        Sound.builder().name("Sound3").category(3).parameters(new byte[385]).build()); // Bass = 3

    List<Sound> results = soundDao.queryBuilder().where().eq("category", 3).query();

    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(s -> Integer.valueOf(3).equals(s.getCategory())));
  }

  @Test
  void testFindAll() throws Exception {
    soundDao.create(Sound.builder().name("Sound1").parameters(new byte[385]).build());
    soundDao.create(Sound.builder().name("Sound2").parameters(new byte[385]).build());
    soundDao.create(Sound.builder().name("Sound3").parameters(new byte[385]).build());

    List<Sound> results = soundDao.queryForAll();

    assertEquals(3, results.size());
  }

  @Test
  void testUpdate() throws Exception {
    Sound sound = Sound.builder().name("Original").parameters(new byte[385]).build();
    soundDao.create(sound);

    sound.setName("Updated");
    soundDao.update(sound);

    Sound updated = soundDao.queryForId(sound.getId());
    assertEquals("Updated", updated.getName());
  }

  @Test
  void testDelete() throws Exception {
    Sound sound = Sound.builder().name("Test").parameters(new byte[385]).build();
    soundDao.create(sound);

    soundDao.deleteById(sound.getId());

    Sound found = soundDao.queryForId(sound.getId());
    assertNull(found);
  }

  @Test
  void testDeleteNonExistent() throws Exception {
    int deleted = soundDao.deleteById(999);
    assertEquals(0, deleted);
  }

  @Test
  void testParametersStoredCorrectly() throws Exception {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = (byte) (i % 128);
    }

    Sound sound = Sound.builder().name("Test").parameters(params).build();
    soundDao.create(sound);

    Sound loaded = soundDao.queryForId(sound.getId());
    assertArrayEquals(params, loaded.getParameters());
  }

  @Test
  void testUniqueBankProgram() throws Exception {
    soundDao.create(
        Sound.builder().name("Sound1").bank(0).program(1).parameters(new byte[385]).build());

    assertThrows(
        SQLException.class,
        () ->
            soundDao.create(
                Sound.builder()
                    .name("Sound2")
                    .bank(0)
                    .program(1)
                    .parameters(new byte[385])
                    .build()));
  }
}

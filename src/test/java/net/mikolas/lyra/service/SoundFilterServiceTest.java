package net.mikolas.lyra.service;

import net.mikolas.lyra.model.Collection;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundCollection;
import net.mikolas.lyra.model.SoundTag;
import net.mikolas.lyra.model.Tag;
import net.mikolas.lyra.ui.FilterType;
import net.mikolas.lyra.ui.SoundFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SoundFilterServiceTest {

  private SoundFilterService service;
  private List<Sound> testSounds;

  @BeforeEach
  void setUp() {
    service = new SoundFilterService();
    testSounds = createTestSounds();
  }

  @Test
  void testFilterByTextName() {
    List<SoundFilter> filters = List.of();
    
    List<Sound> result = service.filter(testSounds, "bass", filters);
    
    assertEquals(1, result.size());
    assertEquals("Bass 1", result.get(0).getName());
  }

  @Test
  void testFilterByTextCaseInsensitive() {
    List<SoundFilter> filters = List.of();
    
    List<Sound> result = service.filter(testSounds, "LEAD", filters);
    
    assertEquals(1, result.size());
    assertEquals("Lead 1", result.get(0).getName());
  }

  @Test
  void testFilterByBank() {
    List<SoundFilter> filters = List.of(new SoundFilter(FilterType.BANK, "A"));
    
    List<Sound> result = service.filter(testSounds, "", filters);
    
    assertEquals(2, result.size());
  }

  @Test
  void testFilterByBankB() {
    List<SoundFilter> filters = List.of(new SoundFilter(FilterType.BANK, "B"));
    
    List<Sound> result = service.filter(testSounds, "", filters);
    
    assertEquals(1, result.size());
    assertEquals("Pad 1", result.get(0).getName());
  }

  @Test
  void testFilterByCategory() {
    List<SoundFilter> filters = List.of(new SoundFilter(FilterType.CATEGORY, "Init"));
    
    List<Sound> result = service.filter(testSounds, "", filters);
    
    assertEquals(1, result.size());
    assertEquals("Bass 1", result.get(0).getName());
  }

  // Note: Collection and Tag filtering requires database setup
  // These are tested in integration tests

  @Test
  void testFilterMultipleFiltersAnd() {
    List<SoundFilter> filters = List.of(
        new SoundFilter(FilterType.BANK, "A"),
        new SoundFilter(FilterType.CATEGORY, "Init")
    );
    
    List<Sound> result = service.filter(testSounds, "", filters);
    
    assertEquals(1, result.size());
    assertEquals("Bass 1", result.get(0).getName());
  }

  @Test
  void testFilterTextAndFilters() {
    List<SoundFilter> filters = List.of(new SoundFilter(FilterType.BANK, "A"));
    
    List<Sound> result = service.filter(testSounds, "lead", filters);
    
    assertEquals(1, result.size());
    assertEquals("Lead 1", result.get(0).getName());
  }

  @Test
  void testFilterNoMatch() {
    List<SoundFilter> filters = List.of(new SoundFilter(FilterType.BANK, "C"));
    
    List<Sound> result = service.filter(testSounds, "", filters);
    
    assertEquals(0, result.size());
  }

  @Test
  void testFilterEmptyInput() {
    List<Sound> result = service.filter(List.of(), "", List.of());
    
    assertEquals(0, result.size());
  }

  @Test
  void testFilterNullBank() {
    Sound sound = Sound.builder().name("No Bank").bank(null).program(0).build();
    List<Sound> sounds = List.of(sound);
    List<SoundFilter> filters = List.of(new SoundFilter(FilterType.BANK, "A"));
    
    List<Sound> result = service.filter(sounds, "", filters);
    
    assertEquals(0, result.size());
  }

  private List<Sound> createTestSounds() {
    List<Sound> sounds = new ArrayList<>();

    // Bass 1 - Bank A, Category Bass
    Sound bass = Sound.builder()
        .name("Bass 1")
        .bank(0)
        .program(0)
        .category(0)
        .build();
    sounds.add(bass);

    // Lead 1 - Bank A, Category Lead
    Sound lead = Sound.builder()
        .name("Lead 1")
        .bank(0)
        .program(1)
        .category(1)
        .build();
    sounds.add(lead);

    // Pad 1 - Bank B, Category Pad
    Sound pad = Sound.builder()
        .name("Pad 1")
        .bank(1)
        .program(0)
        .category(2)
        .build();
    sounds.add(pad);

    return sounds;
  }
}

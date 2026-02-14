package net.mikolas.lyra.service;

import net.mikolas.lyra.ui.FilterType;
import net.mikolas.lyra.ui.SoundFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TreeNavigationServiceTest {

  private TreeNavigationService service;

  @BeforeEach
  void setUp() {
    service = new TreeNavigationService();
  }

  @Test
  void testParseAllSounds() {
    Optional<SoundFilter> filter = service.parseTreeSelection("• All Sounds", null);
    
    assertTrue(filter.isEmpty());
  }

  @Test
  void testParseBankSelection() {
    Optional<SoundFilter> filter = service.parseTreeSelection("Bank A", null);
    
    assertTrue(filter.isPresent());
    assertEquals(FilterType.BANK, filter.get().type());
    assertEquals("A", filter.get().value());
  }

  @Test
  void testParseBankH() {
    Optional<SoundFilter> filter = service.parseTreeSelection("Bank H", null);
    
    assertTrue(filter.isPresent());
    assertEquals(FilterType.BANK, filter.get().type());
    assertEquals("H", filter.get().value());
  }

  @Test
  void testParseCategorySelection() {
    Optional<SoundFilter> filter = service.parseTreeSelection("• Bass", "🎵 Categories");
    
    assertTrue(filter.isPresent());
    assertEquals(FilterType.CATEGORY, filter.get().type());
    assertEquals("Bass", filter.get().value());
  }

  @Test
  void testParseCollectionSelection() {
    Optional<SoundFilter> filter = service.parseTreeSelection("• Favorites", "📚 Collections");
    
    assertTrue(filter.isPresent());
    assertEquals(FilterType.COLLECTION, filter.get().type());
    assertEquals("Favorites", filter.get().value());
  }

  @Test
  void testParseTagSelection() {
    Optional<SoundFilter> filter = service.parseTreeSelection("• Analog", "🏷️ Tags");
    
    assertTrue(filter.isPresent());
    assertEquals(FilterType.TAG, filter.get().type());
    assertEquals("Analog", filter.get().value());
  }

  @Test
  void testParseUnknownSelection() {
    Optional<SoundFilter> filter = service.parseTreeSelection("Unknown", null);
    
    assertTrue(filter.isEmpty());
  }

  @Test
  void testParseWithoutBullet() {
    Optional<SoundFilter> filter = service.parseTreeSelection("Bass", "🎵 Categories");
    
    assertTrue(filter.isEmpty());
  }
}

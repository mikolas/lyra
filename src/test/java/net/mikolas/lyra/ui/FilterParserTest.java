package net.mikolas.lyra.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FilterParserTest {

  @Test
  void testParseTagPrefix() {
    Optional<SoundFilter> filter = FilterParser.parsePrefixFilter("tag:Analog");
    assertTrue(filter.isPresent());
    assertEquals(FilterType.TAG, filter.get().type());
    assertEquals("Analog", filter.get().value());
  }

  @Test
  void testParseCategoryPrefix() {
    Optional<SoundFilter> filter = FilterParser.parsePrefixFilter("cat:Bass");
    assertTrue(filter.isPresent());
    assertEquals(FilterType.CATEGORY, filter.get().type());
    assertEquals("Bass", filter.get().value());

    filter = FilterParser.parsePrefixFilter("category:Pad");
    assertTrue(filter.isPresent());
    assertEquals(FilterType.CATEGORY, filter.get().type());
    assertEquals("Pad", filter.get().value());
  }

  @Test
  void testParseBankPrefix() {
    Optional<SoundFilter> filter = FilterParser.parsePrefixFilter("bank:A");
    assertTrue(filter.isPresent());
    assertEquals(FilterType.BANK, filter.get().type());
    assertEquals("A", filter.get().value());

    filter = FilterParser.parsePrefixFilter("BANK:h");
    assertTrue(filter.isPresent());
    assertEquals("H", filter.get().value());
  }

  @Test
  void testInvalidPrefixes() {
    assertFalse(FilterParser.parsePrefixFilter("just text").isPresent());
    assertFalse(FilterParser.parsePrefixFilter("bank:Z").isPresent()); // Invalid bank
    assertFalse(FilterParser.parsePrefixFilter("").isPresent());
    assertFalse(FilterParser.parsePrefixFilter(null).isPresent());
  }

  @Test
  void testWhitespaceHandling() {
    Optional<SoundFilter> filter = FilterParser.parsePrefixFilter("  tag:  Warm  ");
    assertTrue(filter.isPresent());
    assertEquals("Warm", filter.get().value());
  }
}

package net.mikolas.lyra.ui;

import java.util.Optional;

/**
 * Utility for parsing tokenized search prefixes.
 */
public class FilterParser {

  /**
   * Parse a text string into a SoundFilter if it matches a known prefix.
   *
   * @param text input text (e.g. "tag:Analog")
   * @return Optional SoundFilter if parsed successfully
   */
  public static Optional<SoundFilter> parsePrefixFilter(String text) {
    if (text == null) return Optional.empty();
    String trimmed = text.trim();
    String lower = trimmed.toLowerCase();

    int colonIdx = lower.indexOf(":");
    if (colonIdx == -1) return Optional.empty();

    String prefix = lower.substring(0, colonIdx).trim();
    String value = trimmed.substring(colonIdx + 1).trim();

    if (prefix.equals("tag")) {
      return Optional.of(new SoundFilter(FilterType.TAG, value));
    } 
    
    if (prefix.equals("cat") || prefix.equals("category")) {
      return Optional.of(new SoundFilter(FilterType.CATEGORY, value));
    } 
    
    if (prefix.equals("bank")) {
      String bank = value.toUpperCase();
      if (bank.length() == 1 && bank.charAt(0) >= 'A' && bank.charAt(0) <= 'H') {
        return Optional.of(new SoundFilter(FilterType.BANK, bank));
      }
    }
    
    return Optional.empty();
  }
}

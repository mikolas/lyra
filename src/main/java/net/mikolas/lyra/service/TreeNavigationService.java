package net.mikolas.lyra.service;

import net.mikolas.lyra.ui.FilterType;
import net.mikolas.lyra.ui.SoundFilter;

import java.util.Optional;

/**
 * Service for parsing tree navigation selections into filters.
 */
public class TreeNavigationService {

  /**
   * Parse tree selection into a sound filter.
   *
   * @param value selected tree item value
   * @param parent parent tree item value (can be null)
   * @return filter if selection represents a filter, empty otherwise
   */
  public Optional<SoundFilter> parseTreeSelection(String value, String parent) {
    // "All Sounds" clears filters
    if (value.equals("• All Sounds")) {
      return Optional.empty();
    }

    // Bank selection: "Bank A", "Bank B", etc.
    if (value.startsWith("Bank ")) {
      String bank = value.substring(5);
      return Optional.of(new SoundFilter(FilterType.BANK, bank));
    }

    // Child items start with "• "
    if (value.startsWith("• ") && parent != null) {
      String filterValue = value.substring(2);
      
      if (parent.contains("Categories")) {
        return Optional.of(new SoundFilter(FilterType.CATEGORY, filterValue));
      } else if (parent.contains("Collections")) {
        return Optional.of(new SoundFilter(FilterType.COLLECTION, filterValue));
      } else if (parent.contains("Tags")) {
        return Optional.of(new SoundFilter(FilterType.TAG, filterValue));
      }
    }

    return Optional.empty();
  }
}

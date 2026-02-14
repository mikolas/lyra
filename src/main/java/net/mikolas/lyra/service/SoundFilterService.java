package net.mikolas.lyra.service;

import net.mikolas.lyra.midi.SysExParser;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundCollection;
import net.mikolas.lyra.model.SoundTag;
import net.mikolas.lyra.ui.FilterType;
import net.mikolas.lyra.ui.SoundFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for filtering sounds by text search and filter criteria.
 */
public class SoundFilterService {

  /**
   * Filter sounds by search text and filter criteria.
   *
   * @param sounds all sounds to filter
   * @param searchText text search (case-insensitive, matches name)
   * @param filters list of filter criteria (AND logic)
   * @return filtered list of sounds
   */
  public List<Sound> filter(List<Sound> sounds, String searchText, List<SoundFilter> filters) {
    return sounds.stream()
        .filter(sound -> matchesSearchText(sound, searchText))
        .filter(sound -> matchesAllFilters(sound, filters))
        .collect(Collectors.toList());
  }

  private boolean matchesSearchText(Sound sound, String searchText) {
    if (searchText == null || searchText.isEmpty()) {
      return true;
    }
    String lowerSearch = searchText.toLowerCase();
    return sound.getName() != null && sound.getName().toLowerCase().contains(lowerSearch);
  }

  private boolean matchesAllFilters(Sound sound, List<SoundFilter> filters) {
    for (SoundFilter filter : filters) {
      if (!matchesFilter(sound, filter)) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesFilter(Sound sound, SoundFilter filter) {
    return switch (filter.type()) {
      case BANK -> matchesBankFilter(sound, filter.value());
      case CATEGORY -> matchesCategoryFilter(sound, filter.value());
      case COLLECTION -> matchesCollectionFilter(sound, filter.value());
      case TAG -> matchesTagFilter(sound, filter.value());
      case TEXT -> matchesSearchText(sound, filter.value());
    };
  }

  private boolean matchesBankFilter(Sound sound, String bankValue) {
    Integer bank = sound.getBank();
    if (bank == null) {
      return false;
    }
    String bankLetter = String.valueOf((char) ('A' + bank));
    return bankLetter.equals(bankValue);
  }

  private boolean matchesCategoryFilter(Sound sound, String categoryValue) {
    String category = SysExParser.getCategoryName(sound.getCategory());
    return category != null && category.equals(categoryValue);
  }

  private boolean matchesCollectionFilter(Sound sound, String collectionValue) {
    if (sound.getSoundCollections() == null) {
      return false;
    }
    for (SoundCollection sc : sound.getSoundCollections()) {
      if (sc.getCollection() != null && collectionValue.equals(sc.getCollection().getName())) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesTagFilter(Sound sound, String tagValue) {
    if (sound.getSoundTags() == null) {
      return false;
    }
    for (SoundTag st : sound.getSoundTags()) {
      if (st.getTag() != null && tagValue.equals(st.getTag().getName())) {
        return true;
      }
    }
    return false;
  }
}

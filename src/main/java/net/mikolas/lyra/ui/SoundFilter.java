package net.mikolas.lyra.ui;

/**
 * Filter chip for tokenized search.
 *
 * @param type filter type
 * @param value filter value
 */
public record SoundFilter(FilterType type, String value) {

  public String getDisplayText() {
    return switch (type) {
      case BANK -> "📂 Bank: " + value;
      case CATEGORY -> "🎵 Cat: " + value;
      case COLLECTION -> "📚 Coll: " + value;
      case TAG -> "🏷️ Tag: " + value;
      case TEXT -> value;
    };
  }
}

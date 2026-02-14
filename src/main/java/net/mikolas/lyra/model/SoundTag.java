package net.mikolas.lyra.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Join table for Sound-Tag many-to-many relationship.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "sound_tags")
public class SoundTag {
  @DatabaseField(generatedId = true)
  private Integer id;

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, uniqueCombo = true)
  private Sound sound;

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, uniqueCombo = true)
  private Tag tag;
}
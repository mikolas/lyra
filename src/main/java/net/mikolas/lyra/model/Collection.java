package net.mikolas.lyra.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Collection model for organizing sounds hierarchically.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "collections")
public class Collection {
  @DatabaseField(generatedId = true)
  private Integer id;

  @DatabaseField(canBeNull = false, unique = true)
  private String name;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private Collection parent;
}
package net.mikolas.lyra.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag model for categorizing sounds with optional color.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "tags")
public class Tag {
  @DatabaseField(generatedId = true)
  private Integer id;

  @DatabaseField(canBeNull = false, unique = true)
  private String name;

  @DatabaseField
  private String color;
}
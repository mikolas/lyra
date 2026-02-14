package net.mikolas.lyra.db;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.sql.SQLException;

/**
 * Database manager using ORMLite with SQLite.
 *
 * <p>Manages the database connection lifecycle. Use try-with-resources for automatic cleanup.
 */
public class DatabaseManager implements AutoCloseable {
  private final ConnectionSource connectionSource;

  public DatabaseManager() throws SQLException {
    this(getDefaultDatabasePath());
  }

  public DatabaseManager(String dbPath) throws SQLException {
    String jdbcUrl = "jdbc:sqlite:" + dbPath + "?busy_timeout=30000";
    connectionSource = new JdbcConnectionSource(jdbcUrl);
  }

  /**
   * Get the default database path in the user's home directory.
   * Creates the directory if it doesn't exist.
   *
   * @return Absolute path to the database file
   */
  public static String getDefaultDatabasePath() {
    String userHome = System.getProperty("user.home");
    File lyraDir = new File(userHome, ".lyra");
    
    if (!lyraDir.exists()) {
      lyraDir.mkdirs();
    }
    
    return new File(lyraDir, "lyra.db").getAbsolutePath();
  }

  public ConnectionSource getConnectionSource() {
    return connectionSource;
  }

  @Override
  public void close() throws Exception {
    if (connectionSource != null) {
      connectionSource.close();
    }
  }
}

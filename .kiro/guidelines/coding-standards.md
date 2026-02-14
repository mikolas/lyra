# Lyra: Coding Standards

## Overview

This document defines coding standards for Lyra using Java 25, JavaFX 23, Maven, Lombok, and ORMLite.

---

## Java Coding Standards

### Style Guide

Follow Google Java Style Guide with these specifics:
- **Line length**: 100 characters
- **Indentation**: 2 spaces
- **Braces**: K&R style (opening brace on same line)
- **Imports**: Grouped and sorted (java.*, javax.*, third-party, net.mikolas.lyra.*)

### Lombok Usage

Use Lombok to eliminate boilerplate code:

```java
// Domain/Database models - mutable with Lombok
@Data
@Builder
@DatabaseTable(tableName = "sounds")
public class Sound {
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    private String name;
    
    @DatabaseField
    private String category;
    
    @DatabaseField
    private Integer bank;
    
    @DatabaseField
    private Integer program;
    
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] parameters;
    
    // JavaFX Properties for UI binding (transient, not persisted)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient StringProperty nameProperty;
    
    public StringProperty nameProperty() {
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty(name);
            nameProperty.addListener((obs, old, newVal) -> name = newVal);
        }
        return nameProperty;
    }
}
```

**Lombok Annotations:**
- `@Data` - Generates getters, setters, equals, hashCode, toString
- `@Builder` - Fluent builder pattern
- `@Getter/@Setter` - Individual getter/setter control
- `@NoArgsConstructor/@AllArgsConstructor` - Constructor generation
- `@Slf4j` - Logger field generation

**When to use:**
- Domain models (database entities)
- DTOs (if needed, though avoid)
- Configuration classes
- Test fixtures

**When NOT to use:**
- JavaFX Controllers (use explicit code)
- Complex business logic classes
- Classes with custom initialization

### Modern Java 25 Features

Use Java 25 features throughout:

```java
// Records for immutable message types
public sealed interface MidiMessage 
    permits SoundParameterChange, SoundDumpRequest, SoundDumpData {}

public record SoundParameterChange(int deviceId, int location, int paramId, int value)
    implements MidiMessage {
    public SoundParameterChange {
        if (paramId < 0 || paramId > 384) {
            throw new IllegalArgumentException("Parameter ID must be 0-384");
        }
    }
}

// Pattern matching in switch
var category = switch (paramId / 100) {
    case 0 -> "Oscillator";
    case 1 -> "Filter";
    default -> "Other";
};

// Pattern matching with instanceof
if (message instanceof SoundParameterChange(int id, int value)) {
    sound.setParameter(id, value);
}

// Text blocks
String sql = """
    SELECT * FROM sounds
    WHERE name LIKE ?
    ORDER BY name
    """;

// Unnamed variables
property.addListener((_, _, newValue) -> 
    midiService.sendParameter(paramId, newValue.intValue())
);

// Virtual threads
var executor = Executors.newVirtualThreadPerTaskExecutor();

// Sequenced collections
var first = deque.removeFirst();
deque.addLast(first);

// Math.clamp()
value.set(Math.clamp(val, minValue.get(), maxValue.get()));
```

### Javadoc

Use Javadoc for all public APIs:

```java
/**
 * Sets a sound parameter value.
 * 
 * @param paramId parameter ID (0-384)
 * @param value parameter value (0-127)
 * @throws IllegalArgumentException if paramId or value out of range
 */
public void setParameter(int paramId, int value) {
    if (paramId < 0 || paramId > 384) {
        throw new IllegalArgumentException("Invalid parameter ID: " + paramId);
    }
    parameters[paramId] = (byte) value;
}
```

---

## Code Organization

### Package Structure

```
net.mikolas.lyra/
├── model/          # Domain models (Lombok @Data classes)
├── db/             # ORMLite DAOs and database management
├── midi/           # MIDI protocol (records and services)
├── ui/             # JavaFX controllers and custom controls
├── service/        # Business logic services
└── exception/      # Custom exceptions
```

### Class Design

**Domain Models (Mutable with Lombok):**
```java
@Data
@Builder
@DatabaseTable(tableName = "sounds")
public class Sound {
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    private String name;
    
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] parameters;
    
    // JavaFX property for UI binding
    private transient StringProperty nameProperty;
    
    public StringProperty nameProperty() {
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty(name);
            nameProperty.addListener((_, _, newVal) -> name = newVal);
        }
        return nameProperty;
    }
}
```

**MIDI Messages (Immutable Records):**
```java
public record SoundParameterChange(int deviceId, int location, int paramId, int value)
    implements MidiMessage {
    public SoundParameterChange {
        if (paramId < 0 || paramId > 384) {
            throw new IllegalArgumentException("Parameter ID must be 0-384");
        }
    }
}
```

**Services:**
```java
public class MidiService {
    private final BlofeldProtocol protocol;
    private MidiDevice.Info deviceInfo;
    
    public MidiService(BlofeldProtocol protocol) {
        this.protocol = protocol;
    }
    
    public void sendParameter(int paramId, int value) throws MidiException {
        var msg = new SoundParameterChange(0x7F, 0x00, paramId, value);
        byte[] sysex = protocol.encode(msg);
        // Send via javax.sound.midi
    }
}
```

---

## Database Layer (ORMLite)

### DAO Pattern

```java
public class SoundDao {
    private final Dao<Sound, Integer> dao;
    
    public SoundDao(ConnectionSource connectionSource) throws SQLException {
        dao = DaoManager.createDao(connectionSource, Sound.class);
        TableUtils.createTableIfNotExists(connectionSource, Sound.class);
    }
    
    public Sound save(Sound sound) throws SQLException {
        dao.createOrUpdate(sound);
        return sound;
    }
    
    public Optional<Sound> findById(int id) throws SQLException {
        return Optional.ofNullable(dao.queryForId(id));
    }
    
    public List<Sound> findByName(String pattern) throws SQLException {
        return dao.queryBuilder()
            .where()
            .like("name", "%" + pattern + "%")
            .query();
    }
    
    public void delete(int id) throws SQLException {
        dao.deleteById(id);
    }
}
```

### Database Manager

```java
public class DatabaseManager implements AutoCloseable {
    private final ConnectionSource connectionSource;
    
    public DatabaseManager(String dbPath) throws SQLException {
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        connectionSource = new JdbcConnectionSource(jdbcUrl);
    }
    
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }
    
    @Override
    public void close() throws Exception {
        connectionSource.close();
    }
}
```

---

## JavaFX-Specific Standards

### Properties and Bindings

```java
@Data
@DatabaseTable(tableName = "sounds")
public class Sound {
    @DatabaseField
    private String name;
    
    @DatabaseField
    private int cutoff;
    
    // Lazy-initialized JavaFX properties
    private transient StringProperty nameProperty;
    private transient IntegerProperty cutoffProperty;
    
    public StringProperty nameProperty() {
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty(name);
            nameProperty.addListener((_, _, newVal) -> name = newVal);
        }
        return nameProperty;
    }
    
    public IntegerProperty cutoffProperty() {
        if (cutoffProperty == null) {
            cutoffProperty = new SimpleIntegerProperty(cutoff);
            cutoffProperty.addListener((_, _, newVal) -> cutoff = newVal.intValue());
        }
        return cutoffProperty;
    }
}

// In controller
public class EditorController {
    @FXML private TextField nameField;
    @FXML private Slider cutoffSlider;
    private Sound sound;
    
    public void initialize() {
        nameField.textProperty().bindBidirectional(sound.nameProperty());
        cutoffSlider.valueProperty().bindBidirectional(sound.cutoffProperty());
    }
}
```

### Custom Controls

```java
public class Dial extends Control {
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private final IntegerProperty minValue = new SimpleIntegerProperty(0);
    private final IntegerProperty maxValue = new SimpleIntegerProperty(127);
    
    public Dial() {
        getStyleClass().add("dial");
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new DialSkin(this);
    }
    
    public IntegerProperty valueProperty() { return value; }
    public int getValue() { return value.get(); }
    public void setValue(int val) { value.set(val); }
}
```

---

## Error Handling

### Custom Exceptions

```java
public class LyraException extends Exception {
    public LyraException(String message) {
        super(message);
    }
    
    public LyraException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class MidiException extends LyraException {
    public MidiException(String message) {
        super(message);
    }
    
    public MidiException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class DatabaseException extends LyraException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Exception Handling

```java
public Sound loadSound(int soundId) throws DatabaseException {
    try {
        return soundDao.findById(soundId)
            .orElseThrow(() -> new DatabaseException("Sound not found: " + soundId, null));
    } catch (SQLException e) {
        throw new DatabaseException("Failed to load sound", e);
    }
}
```

---

## Testing Standards (TDD)

### Test-Driven Development Workflow

1. **RED**: Write failing test first
2. **GREEN**: Implement minimal code to pass
3. **REFACTOR**: Clean up code while keeping tests green

### Test Structure

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

class SoundDaoTest {
    private DatabaseManager dbManager;
    private SoundDao soundDao;
    
    @BeforeEach
    void setUp() throws Exception {
        dbManager = new DatabaseManager(":memory:");
        soundDao = new SoundDao(dbManager.getConnectionSource());
    }
    
    @AfterEach
    void tearDown() throws Exception {
        dbManager.close();
    }
    
    @Test
    void testSaveSound() throws Exception {
        Sound sound = Sound.builder()
            .name("Test Sound")
            .parameters(new byte[385])
            .build();
        
        Sound saved = soundDao.save(sound);
        
        assertNotNull(saved.getId());
        assertEquals("Test Sound", saved.getName());
    }
    
    @Test
    void testFindByName() throws Exception {
        soundDao.save(Sound.builder().name("Bass").parameters(new byte[385]).build());
        soundDao.save(Sound.builder().name("Lead").parameters(new byte[385]).build());
        
        List<Sound> results = soundDao.findByName("Bass");
        
        assertEquals(1, results.size());
        assertEquals("Bass", results.get(0).getName());
    }
}
```

---

## Code Formatting

### Maven Configuration

```xml
<plugin>
    <groupId>com.spotify.fmt</groupId>
    <artifactId>fmt-maven-plugin</artifactId>
    <version>2.21.1</version>
    <executions>
        <execution>
            <goals>
                <goal>format</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Format Commands

```bash
# Format all Java files
mvn fmt:format

# Check formatting
mvn fmt:check
```

---

## Git Commit Messages

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Tests
- `chore`: Maintenance

### Examples

```
feat(midi): add SysEx protocol handler

Implement encoding/decoding for Blofeld SysEx messages including
SNDP, SNDD, and SNDR message types with checksum validation.

Closes #42
```

---

**Document Version**: 5.0  
**Last Updated**: 2026-02-05  
**Status**: 🟢 Updated for Java 25/JavaFX/Maven/Lombok/ORMLite  
**Next Review**: After database refactor

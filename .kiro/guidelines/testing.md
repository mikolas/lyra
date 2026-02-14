# Lyra: Testing Strategy

## Overview

Comprehensive testing strategy using JUnit 5, TestFX for UI testing, and TDD methodology (RED → GREEN → REFACTOR).

---

## Testing Pyramid

```
         /\
        /  \  E2E Tests (Few)
       /────\
      /      \  Integration Tests (Some)
     /────────\
    /          \  Unit Tests (Many)
   /────────────\
```

**Target Coverage**: >80% overall, 100% for critical paths

---

## Test Structure

```
src/test/java/net/mikolas/lyra/
├── model/                  # Model tests
│   ├── SoundTest.java
│   ├── CollectionTest.java
│   └── TagTest.java
├── db/                     # Database tests
│   ├── SoundDatabaseTest.java
│   ├── CollectionDatabaseTest.java
│   └── TagDatabaseTest.java
├── midi/                   # MIDI tests
│   ├── BlofeldProtocolTest.java
│   ├── MidiServiceTest.java
│   └── CCMapperTest.java
├── ui/                     # UI tests (TestFX)
│   ├── widgets/
│   │   ├── DialTest.java
│   │   └── EnvelopeEditorTest.java
│   └── views/
│       ├── LibrarianViewTest.java
│       └── EditorViewTest.java
└── integration/            # Integration tests
    ├── MidiFlowTest.java
    └── DatabaseMidiTest.java
```

---

## TDD Workflow

### RED → GREEN → REFACTOR

1. **RED**: Write failing test first
2. **GREEN**: Write minimal code to pass
3. **REFACTOR**: Clean up while keeping tests green

```java
// 1. RED: Write test first
@Test
void shouldCreateSoundWithDefaults() {
    Sound sound = Sound.builder().build();
    assertNull(sound.getName());
    assertNull(sound.getParameters());
}

// 2. GREEN: Implement minimal code
@Data
@Builder
public class Sound {
    private String name;
    private byte[] parameters;
}

// 3. REFACTOR: Add validation, defaults, etc.
@Data
@Builder
public class Sound {
    private String name;
    
    @Builder.Default
    private byte[] parameters = new byte[385];
    
    // Validation in builder
    public static class SoundBuilder {
        public Sound build() {
            if (parameters != null && parameters.length != 385) {
                throw new IllegalArgumentException("Parameters must be 385 bytes");
            }
            return new Sound(name, parameters);
        }
    }
}
```

---

## Unit Tests

### Testing Data Models

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SoundTest {
    @Test
    void shouldCreateSoundWithBuilder() {
        byte[] params = new byte[385];
        Sound sound = Sound.builder()
            .name("Test Sound")
            .parameters(params)
            .build();
        
        assertEquals("Test Sound", sound.getName());
        assertArrayEquals(params, sound.getParameters());
    }
    
    @Test
    void shouldValidateParameterLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            Sound.builder()
                .parameters(new byte[100])  // Wrong length
                .build();
        });
    }
    
    @Test
    void shouldSupportJavaFXProperties() {
        Sound sound = Sound.builder()
            .name("Original")
            .build();
        
        StringProperty nameProperty = sound.nameProperty();
        assertEquals("Original", nameProperty.get());
        
        // Bidirectional sync
        nameProperty.set("Updated");
        assertEquals("Updated", sound.getName());
        
        sound.setName("Direct");
        assertEquals("Direct", nameProperty.get());
    }
}
```

### Testing MIDI Protocol

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BlofeldProtocolTest {
    private final BlofeldProtocol protocol = new BlofeldProtocol(0);
    
    @Test
    void shouldEncodeSNDP() {
        SoundParameterChange msg = new SoundParameterChange(0, 72, 64);
        byte[] sysex = protocol.encode(msg);
        
        assertEquals(0xF0, sysex[0]);           // Start
        assertEquals(0x3E, sysex[1]);           // Waldorf
        assertEquals(0x13, sysex[2]);           // Blofeld
        assertEquals(0x00, sysex[3]);           // Device ID
        assertEquals(0x10, sysex[4]);           // SNDP
        assertEquals(0xF7, sysex[sysex.length - 1]);  // End
    }
    
    @Test
    void shouldDecodeAndEncode() {
        byte[] params = new byte[385];
        Arrays.fill(params, (byte) 0x42);
        
        SoundDumpData original = new SoundDumpData(0, 0, 0, params);
        byte[] encoded = protocol.encode(original);
        MidiMessage decoded = protocol.decode(encoded);
        
        assertInstanceOf(SoundDumpData.class, decoded);
        SoundDumpData result = (SoundDumpData) decoded;
        assertArrayEquals(params, result.parameters());
    }
    
    @Test
    void shouldValidateChecksum() {
        byte[] params = new byte[385];
        SoundDumpData msg = new SoundDumpData(0, 0, 0, params);
        byte[] sysex = protocol.encode(msg);
        
        // Corrupt checksum
        sysex[sysex.length - 2] = 0x00;
        
        assertThrows(MidiException.class, () -> {
            protocol.decode(sysex);
        });
    }
}
```

---

## Database Tests

### Testing with ORMLite

```java
import static org.junit.jupiter.api.Assertions.*;
import com.j256.ormlite.dao.Dao;
import org.junit.jupiter.api.*;

class SoundDatabaseTest {
    private Database db;
    private Dao<Sound, Integer> soundDao;
    
    @BeforeEach
    void setUp() throws Exception {
        db = new Database(":memory:");
        soundDao = db.sounds;
    }
    
    @AfterEach
    void tearDown() throws Exception {
        db.close();
    }
    
    @Test
    void shouldCreateSound() throws Exception {
        Sound sound = Sound.builder()
            .name("Test")
            .parameters(new byte[385])
            .build();
        
        soundDao.create(sound);
        assertNotNull(sound.getId());
    }
    
    @Test
    void shouldQueryByName() throws Exception {
        Sound sound1 = Sound.builder().name("Bass").parameters(new byte[385]).build();
        Sound sound2 = Sound.builder().name("Lead").parameters(new byte[385]).build();
        
        soundDao.create(sound1);
        soundDao.create(sound2);
        
        List<Sound> results = soundDao.queryBuilder()
            .where()
            .like("name", "%Bass%")
            .query();
        
        assertEquals(1, results.size());
        assertEquals("Bass", results.get(0).getName());
    }
    
    @Test
    void shouldEnforceUniqueBankProgram() throws Exception {
        Sound sound1 = Sound.builder()
            .bank(0)
            .program(0)
            .parameters(new byte[385])
            .build();
        
        Sound sound2 = Sound.builder()
            .bank(0)
            .program(0)
            .parameters(new byte[385])
            .build();
        
        soundDao.create(sound1);
        assertThrows(SQLException.class, () -> soundDao.create(sound2));
    }
}
```

---

## Integration Tests

### Testing MIDI Flow

```java
class MidiFlowTest {
    private Database db;
    private MidiService midiService;
    private BlofeldProtocol protocol;
    
    @BeforeEach
    void setUp() throws Exception {
        db = new Database(":memory:");
        protocol = new BlofeldProtocol(0);
        midiService = new MidiService(protocol);
    }
    
    @Test
    void shouldSendAndReceiveParameterChange() throws Exception {
        // Setup mock MIDI device
        MockMidiDevice device = new MockMidiDevice();
        midiService.connect(device);
        
        // Send parameter change
        SoundParameterChange msg = new SoundParameterChange(0, 72, 64);
        midiService.send(msg);
        
        // Verify sent
        byte[] sent = device.getLastSent();
        assertNotNull(sent);
        assertEquals(0xF0, sent[0]);
    }
    
    @Test
    void shouldLoadSoundFromDatabase() throws Exception {
        // Create sound in database
        Sound sound = Sound.builder()
            .name("Test")
            .bank(0)
            .program(0)
            .parameters(new byte[385])
            .build();
        db.sounds.create(sound);
        
        // Load and verify
        Sound loaded = db.sounds.queryForId(sound.getId());
        assertEquals("Test", loaded.getName());
        assertArrayEquals(sound.getParameters(), loaded.getParameters());
    }
}
```

---

## UI Tests (TestFX)

### Testing JavaFX Widgets

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;
import javafx.scene.Scene;

@ExtendWith(ApplicationExtension.class)
class DialTest {
    private Dial dial;
    
    @Start
    void start(Stage stage) {
        dial = new Dial();
        stage.setScene(new Scene(dial, 100, 100));
        stage.show();
    }
    
    @Test
    void shouldCreateDialWithDefaults(FxRobot robot) {
        assertEquals(0, dial.getValue());
        assertEquals(0, dial.getMin());
        assertEquals(127, dial.getMax());
    }
    
    @Test
    void shouldUpdateValueOnDrag(FxRobot robot) {
        robot.drag(dial).moveBy(0, -50);
        assertTrue(dial.getValue() > 0);
    }
    
    @Test
    void shouldFireValueChangeEvent(FxRobot robot) {
        boolean[] fired = {false};
        dial.valueProperty().addListener((obs, old, val) -> fired[0] = true);
        
        dial.setValue(64);
        assertTrue(fired[0]);
    }
}
```

### Testing Views

```java
@ExtendWith(ApplicationExtension.class)
class LibrarianViewTest {
    private LibrarianView view;
    private Database db;
    
    @Start
    void start(Stage stage) throws Exception {
        db = new Database(":memory:");
        view = new LibrarianView(db);
        stage.setScene(new Scene(view, 800, 600));
        stage.show();
    }
    
    @Test
    void shouldDisplaySounds(FxRobot robot) throws Exception {
        // Create test sounds
        Sound sound = Sound.builder()
            .name("Test Sound")
            .parameters(new byte[385])
            .build();
        db.sounds.create(sound);
        
        view.refresh();
        
        // Verify sound appears in list
        robot.lookup("Test Sound").query();
    }
    
    @Test
    void shouldFilterBySearch(FxRobot robot) throws Exception {
        db.sounds.create(Sound.builder().name("Bass").parameters(new byte[385]).build());
        db.sounds.create(Sound.builder().name("Lead").parameters(new byte[385]).build());
        
        view.refresh();
        robot.clickOn("#searchField").write("Bass");
        
        // Should show only Bass
        assertNotNull(robot.lookup("Bass").tryQuery().orElse(null));
        assertNull(robot.lookup("Lead").tryQuery().orElse(null));
    }
}
```

---

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SoundDatabaseTest

# Run specific test method
mvn test -Dtest=SoundDatabaseTest#shouldCreateSound

# Run with coverage (requires jacoco plugin)
mvn test jacoco:report

# Run only unit tests
mvn test -Dgroups=unit

# Run only integration tests
mvn test -Dgroups=integration

# Skip tests during build
mvn package -DskipTests
```

---

## Test Configuration

### Maven Dependencies

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>
    
    <!-- TestFX for JavaFX testing -->
    <dependency>
        <groupId>org.testfx</groupId>
        <artifactId>testfx-junit5</artifactId>
        <version>4.0.18</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito for mocking -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.8.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Maven Surefire Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.3</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
        </includes>
        <groups>unit,integration</groups>
    </configuration>
</plugin>
```

---

## Continuous Integration

### GitHub Actions

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '25'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage
        uses: codecov/codecov-action@v4
```

---

## Current Test Status

### Implemented Tests

✅ **MIDI Protocol** (19 tests)
- SoundParameterChange encoding/decoding
- SoundDumpRequest encoding/decoding
- SoundDumpData encoding/decoding (394 bytes)
- MultiDumpRequest/Data encoding/decoding
- GlobalParametersRequest/Data encoding/decoding
- WavetableDump encoding/decoding
- DeviceIdentityRequest encoding/decoding
- Checksum validation
- 7-bit encoding/decoding

✅ **Database Layer** (12 tests)
- Sound CRUD operations
- Query by name (LIKE)
- Unique bank+program constraint
- Parameter validation
- In-memory database testing

### Pending Tests

⏳ **Collection/Tag Database Tests**
- Collection CRUD operations
- Hierarchical collections
- Sound-collection associations
- Sound-tag associations

⏳ **MidiService Tests**
- Device enumeration
- Connection management
- Bidirectional I/O
- Receiver callbacks

⏳ **UI Tests**
- Dial widget
- EnvelopeEditor widget
- Librarian view
- Editor view

---

**Document Version**: 3.0  
**Last Updated**: 2026-02-05  
**Status**: 🟢 Updated for Java 25/Maven/JUnit 5  
**Next Review**: After UI implementation

# Lyra: Development Workflow

## Overview

Development workflow for Lyra using Java 25, Maven, JavaFX 23, Lombok, and ORMLite.

---

## Environment Setup

### Prerequisites

```bash
# Java 25
java --version  # Should show Java 25

# Maven 3.9+
mvn --version

# Git
git --version
```

### Initial Setup

```bash
# Clone repository
git clone https://github.com/username/lyra.git
cd lyra

# Build project
mvn clean compile

# Run tests
mvn test

# Run application
mvn javafx:run
```

### Project Structure

```
lyra/
├── pom.xml                          # Maven configuration
├── README.md
├── .gitignore
├── src/main/java/net/mikolas/lyra/
│   ├── Main.java                    # Application entry point
│   ├── model/                       # Domain models (Lombok @Data)
│   │   ├── Sound.java
│   │   ├── Collection.java
│   │   └── Tag.java
│   ├── db/                          # ORMLite DAOs
│   │   ├── DatabaseManager.java
│   │   ├── SoundDao.java
│   │   ├── CollectionDao.java
│   │   └── TagDao.java
│   ├── midi/                        # MIDI protocol
│   │   ├── MidiMessage.java         # Sealed interface
│   │   ├── BlofeldProtocol.java     # Encoder/decoder
│   │   └── MidiService.java         # javax.sound.midi wrapper
│   ├── ui/                          # JavaFX controllers
│   │   ├── EditorController.java
│   │   └── LibrarianController.java
│   ├── service/                     # Business logic
│   └── exception/                   # Custom exceptions
├── src/main/resources/
│   ├── fxml/                        # FXML layouts
│   ├── css/                         # Stylesheets
│   └── images/                      # Icons, graphics
├── src/test/java/net/mikolas/lyra/
│   ├── model/
│   ├── db/
│   └── midi/
└── .kiro/                           # Project documentation
    ├── specs/
    ├── implementation/
    └── guidelines/
```

---

## Git Workflow

### Branch Strategy

```
main
  └── develop
      ├── feature/midi-service
      ├── feature/sound-editor-ui
      └── bugfix/parameter-validation
```

**Branches**:
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical production fixes

### Creating Feature Branch

```bash
# Update develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/midi-service

# Work on feature
# ... make changes ...

# Commit changes
git add .
git commit -m "feat(midi): implement MidiService with bidirectional I/O"

# Push to remote
git push origin feature/midi-service

# Create pull request on GitHub
```

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Tests
- `chore`: Maintenance

**Examples**:

```
feat(midi): add SysEx protocol encoder/decoder

Implement encoding/decoding for all Blofeld SysEx messages including
SNDP, SNDR, SNDD, MULR, MULD, GLBR, GLBD, WTBD with checksum validation.
Follows TDD with 19 comprehensive tests.

Closes #42
```

```
fix(db): correct Sound parameter validation

The parameter array length check was off by one. Fixed to validate
exactly 385 parameters as per Blofeld specification.

Fixes #58
```

---

## Development Cycle (TDD)

### 1. RED - Write Failing Test

```bash
# Create test file
vim src/test/java/net/mikolas/lyra/db/SoundDaoTest.java
```

```java
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
```

```bash
# Run test (should FAIL)
mvn test -Dtest=SoundDaoTest#testSaveSound
```

### 2. GREEN - Implement Code

```bash
# Implement minimal code to pass test
vim src/main/java/net/mikolas/lyra/db/SoundDao.java
```

```java
public Sound save(Sound sound) throws SQLException {
    dao.createOrUpdate(sound);
    return sound;
}
```

```bash
# Run test (should PASS)
mvn test -Dtest=SoundDaoTest#testSaveSound
```

### 3. REFACTOR - Clean Up

```bash
# Refactor while keeping tests green
# Add error handling, improve naming, extract methods
vim src/main/java/net/mikolas/lyra/db/SoundDao.java

# Run all tests to ensure nothing broke
mvn test
```

### 4. Commit

```bash
git add .
git commit -m "feat(db): implement SoundDao save operation

Added save method with ORMLite createOrUpdate.
Includes comprehensive test coverage.

Closes #45"
```

---

## Testing Workflow

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SoundDaoTest

# Run specific test method
mvn test -Dtest=SoundDaoTest#testSaveSound

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Categories

```java
// Unit tests
@Test
void testSaveSound() { ... }

// Integration tests
@Test
@Tag("integration")
void testDatabaseIntegration() { ... }

// UI tests (TestFX)
@Test
void testDialInteraction() { ... }
```

### Before Committing

```bash
# Run all tests
mvn test

# Format code
mvn fmt:format

# Check for issues
mvn compile
```

---

## Build Commands

### Compile

```bash
# Clean and compile
mvn clean compile

# Compile only (incremental)
mvn compile
```

### Test

```bash
# Run all tests
mvn test

# Skip tests
mvn compile -DskipTests

# Run specific test
mvn test -Dtest=BlofeldProtocolTest
```

### Run Application

```bash
# Run with Maven
mvn javafx:run

# Run with exec plugin
mvn exec:java -Dexec.mainClass="net.mikolas.lyra.Main"
```

### Package

```bash
# Create JAR
mvn package

# Create JAR without tests
mvn package -DskipTests

# Run packaged JAR
java -jar target/lyra-1.0-SNAPSHOT.jar
```

### Format Code

```bash
# Format all Java files
mvn fmt:format

# Check formatting
mvn fmt:check
```

---

## Database Development

### ORMLite Workflow

1. **Define Model** (Lombok @Data + ORMLite annotations):

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
}
```

2. **Create DAO**:

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
}
```

3. **Write Tests** (TDD):

```java
@Test
void testSaveSound() throws Exception {
    Sound sound = Sound.builder()
        .name("Test")
        .parameters(new byte[385])
        .build();
    
    Sound saved = soundDao.save(sound);
    
    assertNotNull(saved.getId());
}
```

4. **Run Tests**:

```bash
mvn test -Dtest=SoundDaoTest
```

---

## MIDI Development

### Protocol Implementation

1. **Define Message Type** (Record):

```java
public record SoundParameterChange(int deviceId, int location, int paramId, int value)
    implements MidiMessage {
    public SoundParameterChange {
        if (paramId < 0 || paramId > 384) {
            throw new IllegalArgumentException("Invalid parameter ID");
        }
    }
}
```

2. **Write Encoding Test**:

```java
@Test
void testEncodeSoundParameterChange() throws MidiException {
    var msg = new SoundParameterChange(0x7F, 0x00, 1, 62);
    byte[] expected = {
        (byte) 0xF0, 0x3E, 0x13, 0x7F, 0x20, 0x00, 0x00, 0x01, 0x3E, (byte) 0xF7
    };
    
    byte[] actual = protocol.encode(msg);
    
    assertArrayEquals(expected, actual);
}
```

3. **Implement Encoder**:

```java
private byte[] encodeSoundParameterChange(SoundParameterChange msg) {
    int msb = msg.paramId() >> 7;
    int lsb = msg.paramId() & 0x7F;
    
    return new byte[] {
        SYSEX_START, WALDORF_ID, BLOFELD_ID,
        (byte) msg.deviceId(), CMD_SOUND_PARAMETER,
        (byte) msg.location(), (byte) msb, (byte) lsb,
        (byte) msg.value(), SYSEX_END
    };
}
```

4. **Run Tests**:

```bash
mvn test -Dtest=BlofeldProtocolTest
```

---

## JavaFX Development

### Controller Development

1. **Create FXML Layout**:

```xml
<!-- src/main/resources/fxml/editor.fxml -->
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox xmlns:fx="http://javafx.com/fxml" fx:controller="net.mikolas.lyra.ui.EditorController">
    <TextField fx:id="nameField"/>
    <Slider fx:id="cutoffSlider" min="0" max="127"/>
</VBox>
```

2. **Create Controller**:

```java
public class EditorController {
    @FXML private TextField nameField;
    @FXML private Slider cutoffSlider;
    
    private Sound sound;
    
    public void initialize() {
        nameField.textProperty().bindBidirectional(sound.nameProperty());
        cutoffSlider.valueProperty().bindBidirectional(sound.cutoffProperty());
    }
    
    public void setSound(Sound sound) {
        this.sound = sound;
    }
}
```

3. **Load in Application**:

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editor.fxml"));
Parent root = loader.load();
EditorController controller = loader.getController();
controller.setSound(currentSound);
```

---

## Troubleshooting

### Common Issues

**Lombok not working**:
```bash
# Enable annotation processing in IDE
# IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
# Eclipse: Install Lombok plugin
```

**ORMLite table not created**:
```bash
# Check TableUtils.createTableIfNotExists() is called
# Verify @DatabaseTable and @DatabaseField annotations
```

**JavaFX not found**:
```bash
# Verify JavaFX dependencies in pom.xml
# Check javafx-maven-plugin configuration
```

**Tests failing**:
```bash
# Run with verbose output
mvn test -X

# Run specific test
mvn test -Dtest=SoundDaoTest -X
```

---

## Best Practices

### Code Quality

1. **Write tests first** (TDD: RED → GREEN → REFACTOR)
2. **Keep methods small** (<30 lines)
3. **Use Lombok** to eliminate boilerplate
4. **Document public APIs** with Javadoc
5. **Follow Google Java Style**

### Git Practices

1. **Commit often** (small, logical commits)
2. **Write clear messages** (follow format)
3. **Keep branches short-lived** (<1 week)
4. **Rebase before merging**
5. **Never force push to main/develop**

### Performance

1. **Profile before optimizing**
2. **Use lazy initialization** for JavaFX properties
3. **Cache expensive operations**
4. **Use virtual threads** for I/O operations
5. **Test on target hardware**

---

## Tools

### Recommended IDE Setup

**IntelliJ IDEA**:
- Install Lombok plugin
- Enable annotation processing
- Configure Google Java Format
- Enable JavaFX support

**VS Code**:
```json
{
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "java.configuration.updateBuildConfiguration": "automatic"
}
```

---

**Document Version**: 2.0  
**Last Updated**: 2026-02-05  
**Status**: 🟢 Updated for Java 25/Maven/JavaFX/Lombok/ORMLite  
**Next Review**: After database refactor

---

## Environment Setup

### Initial Setup

```bash
# Clone repository
git clone https://github.com/username/bigglesworth-next.git
cd bigglesworth-next

# Install uv
curl -LsSf https://astral.sh/uv/install.sh | sh

# Create virtual environment
uv venv

# Activate environment
source .venv/bin/activate  # Linux/macOS
# or
.venv\Scripts\activate  # Windows

# Install dependencies
uv pip install -e ".[dev]"

# Install pre-commit hooks
pre-commit install
```

### Project Structure

```
bigglesworth-next/
├── bigglesworth_next/      # Main package
│   ├── __init__.py
│   ├── __main__.py
│   ├── ui/
│   ├── backend/
│   ├── data/
│   ├── midi/
│   └── utils/
├── tests/                  # Tests
├── docs/                   # Documentation
├── legacy/                 # Legacy resources
├── .kiro/                  # Project specs
├── pyproject.toml          # Project config
├── README.md
└── .gitignore
```

---

## Git Workflow

### Branch Strategy

```
main
  ├── develop
  │   ├── feature/midi-protocol
  │   ├── feature/sound-editor
  │   └── bugfix/dial-rendering
  └── hotfix/critical-crash
```

**Branches**:
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical production fixes

### Creating Feature Branch

```bash
# Update develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/midi-protocol

# Work on feature
# ... make changes ...

# Commit changes
git add .
git commit -m "feat(midi): implement SysEx protocol"

# Push to remote
git push origin feature/midi-protocol

# Create pull request on GitHub
```

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Tests
- `chore`: Maintenance

**Examples**:

```
feat(midi): add SysEx protocol handler

Implement encoding/decoding for Blofeld SysEx messages including
SNDP, SNDD, and SNDQ message types with checksum validation.

Closes #42
```

```
fix(ui): correct dial rotation angle

The dial rotation was off by 45 degrees. Fixed calculation
to match legacy behavior exactly.

Fixes #58
```

---

## Development Cycle

### 1. Plan

- Review specifications in `.kiro/`
- Break down into small tasks
- Create GitHub issues

### 2. Implement

```bash
# Create feature branch
git checkout -b feature/blofeld-dial

# Write test first (TDD)
# tests/unit/test_blofeld_dial.py
def test_dial_value_change():
    dial = BlofeldDial()
    dial.value = 64
    assert dial.value == 64

# Run test (should fail)
pytest tests/unit/test_blofeld_dial.py

# Implement minimal code
# ui/widgets/blofeld_dial.py
class BlofeldDial(QWidget):
    def __init__(self):
        super().__init__()
        self._value = 0
    
    @Property(int)
    def value(self):
        return self._value
    
    @value.setter
    def value(self, val):
        self._value = val

# Run test (should pass)
pytest tests/unit/test_blofeld_dial.py

# Refactor if needed
# Add more tests
# Repeat
```

### 3. Test

```bash
# Run all tests
pytest

# Run specific test type
pytest -m unit
pytest -m visual

# Check coverage
pytest --cov=bigglesworth_next --cov-report=html

# Open coverage report
open htmlcov/index.html
```

### 4. Format & Lint

```bash
# Format code
black bigglesworth_next/

# Lint code
ruff check bigglesworth_next/

# Fix auto-fixable issues
ruff check --fix bigglesworth_next/

# Type check
mypy bigglesworth_next/
```

### 5. Commit

```bash
# Stage changes
git add .

# Commit with message
git commit -m "feat(ui): implement Blofeld dial widget"

# Push to remote
git push origin feature/blofeld-dial
```

### 6. Pull Request

1. Create PR on GitHub
2. Fill out PR template
3. Request review
4. Address feedback
5. Merge when approved

---

## Code Review

### Reviewer Checklist

- [ ] Code follows style guide (PEP 8, type hints)
- [ ] Tests included and passing
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
- [ ] Performance acceptable
- [ ] Visual regression tests pass (for UI changes)
- [ ] Commit messages follow format

### Review Process

```bash
# Checkout PR branch
git fetch origin
git checkout feature/blofeld-dial

# Run tests
pytest

# Test manually
python -m bigglesworth_next

# Leave feedback on GitHub
# Approve or request changes
```

---

## Release Process

### Version Numbering

Semantic versioning: `MAJOR.MINOR.PATCH`

- `MAJOR`: Breaking changes
- `MINOR`: New features (backward compatible)
- `PATCH`: Bug fixes

### Creating Release

```bash
# Update version in pyproject.toml
# Update CHANGELOG.md

# Commit version bump
git add pyproject.toml CHANGELOG.md
git commit -m "chore: bump version to 1.0.0"

# Tag release
git tag -a v1.0.0 -m "Release v1.0.0"

# Push tag
git push origin v1.0.0

# GitHub Actions will build and publish
```

---

## Testing Workflow

### Before Committing

```bash
# Run tests
pytest

# Check coverage
pytest --cov

# Format code
black bigglesworth_next/

# Lint
ruff check bigglesworth_next/

# Type check
mypy bigglesworth_next/
```

### Pre-commit Hooks

`.pre-commit-config.yaml`:

```yaml
repos:
  - repo: https://github.com/psf/black
    rev: 24.0.0
    hooks:
      - id: black
  
  - repo: https://github.com/astral-sh/ruff-pre-commit
    rev: v0.6.0
    hooks:
      - id: ruff
        args: [--fix]
  
  - repo: https://github.com/pre-commit/mirrors-mypy
    rev: v1.11.0
    hooks:
      - id: mypy
        additional_dependencies: [types-all]
```

---

## Continuous Integration

### GitHub Actions

`.github/workflows/test.yml`:

```yaml
name: Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [ubuntu-latest, macos-latest, windows-latest]
        python-version: ['3.13']
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: ${{ matrix.python-version }}
      
      - name: Install uv
        run: pip install uv
      
      - name: Install dependencies
        run: uv pip install -e ".[dev]"
      
      - name: Run tests
        run: pytest --cov=bigglesworth_next --cov-report=xml
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./coverage.xml
```

---

## Documentation

### Updating Documentation

```bash
# Edit documentation
vim docs/user-guide.md

# Build documentation
cd docs
make html

# View documentation
open _build/html/index.html
```

### API Documentation

```python
# Use Google-style docstrings
def set_parameter(self, param_id: int, value: int) -> None:
    """Set sound parameter value.
    
    Args:
        param_id: Parameter ID (0-384)
        value: Parameter value (0-127)
    
    Raises:
        ValueError: If param_id or value out of range
    
    Example:
        >>> sound.set_parameter(72, 64)
    """
    pass
```

---

## Troubleshooting

### Common Issues

**Import errors**:
```bash
# Reinstall in editable mode
uv pip install -e .
```

**Test failures**:
```bash
# Run with verbose output
pytest -vv

# Run specific test
pytest tests/unit/test_sound.py::test_parameter_set -vv
```

**Qt issues**:
```bash
# Set Qt platform
export QT_QPA_PLATFORM=offscreen  # For headless testing
```

---

## Best Practices

### Code Quality

1. **Write tests first** (TDD)
2. **Keep functions small** (<50 lines)
3. **Use type hints** everywhere
4. **Document public APIs**
5. **Follow PEP 8**

### Git Practices

1. **Commit often** (small, logical commits)
2. **Write clear messages**
3. **Keep branches short-lived** (<1 week)
4. **Rebase before merging**
5. **Never force push to main/develop**

### Performance

1. **Profile before optimizing**
2. **Cache expensive operations**
3. **Use Qt's update() efficiently**
4. **Minimize database queries**
5. **Test on target hardware**

---

## Tools

### Recommended IDE Setup

**VS Code**:
```json
{
  "python.linting.enabled": true,
  "python.linting.ruffEnabled": true,
  "python.formatting.provider": "black",
  "python.testing.pytestEnabled": true,
  "editor.formatOnSave": true
}
```

**PyCharm**:
- Enable Black formatter
- Enable Ruff linter
- Enable pytest test runner
- Enable type checking

---

**Document Version**: 2.0  
**Last Updated**: 2026-01-27  
**Status**: 🟢 Updated for Python/uv workflow  
**Next Review**: After first release

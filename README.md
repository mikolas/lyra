# Lyra - Waldorf Blofeld Sound Editor

Modern, professional sound editor and librarian for the Waldorf Blofeld synthesizer. Built with Java 25 and JavaFX 25, featuring comprehensive editing capabilities, advanced wavetable creation, and bidirectional MIDI synchronization.

## Features

### Sound Editor
- **385 parameters** - Complete Blofeld sound engine coverage
- **Interactive envelopes** - Draggable ADSR curves with visual feedback
- **Modulation matrix** - 16-slot routing with real-time updates
- **Arpeggiator** - Full pattern editor with step sequencer
- **Bidirectional MIDI sync** - Real-time hardware synchronization (1-5ms latency)

### Sound Librarian
- **Advanced search** - Tokenized search with prefixes (tag:, cat:, bank:)
- **Collections & Tags** - Organize sounds with custom collections and tags
- **Tree navigation** - Hierarchical browsing by bank, category, collection, tag
- **Bulk operations** - Multi-select for batch editing
- **Import/Export** - .mid and .syx file support

### Wavetable Editor (Production-Ready)
- **64 waves × 128 samples** - Full Blofeld wavetable format (21-bit precision)
- **Sparse keyframe system** - Efficient editing with automatic interpolation
- **4 morph types** - Constant, Curve, Translate, Spectral
- **Additive synthesis** - Up to 50 harmonics with 5 wave types
- **Advanced drawing tools** - Freehand, Line, Smooth, Normalize, Invert, Reverse
- **Real-time 3D preview** - Interactive visualization of entire wavetable
- **72 factory presets** - Built-in Blofeld wavetables included
- **MIDI dump** - Send wavetables directly to hardware

### Multi-Mode Editor
- **16-part mixer** - Complete multi-timbral editing
- **Visual editors** - Interactive Keys (88-key piano) and Velocity Ramp displays
- **Group editing** - Mirror parameter changes across selected parts
- **Hardware sync** - Identity-aware mode switching (Single/Multi)
- **MIDI protocol** - Full MULR/MULD implementation

### Settings & Configuration
- **Database management** - Custom path, automatic backups (5-30 min intervals)
- **MIDI configuration** - Device ID, channel filtering (1-16, OMNI)
- **Editor preferences** - Autosave modes, MIDI filtering options
- **Persistent settings** - All preferences saved across restarts

## Tech Stack

- **Java 25** - Modern language features (virtual threads, pattern matching, records)
- **JavaFX 25** - Cross-platform GUI with reactive Properties
- **AtlantaFX** - Modern dark theme system
- **Maven** - Build system with dependency management
- **SQLite + HikariCP** - Lightweight database with connection pooling
- **javax.sound.midi** - Built-in MIDI support
- **JUnit 5** - Comprehensive test coverage

## Code Quality

- **525 tests** - All passing, 0 failures
- **25% test coverage** - Model and service layers fully tested
- **Clean architecture** - Service layer with TDD methodology
- **Zero resource leaks** - Proper thread safety and cleanup
- **Business logic extracted** - 23% remaining in UI controllers

## Requirements

- **Java 25** or later
- **Maven 3.9+**
- **Waldorf Blofeld** synthesizer (hardware or plugin)
- **Operating System**: Windows, macOS, or Linux

## Quick Start

```bash
# Clone repository
git clone https://github.com/mikolas/lyra.git
cd lyra

# Build
mvn clean compile

# Run
mvn javafx:run

# Run tests
mvn test

# Package JAR
mvn package
```

## Installation

### From Source
```bash
mvn clean package
java -jar target/lyra-1.0-SNAPSHOT.jar
```

### Requirements
- Ensure Java 25 is installed: `java -version`
- Maven will download all dependencies automatically

## Usage

### First Launch
1. Connect your Blofeld via USB or MIDI interface
2. Launch Lyra
3. Go to **Settings** → **MIDI Connections**
4. Select your Blofeld device
5. Click **OK** to connect

### Sound Editing
1. Open **Sound Editor** from menu or toolbar
2. Edit parameters - changes sync to hardware in real-time
3. Use **Save** to store in database
4. Use **Dump to Hardware** to save to Blofeld memory

### Wavetable Creation
1. Open **Wavetable Editor**
2. Create keyframes at different wave positions
3. Select morph type (Curve, Translate, Spectral)
4. Click **Apply** to generate interpolated waves
5. Use **Dump to Hardware** to send to Blofeld

See [WAVETABLE_USER_GUIDE.md](WAVETABLE_USER_GUIDE.md) for detailed instructions.

## Project Structure

```
lyra/
├── src/
│   ├── main/
│   │   ├── java/net/mikolas/lyra/
│   │   │   ├── ui/          # JavaFX controllers
│   │   │   ├── service/     # Business logic services
│   │   │   ├── model/       # Data models
│   │   │   ├── midi/        # MIDI protocol
│   │   │   └── db/          # Database layer
│   │   └── resources/       # FXML, CSS, images
│   └── test/                # Unit tests
├── pom.xml                  # Maven configuration
└── README.md
```

## Development

### Building
```bash
mvn clean compile          # Compile source
mvn test                   # Run tests
mvn package                # Create JAR
```

### Code Style
- Google Java Style Guide
- Java 25 features encouraged
- JavaFX Properties for reactive programming
- FXML for UI layouts
- TDD methodology for services

### Testing
```bash
mvn test                   # Run all tests
mvn test -Dtest=ClassName  # Run specific test
```

## Features Status

### Completed ✅
- **Sound Editor** - Full 385-parameter editing with real-time sync
- **Sound Librarian** - Advanced search, collections, tags, bulk operations
- **Wavetable Editor** - Production-ready with keyframes, morphing, additive synthesis
- **Multi-Mode Editor** - 16-part mixer with visual editors and group editing
- **MIDI Protocol** - Bidirectional sync with Identity Handshake
- **Settings** - Database, MIDI, editor, and misc preferences
- **Hardware Dump** - Request/receive sounds and multis from Blofeld

### Planned 🔮
- Sound randomization
- Virtual keyboard
- Wavetable undo/redo
- Theme customization
- Window geometry persistence

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Follow existing code style
4. Add tests for new features
5. Submit a pull request

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Credits

**Inspired by Bigglesworth** - Original Python/Qt editor by Maurizio Berti. The Bigglesworth codebase was used as reference for reverse engineering the Blofeld MIDI protocol.

## Author

**Mikolas Hämäläinen**  
GitHub: [@mikolas](https://github.com/mikolas)  
Email: mikolas@mikolas.net

## Support

- **Issues**: [GitHub Issues](https://github.com/mikolas/lyra/issues)
- **Discussions**: [GitHub Discussions](https://github.com/mikolas/lyra/discussions)

---

**Note**: This is an independent project and is not affiliated with or endorsed by Waldorf Music.

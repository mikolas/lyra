# Bigglesworth-Next: UI Design Guidelines

## Overview

UI design guidelines for Bigglesworth-Next with **strict pixel-perfect legacy compliance**. All UI elements must exactly match the original Bigglesworth design.

---

## Core Principle: Pixel-Perfect Legacy Fidelity

**CRITICAL**: The UI must be a pixel-perfect recreation of legacy Bigglesworth. No creative liberties.

### Measurement Process

1. **Screenshot legacy** at 100% scale
2. **Measure dimensions** in pixels using image editor
3. **Extract colors** using color picker (exact RGB/hex values)
4. **Document spacing** (margins, padding, gaps)
5. **Verify continuously** with visual regression tests

---

## Design Resources

### Source of Truth

All design resources come from `legacy/resources/`:

```
legacy/resources/
├── FiraSans-Regular.ttf      # Primary UI font
├── onesize.ttf                # Display font
├── dial.svg                   # Dial control
├── midiicon*.svg              # MIDI indicators
├── blofeld_dt_perspective_alpha.png
├── blofeld_kb_perspective_alpha.png
└── ... (all other resources)
```

### Color Palette (Extracted from Legacy)

```python
# Dark theme (default)
COLORS_DARK = {
    "background": "#1e1e1e",
    "surface": "#2d2d2d",
    "primary": "#4a9eff",
    "secondary": "#00d4aa",
    "text": "#e0e0e0",
    "text_secondary": "#a0a0a0",
    "border": "#3e3e3e",
    "accent": "#ff6b35",
    "lcd_bg": "#283c28",
    "lcd_fg": "#b4ffb4",
}

# Light theme
COLORS_LIGHT = {
    "background": "#f5f5f5",
    "surface": "#ffffff",
    "primary": "#2196f3",
    "secondary": "#00bcd4",
    "text": "#212121",
    "text_secondary": "#757575",
    "border": "#e0e0e0",
    "accent": "#ff5722",
}
```

---

## Widget Specifications

### Blofeld Display

**Dimensions** (measured from legacy):
- Width: 240px
- Height: 64px
- Border: 2px solid #1e2e1e
- Background: #283c28 (LCD green)
- Text color: #b4ffb4

**Font**:
- Family: OneSize (from onesize.ttf)
- Size: 12pt
- Weight: Normal

**Layout**:
- Sound name: Centered, top 40px
- Bank/Program: Bottom-right, "A001" format

```python
class BlofeldDisplay(QWidget):
    WIDTH = 240
    HEIGHT = 64
    BG_COLOR = QColor(40, 60, 40)
    FG_COLOR = QColor(180, 255, 180)
    BORDER_COLOR = QColor(20, 30, 20)
```

### Blofeld Dial

**Dimensions**:
- Size: 60×60px
- Rotation: -135° to +135° (270° total)
- Indicator: 2px line, 20px length
- Indicator color: #ff6432

**Interaction**:
- Vertical drag to change value
- Sensitivity: 2px = 1 value unit
- Range: 0-127 (configurable)

**Visual**:
- Background: dial.svg from resources
- Value text: Centered, 10pt FiraSans

```python
class BlofeldDial(QWidget):
    SIZE = 60
    ROTATION_MIN = -135
    ROTATION_MAX = 135
    INDICATOR_LENGTH = 20
    INDICATOR_WIDTH = 2
    INDICATOR_COLOR = QColor(255, 100, 50)
```

### LED Indicator

**Dimensions**:
- Size: 16×16px
- States: Off, On, Active (blinking)

**Colors**:
- Off: #404040
- On: #00ff00
- Active: Blink between #00ff00 and #80ff80 (500ms interval)

**Resources**:
- Use midiicon*.svg from legacy

```python
class LEDIndicator(QWidget):
    SIZE = 16
    COLOR_OFF = QColor(64, 64, 64)
    COLOR_ON = QColor(0, 255, 0)
    BLINK_INTERVAL = 500  # ms
```

---

## Layout Guidelines

### Spacing

**Measured from legacy**:
- Widget spacing: 10px
- Section spacing: 20px
- Margin: 15px
- Group padding: 10px

### Grid System

Use QGridLayout with consistent spacing:

```python
layout = QGridLayout()
layout.setSpacing(10)
layout.setContentsMargins(15, 15, 15, 15)
```

### Alignment

- Labels: Left-aligned above controls
- Values: Centered below controls
- Buttons: Right-aligned in toolbars

---

## Typography

### Font Hierarchy

```python
FONTS = {
    "default": ("FiraSans", 10),      # Body text
    "display": ("OneSize", 12),       # LCD display
    "heading": ("FiraSans", 12),      # Section headings
    "small": ("FiraSans", 8),         # Small labels
}
```

### Text Styles

```python
# Heading
font = QFont("FiraSans", 12)
font.setBold(True)

# Body
font = QFont("FiraSans", 10)

# Display
font = QFont("OneSize", 12)
```

---

## Component Patterns

### Parameter Control Group

Standard pattern for parameter controls:

```python
class ParameterGroup(QWidget):
    """Standard parameter control group."""
    
    def __init__(self, label: str, parent=None):
        super().__init__(parent)
        
        layout = QVBoxLayout(self)
        layout.setSpacing(5)
        
        # Label
        label_widget = QLabel(label)
        label_widget.setAlignment(Qt.AlignCenter)
        layout.addWidget(label_widget)
        
        # Control (dial)
        self.dial = BlofeldDial()
        layout.addWidget(self.dial, alignment=Qt.AlignCenter)
        
        # Value display
        self.value_label = QLabel("0")
        self.value_label.setAlignment(Qt.AlignCenter)
        layout.addWidget(self.value_label)
        
        # Connect
        self.dial.valueChanged.connect(self._on_value_changed)
    
    def _on_value_changed(self, value: int):
        self.value_label.setText(str(value))
```

### Section Frame

Standard frame for grouping controls:

```python
class SectionFrame(QFrame):
    """Standard section frame."""
    
    def __init__(self, title: str, parent=None):
        super().__init__(parent)
        
        # Frame style
        self.setFrameStyle(QFrame.StyledPanel | QFrame.Raised)
        self.setLineWidth(1)
        
        # Layout
        layout = QVBoxLayout(self)
        layout.setSpacing(10)
        layout.setContentsMargins(10, 10, 10, 10)
        
        # Title
        title_label = QLabel(title)
        title_font = QFont("FiraSans", 12)
        title_font.setBold(True)
        title_label.setFont(title_font)
        layout.addWidget(title_label)
        
        # Content area
        self.content_layout = QVBoxLayout()
        layout.addLayout(self.content_layout)
```

---

## QML Integration

### Embedding QWidgets in QML

```qml
// High-level layout in QML
import QtQuick
import QtQuick.Layouts
import Bigglesworth 1.0

Rectangle {
    color: "#1e1e1e"
    
    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 15
        spacing: 20
        
        // Section header (QML)
        Text {
            text: "Filter"
            font.family: "FiraSans"
            font.pixelSize: 12
            font.bold: true
            color: "#e0e0e0"
        }
        
        // Parameter controls (Custom QWidgets)
        RowLayout {
            spacing: 10
            
            ColumnLayout {
                Text {
                    text: "Cutoff"
                    font.family: "FiraSans"
                    font.pixelSize: 10
                    color: "#e0e0e0"
                    Layout.alignment: Qt.AlignHCenter
                }
                
                BlofeldDial {
                    id: cutoffDial
                    Layout.alignment: Qt.AlignHCenter
                    onValueChanged: backend.setParameter(72, value)
                }
                
                Text {
                    text: cutoffDial.value.toString()
                    font.family: "FiraSans"
                    font.pixelSize: 10
                    color: "#a0a0a0"
                    Layout.alignment: Qt.AlignHCenter
                }
            }
            
            // More controls...
        }
    }
}
```

---

## Accessibility

### Keyboard Navigation

All controls must be keyboard accessible:

```python
class AccessibleDial(BlofeldDial):
    """Dial with keyboard support."""
    
    def keyPressEvent(self, event):
        if event.key() == Qt.Key_Up:
            self.value = min(self._max_value, self.value + 1)
        elif event.key() == Qt.Key_Down:
            self.value = max(self._min_value, self.value - 1)
        elif event.key() == Qt.Key_PageUp:
            self.value = min(self._max_value, self.value + 10)
        elif event.key() == Qt.Key_PageDown:
            self.value = max(self._min_value, self.value - 10)
        else:
            super().keyPressEvent(event)
```

### Tooltips

All controls should have descriptive tooltips:

```python
dial.setToolTip("Filter Cutoff (0-127)\nControls the filter cutoff frequency")
```

---

## Animation

### Subtle Animations Only

Animations should be subtle and not distract:

```python
from PySide6.QtCore import QPropertyAnimation, QEasingCurve

def animate_value(widget, start: int, end: int, duration: int = 200):
    """Animate value change."""
    animation = QPropertyAnimation(widget, b"value")
    animation.setDuration(duration)
    animation.setStartValue(start)
    animation.setEndValue(end)
    animation.setEasingCurve(QEasingCurve.OutCubic)
    animation.start()
```

---

## Verification Checklist

For each widget implementation:

- [ ] Dimensions match legacy exactly (±1px tolerance)
- [ ] Colors match legacy exactly (RGB values)
- [ ] Fonts match legacy (family, size, weight)
- [ ] Spacing matches legacy (margins, padding)
- [ ] Interaction behavior matches legacy
- [ ] Visual regression test passes (>99% match)
- [ ] Keyboard navigation works
- [ ] Tooltips present and descriptive
- [ ] Renders correctly in both themes

---

## Tools

### Visual Comparison Tool

```python
# tools/visual_compare.py
from PySide6.QtWidgets import QApplication, QWidget, QHBoxLayout, QLabel
from PySide6.QtGui import QPixmap

class VisualComparator(QWidget):
    """Side-by-side visual comparison tool."""
    
    def __init__(self, legacy_screenshot: str, widget: QWidget):
        super().__init__()
        
        layout = QHBoxLayout(self)
        
        # Legacy screenshot
        legacy_label = QLabel("Legacy")
        legacy_pixmap = QPixmap(legacy_screenshot)
        legacy_label.setPixmap(legacy_pixmap)
        layout.addWidget(legacy_label)
        
        # New widget
        new_label = QLabel("New")
        widget.show()
        QApplication.processEvents()
        new_pixmap = widget.grab()
        new_label.setPixmap(new_pixmap)
        layout.addWidget(new_label)
        
        self.setWindowTitle("Visual Comparison")
        self.show()
```

---

**Document Version**: 2.0  
**Last Updated**: 2026-01-27  
**Status**: 🟢 Updated for pixel-perfect legacy compliance  
**Next Review**: After widget implementation

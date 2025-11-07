# Service Station Simulation - OS Assignment

## 📁 Repository Structure

```
Assignment 2/
├── Console/          - Console version (Ready to run)
├── Gui_Bonus/        - JavaFX GUI version (Requires setup)
└── Released Gui/     - Portable GUI (Double-click to run)
```

---

## 🚀 Quick Start

### Option 1: Console Version (Immediate)
```bash
cd "Assignment 2/Console"
javac ServiceStation.java
java ServiceStation
```

### Option 2: JavaFX GUI (Requires JavaFX SDK)
**Prerequisites:**
- Java JDK 17+
- [JavaFX SDK 25.0.1+](https://openjfx.io/)

**Run via Command Line:**
```bash
cd "Assignment 2/Gui_Bonus"
javac --module-path "PATH_TO_JAVAFX/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics ServiceStationGUI.java
java --module-path "PATH_TO_JAVAFX/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics ServiceStationGUI
```

**Run via IntelliJ IDEA:**
1. Open project in IntelliJ IDEA
2. Go to `File > Project Structure > Libraries`
3. Add JavaFX JARs from your JavaFX SDK `lib` folder
4. Go to `Run > Edit Configurations`
5. Add VM options:
   ```
   --module-path "C:\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
   ```
6. Click `Run`

### Option 3: Portable GUI (No Setup Required)
```bash
cd "Assignment 2/Released Gui"
# Double-click ServiceStationGUI.exe
```

---

## ✅ Features

- **Producer-Consumer Pattern** with Semaphores
- **Thread Synchronization** (mutex, empty, full, pumps)
- **Queue Validation** (1-10 range)
- **Adjustable Speed Control** (GUI only)
- **Real-time Visual Updates** (GUI only)
- **Color-coded Activity Logs**

---

##  Submission Date
November 7, 2025

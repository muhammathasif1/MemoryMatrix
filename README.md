# 🧠 Memory Matrix

> A deep-space sci-fi memory game built with Java Swing — test and push your memory to the limit.

![Java](https://img.shields.io/badge/Java-17%2B-blue?style=flat-square&logo=java)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-purple?style=flat-square)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-success?style=flat-square)

---

## 🎮 What is Memory Matrix?

Memory Matrix is a pattern memory game with a deep space aesthetic. A grid of cells appears on screen — some cells flash blue. Your job is to remember which ones lit up and click them from memory.

Each round gets harder. The grid grows. More cells flash. The pressure builds.

---

## ✨ Features

- 🌌 **Deep space UI** — custom starfield, nebula glow, glass panels, and neon cell rendering built entirely with Java2D
- 🎯 **4 game modes** — Classic, Easy, Medium, Hard
- ⚡ **Two flash styles** — ALL (all cells at once) or ONE (sequential, one at a time)
- ⏱️ **Timer bar** — live countdown with color shifts (blue → orange → red)
- 💥 **Decoy system** — Hard mode flickers fake orange cells to trick you
- 🏆 **High score tracking** — best score saved per mode to local file
- ▶️ **Player-controlled pacing** — START, NEXT ROUND, and RETRY buttons mean nothing advances until you're ready
- 🛑 **Stop anytime** — Classic mode has a STOP button to end early
- 💀 **3 lives system** — with visual heart display
- 🎬 **Animated splash screen** — progress bar on launch

---

## 🕹️ How to Play

1. Launch the game and choose a mode from the home screen
2. Press **START GAME** when you're ready
3. Watch the grid — blue cells will flash (ALL at once or ONE by one)
4. After the flash, the grid goes dark
5. Click the cells you remember from the pattern
6. Press **SUBMIT ANSWER** when done
7. ✅ Correct → press **NEXT ROUND** when ready
8. ❌ Wrong → press **RETRY** when ready (costs 1 life)
9. Lose all 3 lives → Game Over

---

## 🗂️ Game Modes

| Mode | Time Limit | Grid Size | Decoys | Notes |
|------|-----------|-----------|--------|-------|
| **Classic** | None | 4×4 → 8×8 | No | Endless, grid grows each round |
| **Easy** | 7 seconds | 4×4 | No | Great for beginners |
| **Medium** | 12 seconds | 4×4 → 6×6 | No | Real pressure builds |
| **Hard** | 20 seconds | Starts 6×6 | ✅ Yes | Orange decoys flicker to fool you |

---

## ⚡ Flash Styles

| Style | Description |
|-------|-------------|
| **ONE** | Cells flash one at a time in sequence — default, easier to track |
| **ALL** | All cells flash at the same moment — harder, tests visual memory |

Switch between them using the **CHOOSE MODE** toggle in the top bar during gameplay.

---

## 📁 Project Structure

```
MemoryMatrix/
└── src/
    └── memorymatrix/
        ├── Main.java          # Entry point
        ├── Theme.java         # Colors, fonts, drawGlassPanel, drawGlowButton, drawStars
        ├── SplashScreen.java  # Animated launch screen
        ├── HomeScreen.java    # Home, Countdown sub-screen, Rules screen
        ├── GameScreen.java    # Core game logic, grid rendering, all modes
        └── ScoreManager.java  # Saves/loads best score per mode
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Any OS with a display (Windows, macOS, Linux)

### Compile

```bash
cd MemoryMatrix/src
javac -encoding UTF-8 memorymatrix/*.java
```

### Run

```bash
java memorymatrix.Main
```

### Package as JAR (optional)

Create `manifest.txt`:
```
Main-Class: memorymatrix.Main
```

Then:
```bash
jar cfm MemoryMatrix.jar manifest.txt memorymatrix/*.class
javaw -jar MemoryMatrix.jar
```

> ⚠️ On Windows, open `.jar` files with `javaw.exe`, not by double-clicking in IntelliJ.

---

## 🎨 Tech & Design

- **Language:** Java (no external libraries)
- **UI Framework:** Java Swing with custom `paintComponent` rendering
- **Graphics:** Java2D — `GradientPaint`, `RadialGradientPaint`, `RenderingHints`, `AlphaComposite`
- **Aesthetic:** Deep space sci-fi — dark backgrounds, glowing cells, starfield, neon borders
- **Persistence:** Scores saved to plain `.txt` files (`memorymatrix_classic.txt`, etc.)

---

## 📸 Screens

| Screen | Description |
|--------|-------------|
| Splash | Animated loading bar with starfield |
| Home | 3 cards — Classic, Countdown, Rules |
| Countdown Select | Easy / Medium / Hard with full details |
| Rules | Scrollable guide with visual grid diagrams |
| Game | Live grid, stat cards, timer bar, flash toggle |
| Game Over | Score, rounds, best score, play again |

---

## 🏆 Scoring

```
Base points  =  round number × 10
Time bonus   =  seconds remaining × 2  (Countdown modes only)

Example:
  Round 6, correct, 11s left on Medium
  = (6 × 10) + (11 × 2)
  = 60 + 22
  = 82 points
```

---

## 👤 Author

**Muhammed Aasif**
- GitHub: [@muhammathasif1](https://github.com/muhammathasif1)

---

## 📌 Also Check Out

- [TYPEX](https://github.com/muhammathasif1/TypingSpeedTester) — A deep-space typing speed tester, also built with Java Swing

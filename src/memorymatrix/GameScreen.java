package memorymatrix;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameScreen extends JFrame {

    // ── Mode config ───────────────────────────────────────────────────────
    // mode: "classic" | "easy" | "medium" | "hard"
    private final String mode;
    private final boolean isClassic;
    private final boolean isTimed;

    // Timed mode settings per difficulty
    private final int recallSeconds;   // recall time limit (timed only)

    // ── Game state ────────────────────────────────────────────────────────
    private enum Phase { READY, FLASHING, RECALLING, RESULT, GAMEOVER }
    private Phase phase = Phase.READY;

    private int round     = 1;
    private int score     = 0;
    private int lives     = 3;
    private int gridSize  = 4;
    private int bestScore = 0;

    private boolean[][] pattern;
    private boolean[][] playerGuess;
    private float[][]   cellGlow;

    // Sequence flash
    private List<int[]> flashSequence;   // ordered list of [row,col] to flash
    private int         flashIndex = 0;  // current index in sequence
    private Timer       sequenceTimer;   // fires to reveal next cell in sequence

    // Decoy flash (hard timed only) — cells that briefly flicker but are NOT in pattern
    private boolean[][] decoyActive;
    private Timer       decoyTimer;

    // Flash phase overall timer (ends flashing phase)
    private Timer flashTimer;
    private Timer glowTimer;

    // Recall timer (timed modes)
    private Timer recallTimer;
    private int   recallSecondsLeft;

    // Animation
    private float[] flashPulse = {0f};
    private boolean pulseUp    = true;
    private boolean[][] sequenceVisible;  // which cells are currently lit in sequence

    // Flash style: false = ONE (default), true = ALL
    private boolean flashAll = false;
    private JButton btnAll, btnOne;

    // Start / stop / next
    private boolean gameStarted = false;
    private JButton startBtn, stopBtn, nextRoundBtn;

    // UI
    private JPanel  gridPanel;
    private JLabel  roundVal, scoreVal, livesVal, bestVal, statusLabel;
    private JPanel  timerBarPanel;
    private JButton submitBtn;
    private JPanel  statsPanel;
    private int     hoverRow = -1, hoverCol = -1;

    // ── Constructor ────────────────────────────────────────────────────────
    public GameScreen(String mode) {
        this.mode      = mode;
        this.isClassic = mode.equals("classic");
        this.isTimed   = !isClassic;

        // Recall time per difficulty
        switch (mode) {
            case "easy":   recallSeconds = 7;  break;
            case "medium": recallSeconds = 12; break;
            case "hard":   recallSeconds = 20; break;
            default:       recallSeconds = 0;  break; // classic = no timer
        }

        setTitle("Memory Matrix \u2014 " + mode.toUpperCase());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(840, isTimed ? 750 : 710);
        setLocationRelativeTo(null);
        setResizable(false);
        setBackground(Theme.BG);

        bestScore = ScoreManager.loadBest(mode);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint space = new GradientPaint(0, 0, Theme.BG, getWidth(), getHeight(), Theme.BG2);
                g2.setPaint(space); g2.fillRect(0, 0, getWidth(), getHeight());
                Theme.drawStars(g2, getWidth(), getHeight(), 77L);
                Color nc = modeColor();
                RadialGradientPaint neb = new RadialGradientPaint(
                    getWidth() / 2f, getHeight() / 2f, 320f,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{Theme.alpha(nc, 38), Theme.alpha(nc, 15), Theme.alpha(Theme.BG, 0)}
                );
                g2.setPaint(neb); g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildGridArea(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        // Glow + pulse animation loop
        glowTimer = new Timer(30, e -> {
            if (phase == Phase.FLASHING) {
                if (pulseUp) { flashPulse[0] = Math.min(1f, flashPulse[0] + 0.06f); if (flashPulse[0] >= 1f) pulseUp = false; }
                else         { flashPulse[0] = Math.max(0.4f, flashPulse[0] - 0.04f); if (flashPulse[0] <= 0.4f) pulseUp = true; }
            }
            if (phase == Phase.RECALLING || phase == Phase.RESULT) {
                for (int r = 0; r < gridSize; r++)
                    for (int c = 0; c < gridSize; c++)
                        if (cellGlow[r][c] > 0) cellGlow[r][c] = Math.max(0f, cellGlow[r][c] - 0.04f);
            }
            gridPanel.repaint();
        });
        glowTimer.start();

        setVisible(true);
        // Game waits for player to press START
    }

    // ── Difficulty helpers ────────────────────────────────────────────────

    private Color modeColor() {
        switch (mode) {
            case "easy":   return new Color(0, 180, 100);
            case "medium": return Theme.WARNING;
            case "hard":   return Theme.DANGER;
            default:       return Theme.BLUE;
        }
    }

    // Classic: very gradual ramp. Challenging from round 5+.
    // Returns [gridSize, cellsToFlash, flashTotalMs, seqIntervalMs]
    private int[] classicDifficulty() {
        int r = round;
        // Grid: stays 4x4 until round 7, then grows every 4 rounds
        int gs = 4;
        if      (r >= 19) gs = 8;
        else if (r >= 15) gs = 7;
        else if (r >= 11) gs = 6;
        else if (r >= 7)  gs = 5;

        // Cells: starts 3, grows slowly
        int cells = Math.min(3 + (r / 2), gs * gs - 3);

        // Sequence interval (ms between each cell lighting up in sequence)
        // Round 1-4: 700ms, then shrinks slowly
        int seqMs = Math.max(220, 700 - (r - 1) * 25);

        // Total flash time = seqMs * cellCount + 400ms extra to see last cell
        int totalMs = seqMs * cells + 400;

        return new int[]{gs, cells, totalMs, seqMs};
    }

    // Timed: fixed params per difficulty level, no progression
    private int[] timedDifficulty() {
        switch (mode) {
            case "easy":
                // 4x4 always, 3-5 cells, long flash (1800ms per cell shown all-at-once)
                return new int[]{4, Math.min(3 + round / 3, 8), 1800, 0};
            case "medium":
                // 4x4 -> 6x6, more cells, shorter flash
                int gsM = round >= 6 ? 6 : round >= 3 ? 5 : 4;
                int cellsM = Math.min(4 + round / 2, gsM * gsM - 4);
                return new int[]{gsM, cellsM, 1400, 0};
            case "hard":
                // Starts at 6x6, very short flash, many cells
                int gsH = round >= 5 ? 8 : round >= 3 ? 7 : 6;
                int cellsH = Math.min(6 + round, gsH * gsH - 4);
                return new int[]{gsH, cellsH, 800, 0};
            default:
                return new int[]{4, 3, 1800, 0};
        }
    }

    // ── Round init ────────────────────────────────────────────────────────
    private void initRound() {
        int[] diff = isClassic ? classicDifficulty() : timedDifficulty();
        gridSize      = diff[0];
        int cellCount = diff[1];
        int flashMs   = diff[2];
        int seqMs     = diff[3]; // 0 = show all at once (timed modes)

        pattern          = new boolean[gridSize][gridSize];
        playerGuess      = new boolean[gridSize][gridSize];
        cellGlow         = new float[gridSize][gridSize];
        sequenceVisible  = new boolean[gridSize][gridSize];
        decoyActive      = new boolean[gridSize][gridSize];
        flashSequence    = new ArrayList<>();
        flashIndex       = 0;
        recallSecondsLeft = recallSeconds;

        // Build random pattern
        List<Integer> allCells = new ArrayList<>();
        for (int i = 0; i < gridSize * gridSize; i++) allCells.add(i);
        Collections.shuffle(allCells);
        for (int i = 0; i < cellCount; i++) {
            int idx = allCells.get(i);
            pattern[idx / gridSize][idx % gridSize] = true;
            flashSequence.add(new int[]{idx / gridSize, idx % gridSize});
        }

        updateStats();
        submitBtn.setVisible(false);
        if (nextRoundBtn != null) nextRoundBtn.setVisible(false);
        hoverRow = hoverCol = -1;
        if (timerBarPanel != null) timerBarPanel.repaint();

        phase = Phase.READY;
        statusLabel.setText("Round " + round + "  \u00b7  " + modeLabel() + "  \u00b7  Get ready...");
        statusLabel.setForeground(Theme.TEXT_MUTED);
        gridPanel.repaint();

        delay(900, this::startFlash);
    }

    private String modeLabel() {
        switch (mode) {
            case "easy":   return "EASY";
            case "medium": return "MEDIUM";
            case "hard":   return "HARD";
            default:       return "CLASSIC";
        }
    }

    // ── Flash phase ───────────────────────────────────────────────────────
    private void startFlash() {
        phase = Phase.FLASHING;
        pulseUp = true; flashPulse[0] = 0.5f;

        // Reset visibility
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                sequenceVisible[r][c] = false;

        // Get flash duration
        int[] diff = isClassic ? classicDifficulty() : timedDifficulty();
        int seqMs   = diff[3]; // interval between cells in ONE mode
        int flashMs = diff[2]; // total ALL-at-once duration

        if (!flashAll) {
            // ── ONE mode: flash cells one at a time ──
            statusLabel.setText(isClassic ? "Watch the sequence!" : "Watch the cells!");
            statusLabel.setForeground(modeColor().brighter());

            // For timed modes seqMs=0, compute a sensible interval
            if (seqMs == 0) seqMs = Math.max(300, flashMs / Math.max(flashSequence.size(), 1));

            flashIndex = 0;
            sequenceTimer = new Timer(seqMs, e -> {
                if (flashIndex > 0) {
                    int[] prev = flashSequence.get(flashIndex - 1);
                    sequenceVisible[prev[0]][prev[1]] = false;
                }
                if (flashIndex < flashSequence.size()) {
                    int[] cur = flashSequence.get(flashIndex);
                    sequenceVisible[cur[0]][cur[1]] = true;
                    flashIndex++;
                    gridPanel.repaint();
                } else {
                    sequenceTimer.stop();
                    delay(350, this::startRecall);
                }
            });
            sequenceTimer.start();

        } else {
            // ── ALL mode: show all cells at once ──
            for (int[] rc : flashSequence) sequenceVisible[rc[0]][rc[1]] = true;
            statusLabel.setText("Memorize the pattern!");
            statusLabel.setForeground(modeColor().brighter());
            gridPanel.repaint();

            // Hard mode decoys
            if (mode.equals("hard")) scheduleDecoys(flashMs);

            flashTimer = new Timer(flashMs, e -> {
                flashTimer.stop();
                for (int r2 = 0; r2 < gridSize; r2++)
                    for (int c2 = 0; c2 < gridSize; c2++) {
                        sequenceVisible[r2][c2] = false;
                        decoyActive[r2][c2]     = false;
                    }
                if (decoyTimer != null) decoyTimer.stop();
                startRecall();
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
        }
    }

    // Hard mode: briefly flicker non-pattern cells as decoys
    private void scheduleDecoys(int flashMs) {
        // Fire 3-5 decoy flickers at random times during flash window
        int decoys = 3 + (int)(Math.random() * 3);
        for (int d = 0; d < decoys; d++) {
            int triggerMs = 200 + (int)(Math.random() * (flashMs - 400));
            final int fd = d;
            delay(triggerMs, () -> {
                if (phase != Phase.FLASHING) return;
                // Pick a non-pattern cell
                List<Integer> nonPattern = new ArrayList<>();
                for (int i = 0; i < gridSize * gridSize; i++) {
                    int r = i / gridSize, c = i % gridSize;
                    if (!pattern[r][c]) nonPattern.add(i);
                }
                if (nonPattern.isEmpty()) return;
                Collections.shuffle(nonPattern);
                int idx = nonPattern.get(0);
                int dr = idx / gridSize, dc = idx % gridSize;
                decoyActive[dr][dc] = true;
                gridPanel.repaint();
                // Hide after 280ms
                delay(280, () -> {
                    if (dr < gridSize && dc < gridSize) decoyActive[dr][dc] = false;
                    gridPanel.repaint();
                });
            });
        }
    }

    // ── Recall phase ──────────────────────────────────────────────────────
    private void startRecall() {
        phase = Phase.RECALLING;
        recallSecondsLeft = recallSeconds;

        if (isTimed) {
            statusLabel.setText("Click the cells!  (\u23f1 " + recallSecondsLeft + "s)");
            statusLabel.setForeground(modeColor().brighter());
            if (timerBarPanel != null) timerBarPanel.repaint();

            recallTimer = new Timer(1000, null);
            recallTimer.addActionListener(e -> {
                recallSecondsLeft--;
                if (timerBarPanel != null) timerBarPanel.repaint();
                Color tc = recallSecondsLeft <= 3 ? Theme.DANGER
                         : recallSecondsLeft <= (recallSeconds / 3) ? Theme.WARNING
                         : modeColor().brighter();
                statusLabel.setText("Click the cells!  (\u23f1 " + recallSecondsLeft + "s)");
                statusLabel.setForeground(tc);
                if (recallSecondsLeft <= 0) { recallTimer.stop(); onTimeout(); }
            });
            recallTimer.start();
        } else {
            statusLabel.setText("Now click the cells you remember!");
            statusLabel.setForeground(Theme.CYAN);
        }

        submitBtn.setVisible(true);
        gridPanel.repaint();
    }

    private void onTimeout() {
        if (phase != Phase.RECALLING) return;
        submitBtn.setVisible(false);
        phase = Phase.RESULT;
        gridPanel.repaint();
        lives--;
        updateStats();
        if (lives <= 0) {
            statusLabel.setText("TIME'S UP!  Game Over!");
            statusLabel.setForeground(Theme.DANGER);
            delay(1800, this::showGameOver);
        } else {
            statusLabel.setText("TIME'S UP!  " + lives + " " + (lives == 1 ? "life" : "lives") + " left  \u2014  Press RETRY when ready");
            statusLabel.setForeground(Theme.WARNING);
            showNextBtn("RETRY  \u21ba", this::initRound);
        }
    }

    // ── Click handling ────────────────────────────────────────────────────
    private void handleClick(int mx, int my) {
        if (phase != Phase.RECALLING) return;
        int[] rc = getCell(mx, my);
        if (rc == null) return;
        playerGuess[rc[0]][rc[1]] = !playerGuess[rc[0]][rc[1]];
        cellGlow[rc[0]][rc[1]] = 1.0f;
        gridPanel.repaint();
    }

    private void handleHover(int mx, int my) {
        if (phase != Phase.RECALLING) { hoverRow = hoverCol = -1; return; }
        int[] rc = getCell(mx, my);
        hoverRow = rc == null ? -1 : rc[0];
        hoverCol = rc == null ? -1 : rc[1];
        gridPanel.repaint();
    }

    private int[] getCell(int mx, int my) {
        int pw = gridPanel.getWidth(), ph = gridPanel.getHeight();
        int total = Math.min(pw, ph) - 20;
        int gap = 7, cell = (total - gap * (gridSize - 1)) / gridSize;
        int sx = (pw - (cell * gridSize + gap * (gridSize - 1))) / 2;
        int sy = (ph - (cell * gridSize + gap * (gridSize - 1))) / 2;
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++) {
                int x = sx + c * (cell + gap), y = sy + r * (cell + gap);
                if (mx >= x && mx <= x + cell && my >= y && my <= y + cell) return new int[]{r, c};
            }
        return null;
    }

    // ── Answer check ─────────────────────────────────────────────────────
    private void checkAnswer() {
        if (phase != Phase.RECALLING) return;
        if (recallTimer != null) recallTimer.stop();
        submitBtn.setVisible(false);
        phase = Phase.RESULT;

        boolean perfect = true;
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                if (pattern[r][c] != playerGuess[r][c]) { perfect = false; break; }

        gridPanel.repaint();

        if (perfect) {
            int base  = round * 10;
            int bonus = isTimed ? recallSecondsLeft * 2 : 0;
            int earned = base + bonus;
            score += earned;
            bestScore = ScoreManager.saveBest(score, mode);

            String bonusStr = isTimed && bonus > 0 ? "  +" + bonus + " time bonus!" : "";
            statusLabel.setText("CORRECT!  +" + earned + " pts" + bonusStr + "  \u2014  Press NEXT when ready");
            statusLabel.setForeground(Theme.SUCCESS);
            updateStats();
            showNextBtn("NEXT ROUND  \u25ba", () -> { round++; initRound(); });
        } else {
            lives--;
            updateStats();
            if (lives <= 0) {
                statusLabel.setText("WRONG!  No lives left  \u2014  Game Over!");
                statusLabel.setForeground(Theme.DANGER);
                delay(1800, this::showGameOver);
            } else {
                statusLabel.setText("WRONG!  " + lives + " " + (lives == 1 ? "life" : "lives") + " left  \u2014  Press RETRY when ready");
                statusLabel.setForeground(Theme.WARNING);
                showNextBtn("RETRY  \u21ba", this::initRound);
            }
        }
    }

    private void showNextBtn(String label, Runnable action) {
        for (ActionListener al : nextRoundBtn.getActionListeners())
            nextRoundBtn.removeActionListener(al);
        nextRoundBtn.setText(label);
        nextRoundBtn.addActionListener(ev -> {
            nextRoundBtn.setVisible(false);
            action.run();
        });
        nextRoundBtn.setVisible(true);
    }

    // ── Draw grid ─────────────────────────────────────────────────────────
    private void drawGrid(Graphics2D g2) {
        int pw = gridPanel.getWidth(), ph = gridPanel.getHeight();
        int total = Math.min(pw, ph) - 20;
        int gap = 7, cell = (total - gap * (gridSize - 1)) / gridSize;
        int sx = (pw - (cell * gridSize + gap * (gridSize - 1))) / 2;
        int sy = (ph - (cell * gridSize + gap * (gridSize - 1))) / 2;
        for (int row = 0; row < gridSize; row++)
            for (int col = 0; col < gridSize; col++)
                drawCell(g2, sx + col * (cell + gap), sy + row * (cell + gap), cell, row, col);
    }

    private void drawCell(Graphics2D g2, int x, int y, int size, int row, int col) {
        boolean isPattern = pattern != null && pattern[row][col];
        boolean isGuess   = playerGuess != null && playerGuess[row][col];
        boolean isVisible = sequenceVisible != null && sequenceVisible[row][col];
        boolean isDecoy   = decoyActive != null && decoyActive[row][col];
        boolean isHover   = (row == hoverRow && col == hoverCol && phase == Phase.RECALLING);
        float   glow      = cellGlow != null ? cellGlow[row][col] : 0f;

        Color cellColor, borderColor;

        switch (phase) {
            case FLASHING:
                if (isVisible) {
                    float p = flashPulse[0];
                    cellColor   = blend(Theme.CELL_OFF, Theme.CELL_FLASH, p);
                    borderColor = Theme.BLUE_BRIGHT;
                } else if (isDecoy) {
                    // Decoy: orange-ish flicker to confuse
                    cellColor   = blend(Theme.CELL_OFF, new Color(255, 140, 0), 0.7f);
                    borderColor = Theme.WARNING;
                } else {
                    cellColor = Theme.CELL_OFF; borderColor = Theme.BORDER_CLR;
                }
                break;
            case RECALLING:
                if (isGuess)      { cellColor = Theme.CELL_ACTIVE;  borderColor = Theme.CYAN; }
                else if (isHover) { cellColor = Theme.CELL_HOVER;   borderColor = Theme.alpha(modeColor(), 150); }
                else              { cellColor = Theme.CELL_OFF;     borderColor = Theme.BORDER_CLR; }
                break;
            case RESULT:
                if       (isPattern &&  isGuess) { cellColor = Theme.CELL_CORRECT; borderColor = Theme.SUCCESS; }
                else if (!isPattern &&  isGuess) { cellColor = Theme.CELL_WRONG;   borderColor = Theme.DANGER; }
                else if  (isPattern && !isGuess) { cellColor = Theme.CELL_MISSED;  borderColor = Theme.WARNING; }
                else                             { cellColor = Theme.CELL_OFF;     borderColor = Theme.BORDER_CLR; }
                break;
            default:
                cellColor = Theme.CELL_OFF; borderColor = Theme.BORDER_CLR;
        }

        // Shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(x + 4, y + 5, size, size, 12, 12);

        // Glow halo
        if (glow > 0.05f) {
            g2.setColor(Theme.alpha(borderColor, (int)(glow * 80)));
            g2.fillRoundRect(x - 4, y - 4, size + 8, size + 8, 16, 16);
        }

        // 3D gradient fill
        GradientPaint fill = new GradientPaint(x, y, cellColor.brighter(), x, y + size, Theme.alpha(cellColor, 200));
        g2.setPaint(fill);
        g2.fillRoundRect(x, y, size, size, 12, 12);

        // Glass shine top
        g2.setColor(Theme.alpha(Color.WHITE, (phase == Phase.FLASHING && (isVisible || isDecoy)) ? 60 : 22));
        g2.fillRoundRect(x + 2, y + 2, size - 4, size / 3, 10, 10);

        // Outer glow for lit cells
        if (phase == Phase.FLASHING && isVisible) {
            g2.setColor(Theme.alpha(borderColor, (int)(flashPulse[0] * 110)));
            g2.setStroke(new BasicStroke(5f));
            g2.drawRoundRect(x - 3, y - 3, size + 6, size + 6, 16, 16);
        }

        // Border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, size, size, 12, 12);
    }

    private Color blend(Color a, Color b, float t) {
        return new Color(
            clamp((int)(a.getRed()   + (b.getRed()   - a.getRed())   * t)),
            clamp((int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t)),
            clamp((int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t))
        );
    }
    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    // ── UI builders ────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        // Outer: title top, stats + controls bottom
        JPanel h = new JPanel(new BorderLayout(0, 6));
        h.setOpaque(false);
        h.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Title row (top)
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("MEMORY MATRIX");
        title.setFont(new Font("Courier New", Font.BOLD, 22));
        title.setForeground(Theme.BLUE_BRIGHT);

        JLabel modeTag = new JLabel(modeBadge(), SwingConstants.RIGHT);
        modeTag.setFont(Theme.MONO_XS);
        modeTag.setForeground(modeColor().brighter());

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(modeTag, BorderLayout.EAST);

        // Stats + controls row (bottom)
        statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPanel.setOpaque(false);

        roundVal = makeStatLabel("1");
        scoreVal = makeStatLabel("0");
        livesVal = makeStatLabel("\u2665\u2665\u2665");
        bestVal  = makeStatLabel(bestScore > 0 ? String.valueOf(bestScore) : "-");

        addStatCard(statsPanel, roundVal, "ROUND", modeColor());
        addStatCard(statsPanel, scoreVal, "SCORE", Theme.PURPLE);
        addStatCard(statsPanel, livesVal, "LIVES", Theme.CYAN_DEEP);
        addStatCard(statsPanel, bestVal,  "BEST",  Theme.GOLD);

        statsPanel.add(Box.createHorizontalStrut(6));
        statsPanel.add(makeFlashTogglePanel());

        JButton homeBtn = new JButton("\u2302") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), Theme.SURFACE3, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        homeBtn.setFont(new Font("Courier New", Font.BOLD, 16));
        homeBtn.setForeground(Theme.TEXT_DIM);
        homeBtn.setContentAreaFilled(false); homeBtn.setBorderPainted(false); homeBtn.setFocusPainted(false);
        homeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeBtn.setPreferredSize(new Dimension(44, 36));
        homeBtn.addActionListener(e -> goHome());
        statsPanel.add(homeBtn);

        h.add(titleRow,   BorderLayout.NORTH);
        h.add(statsPanel, BorderLayout.SOUTH);
        return h;
    }

    private JPanel makeFlashTogglePanel() {
        JPanel tp = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlassPanel((Graphics2D) g, 0, 0, getWidth(), getHeight(), Theme.BORDER_CLR);
            }
        };
        tp.setOpaque(false);
        tp.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        tp.setPreferredSize(new Dimension(130, 58));

        JLabel lbl = new JLabel("CHOOSE MODE", SwingConstants.CENTER);
        lbl.setFont(new Font("Courier New", Font.BOLD, 8));
        lbl.setForeground(Theme.TEXT_MUTED);

        JPanel btns = new JPanel(new GridLayout(1, 2, 4, 0));
        btns.setOpaque(false);

        btnAll = makeToggleBtn("ALL");
        btnOne = makeToggleBtn("ONE");

        // ONE is active by default
        setToggleActive(btnOne, true);
        setToggleActive(btnAll, false);

        btnAll.addActionListener(e -> {
            if (!flashAll) {
                flashAll = true;
                setToggleActive(btnAll, true);
                setToggleActive(btnOne, false);
            }
        });
        btnOne.addActionListener(e -> {
            if (flashAll) {
                flashAll = false;
                setToggleActive(btnOne, true);
                setToggleActive(btnAll, false);
            }
        });

        btns.add(btnAll);
        btns.add(btnOne);

        tp.add(lbl, BorderLayout.NORTH);
        tp.add(btns, BorderLayout.CENTER);
        return tp;
    }

    private JButton makeToggleBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                Color c = active ? modeColor() : Theme.SURFACE3;
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), c, getModel().isRollover());
                // Draw label manually so it always shows
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new Font("Courier New", Font.BOLD, 11));
                boolean active2 = Boolean.TRUE.equals(getClientProperty("active"));
                g2.setColor(active2 ? Color.WHITE : Theme.TEXT_DIM);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(label)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(label, tx, ty);
            }
            @Override protected void paintChildren(Graphics g) { /* skip default text render */ }
        };
        btn.setFont(new Font("Courier New", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setText(label);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(48, 26));
        btn.setMinimumSize(new Dimension(48, 26));
        return btn;
    }

    private void setToggleActive(JButton btn, boolean active) {
        btn.putClientProperty("active", active);
        btn.setForeground(active ? Color.WHITE : Theme.TEXT_MUTED);
        btn.repaint();
    }

    private String modeBadge() {
        switch (mode) {
            case "easy":   return "\u25cb  EASY  \u00b7  7s recall";
            case "medium": return "\u25cf  MEDIUM  \u00b7  12s recall";
            case "hard":   return "\u2605  HARD  \u00b7  20s recall  \u00b7  decoys active";
            default:       return "\u2736  CLASSIC  \u00b7  sequence flash  \u00b7  no time limit";
        }
    }

    private JPanel buildGridArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        if (isTimed) {
            timerBarPanel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), bh = getHeight();
                    g2.setColor(Theme.SURFACE3);
                    g2.fillRoundRect(0, 0, w, bh, bh, bh);
                    float pct = (float) recallSecondsLeft / recallSeconds;
                    int fw = (int)(w * pct);
                    if (fw > 0 && phase == Phase.RECALLING) {
                        Color barC = recallSecondsLeft <= 3 ? Theme.DANGER
                                   : recallSecondsLeft <= recallSeconds / 3 ? Theme.WARNING
                                   : modeColor();
                        GradientPaint gp = new GradientPaint(0, 0, barC.brighter(), fw, 0, barC);
                        g2.setPaint(gp);
                        g2.fillRoundRect(0, 0, fw, bh, bh, bh);
                        if (recallSecondsLeft <= 3) {
                            g2.setColor(Theme.alpha(Theme.DANGER, 120));
                            g2.fillOval(fw - bh, 0, bh + 4, bh + 4);
                        }
                    }
                    g2.setFont(new Font("Courier New", Font.BOLD, 10));
                    g2.setColor(Color.WHITE);
                    String t = recallSecondsLeft + "s";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(t, (w - fm.stringWidth(t)) / 2, bh - 3);
                }
            };
            timerBarPanel.setOpaque(false);
            timerBarPanel.setPreferredSize(new Dimension(100, 20));

            JPanel barRow = new JPanel(new BorderLayout(8, 0));
            barRow.setOpaque(false);
            barRow.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));
            JLabel lbl = new JLabel("RECALL TIME");
            lbl.setFont(Theme.MONO_XS); lbl.setForeground(Theme.TEXT_MUTED);
            lbl.setPreferredSize(new Dimension(95, 20));
            barRow.add(lbl, BorderLayout.WEST);
            barRow.add(timerBarPanel, BorderLayout.CENTER);
            wrapper.add(barRow, BorderLayout.NORTH);
        }

        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.setOpaque(false);

        gridPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawGrid(g2);
            }
        };
        gridPanel.setOpaque(false);
        gridPanel.setPreferredSize(new Dimension(520, 460));
        gridPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });
        gridPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { handleHover(e.getX(), e.getY()); }
        });

        gridWrapper.add(gridPanel);
        wrapper.add(gridWrapper, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        statusLabel = new JLabel("Press START to begin", SwingConstants.CENTER);
        statusLabel.setFont(Theme.MONO_MD);
        statusLabel.setForeground(Theme.TEXT_MUTED);

        // SUBMIT button
        submitBtn = new JButton("SUBMIT ANSWER  \u2713") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), modeColor(), getModel().isRollover());
                super.paintComponent(g);
            }
        };
        submitBtn.setFont(Theme.BTN); submitBtn.setForeground(Color.WHITE);
        submitBtn.setContentAreaFilled(false); submitBtn.setBorderPainted(false); submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.setPreferredSize(new Dimension(210, 44));
        submitBtn.setVisible(false);
        submitBtn.addActionListener(e -> checkAnswer());

        // START button
        startBtn = new JButton("START GAME  \u25ba") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), Theme.SUCCESS, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        startBtn.setFont(Theme.BTN); startBtn.setForeground(Color.WHITE);
        startBtn.setContentAreaFilled(false); startBtn.setBorderPainted(false); startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.setPreferredSize(new Dimension(200, 44));
        startBtn.addActionListener(e -> {
            gameStarted = true;
            startBtn.setVisible(false);
            if (isClassic) stopBtn.setVisible(true);
            initRound();
        });

        // STOP button (classic only)
        stopBtn = new JButton("\u25a0  STOP") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), Theme.DANGER, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        stopBtn.setFont(Theme.BTN_SM); stopBtn.setForeground(Color.WHITE);
        stopBtn.setContentAreaFilled(false); stopBtn.setBorderPainted(false); stopBtn.setFocusPainted(false);
        stopBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stopBtn.setPreferredSize(new Dimension(110, 44));
        stopBtn.setVisible(false);
        stopBtn.addActionListener(e -> {
            stopAll();
            showGameOver();
        });

        // NEXT ROUND button — shown after correct/wrong, player decides when ready
        nextRoundBtn = new JButton("NEXT ROUND  \u25ba") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), Theme.SUCCESS, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        nextRoundBtn.setFont(Theme.BTN); nextRoundBtn.setForeground(Color.WHITE);
        nextRoundBtn.setContentAreaFilled(false); nextRoundBtn.setBorderPainted(false); nextRoundBtn.setFocusPainted(false);
        nextRoundBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextRoundBtn.setPreferredSize(new Dimension(200, 44));
        nextRoundBtn.setVisible(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(startBtn);
        btnRow.add(submitBtn);
        btnRow.add(nextRoundBtn);
        if (isClassic) btnRow.add(stopBtn);

        f.add(statusLabel, BorderLayout.CENTER);
        f.add(btnRow, BorderLayout.SOUTH);
        return f;
    }

    // ── Game over ─────────────────────────────────────────────────────────
    private void showGameOver() {
        phase = Phase.GAMEOVER;
        if (stopBtn != null) stopBtn.setVisible(false);
        bestScore = ScoreManager.saveBest(score, mode);
        boolean newBest = score >= bestScore && score > 0;

        JDialog dlg = new JDialog(this, "Game Over", true);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setUndecorated(true);

        Color accent = modeColor();
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlassPanel((Graphics2D) g, 0, 0, getWidth(), getHeight(), accent);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(26, 36, 22, 36));

        JLabel over = new JLabel("GAME OVER", SwingConstants.CENTER);
        over.setFont(new Font("Courier New", Font.BOLD, 30));
        over.setForeground(Theme.DANGER);
        over.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel modeLbl = new JLabel(modeBadge(), SwingConstants.CENTER);
        modeLbl.setFont(Theme.MONO_XS);
        modeLbl.setForeground(accent.brighter());
        modeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        modeLbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 16, 0));

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        addEndStat(statsRow, String.valueOf(round - 1), "ROUNDS",  Theme.BLUE_BRIGHT);
        addEndStat(statsRow, String.valueOf(score),     "SCORE",   Theme.PURPLE_BRIGHT);
        addEndStat(statsRow, String.valueOf(bestScore), "BEST",    Theme.GOLD);

        JLabel msg = new JLabel(newBest ? "\uD83C\uDFC6  NEW HIGH SCORE!" : "Keep going!", SwingConstants.CENTER);
        msg.setFont(Theme.MONO_MD);
        msg.setForeground(newBest ? Theme.GOLD : Theme.TEXT_MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setOpaque(false);
        btns.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JButton again = makeEndBtn("PLAY AGAIN", accent);
        again.addActionListener(e -> { dlg.dispose(); stopAll(); new GameScreen(mode); });

        JButton home = makeEndBtn("HOME", Theme.SURFACE3);
        home.addActionListener(e -> { dlg.dispose(); goHome(); });

        btns.add(again); btns.add(home);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        over.setAlignmentX(Component.CENTER_ALIGNMENT);
        modeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(over); center.add(modeLbl); center.add(statsRow); center.add(msg);

        p.add(center, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void delay(int ms, Runnable action) {
        Timer t = new Timer(ms, e -> action.run());
        t.setRepeats(false); t.start();
    }

    private void goHome()  { stopAll(); dispose(); new HomeScreen(); }
    private void stopAll() {
        if (glowTimer     != null) glowTimer.stop();
        if (flashTimer    != null) flashTimer.stop();
        if (recallTimer   != null) recallTimer.stop();
        if (sequenceTimer != null) sequenceTimer.stop();
        if (decoyTimer    != null) decoyTimer.stop();
    }

    private void updateStats() {
        roundVal.setText(String.valueOf(round));
        scoreVal.setText(String.valueOf(score));
        livesVal.setText(lives == 3 ? "\u2665\u2665\u2665" : lives == 2 ? "\u2665\u2665\u2661" : lives == 1 ? "\u2665\u2661\u2661" : "\u2661\u2661\u2661");
        livesVal.setForeground(lives == 3 ? Theme.CYAN : lives == 2 ? Theme.WARNING : Theme.DANGER);
        bestVal.setText(bestScore > 0 ? String.valueOf(bestScore) : "-");
    }

    private JLabel makeStatLabel(String val) {
        JLabel l = new JLabel(val, SwingConstants.CENTER);
        l.setFont(Theme.STAT_MD); l.setForeground(Theme.BLUE_BRIGHT);
        return l;
    }

    private void addStatCard(JPanel parent, JLabel valLabel, String lbl, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlassPanel((Graphics2D) g, 0, 0, getWidth(), getHeight(), color);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        valLabel.setForeground(color);
        JLabel l = new JLabel(lbl, SwingConstants.CENTER);
        l.setFont(Theme.MONO_XS); l.setForeground(Theme.TEXT_MUTED);
        card.add(valLabel); card.add(l);
        card.setPreferredSize(new Dimension(90, 58));
        parent.add(card);
    }

    private void addEndStat(JPanel parent, String val, String lbl, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlassPanel((Graphics2D) g, 0, 0, getWidth(), getHeight(), color);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(Theme.STAT_LG); v.setForeground(color);
        JLabel l = new JLabel(lbl, SwingConstants.CENTER);
        l.setFont(Theme.MONO_XS); l.setForeground(Theme.TEXT_MUTED);
        card.add(v); card.add(l);
        parent.add(card);
    }

    private JButton makeEndBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), color, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.BTN_SM); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 42));
        return btn;
    }
}

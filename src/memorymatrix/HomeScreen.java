package memorymatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HomeScreen extends JFrame {

    public HomeScreen() {
        setTitle("Memory Matrix");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setBackground(Theme.BG);
        setContentPane(buildMainPanel());
        setVisible(true);
    }

    // ── Main panel (3 cards) ─────────────────────────────────────────────────
    private JPanel buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                paintBg((Graphics2D) g, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 36, 20, 36));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("MEMORY MATRIX", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 42));
        title.setForeground(Theme.BLUE_BRIGHT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("push your memory to the limit", SwingConstants.CENTER);
        sub.setFont(Theme.MONO_MD);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        int bc = ScoreManager.loadBest("classic");
        int be = ScoreManager.loadBest("easy");
        int bm = ScoreManager.loadBest("medium");
        int bh = ScoreManager.loadBest("hard");
        JLabel bestLbl = new JLabel(
            "Classic: " + fmt(bc) + "   Easy: " + fmt(be) + "   Medium: " + fmt(bm) + "   Hard: " + fmt(bh),
            SwingConstants.CENTER);
        bestLbl.setFont(Theme.MONO_XS);
        bestLbl.setForeground(Theme.PURPLE_BRIGHT);
        bestLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title); header.add(sub); header.add(bestLbl);

        // 3 cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.add(makeCard("CLASSIC", "Endless Mode", Theme.BLUE,
            new String[]{"No time pressure", "4\u00d74 \u2192 8\u00d78 grid", "Gradual difficulty", "Sequence or all-at-once flash", "3 lives  \u00b7  Score saved"},
            "PLAY  \u25ba", e -> { dispose(); new GameScreen("classic"); }
        ));
        cards.add(makeCard("COUNTDOWN", "Race the clock", Theme.PURPLE,
            new String[]{"3 difficulties inside", "Easy: 7s  \u00b7  Medium: 12s  \u00b7  Hard: 20s", "Timer bar shrinks live", "Hard has decoy flickers", "Time bonus points"},
            "SELECT  \u25ba", e -> showCountdownScreen()
        ));
        cards.add(makeCard("RULES", "How to play", Theme.CYAN_DEEP,
            new String[]{"Learn all the mechanics", "Classic and Countdown rules", "Flash style explained", "Scoring breakdown", "Scroll for full guide"},
            "VIEW  \u25ba", e -> showRulesScreen()
        ));

        JLabel footer = new JLabel("v3.0  \u00b7  Memory Matrix  \u00b7  Java Swing", SwingConstants.CENTER);
        footer.setFont(Theme.MONO_XS);
        footer.setForeground(new Color(40, 40, 70));
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        root.add(header, BorderLayout.NORTH);
        root.add(cards, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // ── COUNTDOWN sub-screen ─────────────────────────────────────────────────
    private void showCountdownScreen() {
        JFrame screen = new JFrame("Memory Matrix — COUNTDOWN");
        screen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        screen.setSize(980, 620);
        screen.setLocationRelativeTo(null);
        screen.setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                paintBg((Graphics2D) g, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(28, 40, 24, 40));

        // Back button top-left
        JButton back = makeBackBtn();
        back.addActionListener(e -> screen.dispose());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.setOpaque(false);
        JLabel t = new JLabel("COUNTDOWN MODE");
        t.setFont(new Font("Courier New", Font.BOLD, 30));
        t.setForeground(Theme.PURPLE_BRIGHT);
        JLabel s = new JLabel("choose your difficulty");
        s.setFont(Theme.MONO_SM);
        s.setForeground(Theme.TEXT_MUTED);
        titleCol.add(t); titleCol.add(s);

        JPanel leftSide1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSide1.setOpaque(false);
        leftSide1.add(back);
        leftSide1.add(Box.createHorizontalStrut(18));
        topBar.add(leftSide1, BorderLayout.WEST);
        topBar.add(titleCol, BorderLayout.CENTER);

        // 3 difficulty cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        cards.setOpaque(false);

        cards.add(makeDiffCard("EASY", "7 seconds", new Color(0, 200, 110),
            new String[]{
                "4\u00d74 grid only",
                "Few cells flash",
                "Generous flash time",
                "Good starting point",
                "No decoys",
                "Perfect for beginners",
                "3 lives"
            }, "easy", screen));

        cards.add(makeDiffCard("MEDIUM", "12 seconds", Theme.WARNING,
            new String[]{
                "4\u00d74 \u2192 6\u00d76 grid",
                "More cells flash",
                "Moderate flash time",
                "Real pressure builds",
                "No decoys",
                "Designed for adults",
                "3 lives"
            }, "medium", screen));

        cards.add(makeDiffCard("HARD", "20 seconds", Theme.DANGER,
            new String[]{
                "Starts 6\u00d76 grid",
                "Many cells flash fast",
                "Very short flash time",
                "Decoy flickers active",
                "Tricks your memory",
                "Brutal difficulty",
                "3 lives — good luck"
            }, "hard", screen));

        root.add(topBar, BorderLayout.NORTH);
        root.add(cards, BorderLayout.CENTER);
        screen.setContentPane(root);
        screen.setVisible(true);
    }

    private JPanel makeDiffCard(String label, String time, Color color, String[] features, String modeKey, JFrame parent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlassPanel((Graphics2D) g, 0, 0, getWidth(), getHeight(), color);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 22));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(new Font("Courier New", Font.BOLD, 34));
        nameLbl.setForeground(color.brighter());
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timeLbl = new JLabel(time + " per round", SwingConstants.CENTER);
        timeLbl.setFont(Theme.MONO_SM);
        timeLbl.setForeground(Theme.TEXT_DIM);
        timeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLbl.setBorder(BorderFactory.createEmptyBorder(3, 0, 12, 0));

        JPanel div = makeDivider(color);

        top.add(nameLbl); top.add(timeLbl); top.add(div);
        top.add(Box.createVerticalStrut(10));

        // Feature list
        JPanel fp = new JPanel();
        fp.setLayout(new BoxLayout(fp, BoxLayout.Y_AXIS));
        fp.setOpaque(false);
        for (String f : features) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
            row.setOpaque(false);
            JLabel dot = new JLabel("\u25b8");
            dot.setFont(new Font("Courier New", Font.BOLD, 11)); dot.setForeground(color.brighter());
            JLabel txt = new JLabel(f); txt.setFont(Theme.MONO_SM); txt.setForeground(Theme.TEXT_MUTED);
            row.add(dot); row.add(txt); fp.add(row);
        }

        JButton btn = new JButton("PLAY " + label + "  \u25ba") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), color, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 44));
        btn.addActionListener(e -> { parent.dispose(); dispose(); new GameScreen(modeKey); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        btnRow.add(btn);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(fp, BorderLayout.CENTER);

        card.add(center, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── RULES screen ─────────────────────────────────────────────────────────
    private void showRulesScreen() {
        JFrame screen = new JFrame("Memory Matrix \u2014 RULES");
        screen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        screen.setSize(900, 680);
        screen.setLocationRelativeTo(null);
        screen.setResizable(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                paintBg((Graphics2D) g, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(20, 32, 16, 32));

        // ── Top bar ──────────────────────────────────────────────────────────
        JButton back = makeBackBtn();
        back.addActionListener(e -> screen.dispose());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSide.setOpaque(false);
        leftSide.add(back);
        leftSide.add(Box.createHorizontalStrut(20));

        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.setOpaque(false);
        JLabel t = new JLabel("\uD83D\uDCDA  RULES  \u00b7  HOW TO PLAY");
        t.setFont(new Font("Courier New", Font.BOLD, 24));
        t.setForeground(Theme.CYAN);
        JLabel s = new JLabel("   scroll down to read everything  \u00b7  scroll right if needed");
        s.setFont(Theme.MONO_XS);
        s.setForeground(Theme.TEXT_MUTED);
        titleCol.add(t); titleCol.add(s);

        topBar.add(leftSide, BorderLayout.WEST);
        topBar.add(titleCol, BorderLayout.CENTER);

        // ── Content: wide panel so horizontal scroll works ────────────────────
        JPanel content2 = new JPanel();
        content2.setLayout(new BoxLayout(content2, BoxLayout.Y_AXIS));
        content2.setOpaque(false);
        content2.setBorder(BorderFactory.createEmptyBorder(4, 4, 20, 4));

        // ── 1. OVERVIEW BANNER ───────────────────────────────────────────────
        addBanner(content2, "\u2605  MEMORY MATRIX  \u2014  COMPLETE GUIDE  \u2605",
            "Test your memory by watching a grid flash and recreating the pattern from memory.",
            Theme.BLUE_BRIGHT);

        // ── 2. HOW THE GRID WORKS (with diagram) ─────────────────────────────
        addRichSection(content2, "\u25a6  HOW THE GRID WORKS", Theme.CYAN, new String[][]{
            {"\u25b6", "A grid of dark cells appears on screen"},
            {"\u25b6", "Some cells will FLASH bright blue — that is the pattern"},
            {"\u25b6", "The grid goes dark after the flash"},
            {"\u25b6", "Your job: click the cells that were lit up"},
            {"\u25b6", "Press  SUBMIT ANSWER  when you are done"},
        }, buildGridDiagram());

        // ── 3. LIVES & WINNING ────────────────────────────────────────────────
        addRichSection(content2, "\u2665  LIVES & WINNING", Theme.DANGER, new String[][]{
            {"\u2665", "You start with 3 lives:  \u2665 \u2665 \u2665"},
            {"\u2665", "Wrong answer = lose 1 life"},
            {"\u2665", "Time runs out (Countdown) = lose 1 life"},
            {"\u2665", "Lose all 3 lives = GAME OVER"},
            {"\u2713", "Correct answer = advance to next round"},
            {"\u2713", "Each round gets harder as you progress"},
        }, null);

        // ── 4. FLASH STYLE ────────────────────────────────────────────────────
        addRichSection(content2, "\u26a1  FLASH STYLE  (CHOOSE MODE toggle)", Theme.BLUE_BRIGHT, new String[][]{
            {"ALL", "All cells in the pattern flash at the SAME time"},
            {"ALL", "You must memorise the full pattern at once"},
            {"ONE", "Cells flash ONE at a time in sequence"},
            {"ONE", "Default setting — easier to track each cell"},
            {"\u2699", "Toggle is in the top bar labelled  CHOOSE MODE"},
            {"\u2699", "You can switch ANY time — even mid-game between rounds"},
        }, buildFlashDiagram());

        // ── 5. SCORING ────────────────────────────────────────────────────────
        addRichSection(content2, "\u2605  SCORING", Theme.GOLD, new String[][]{
            {"\u25b6", "Base points per round = round number \u00d7 10"},
            {"\u25b6", "Round 1 = 10 pts   Round 5 = 50 pts   Round 10 = 100 pts"},
            {"\u23f1", "Countdown bonus = seconds remaining \u00d7 2"},
            {"\u23f1", "Submit with 14s left on Easy = +28 bonus points"},
            {"\uD83C\uDFC6", "High score saved separately for each mode"},
            {"\uD83C\uDFC6", "Classic, Easy, Medium and Hard each track independently"},
        }, null);

        // ── 6. CLASSIC MODE ───────────────────────────────────────────────────
        addRichSection(content2, "\u2736  CLASSIC MODE", Theme.BLUE_BRIGHT, new String[][]{
            {"\u221e", "No time limit — take as long as you need to recall"},
            {"\u25a6", "Grid starts at 4\u00d74 and grows up to 8\u00d78"},
            {"\u25a6", "More cells flash each round as you advance"},
            {"\u25a6", "Difficulty ramps up noticeably from round 5 onwards"},
            {"\u25ba", "Press  START GAME  to begin — grid waits for you"},
            {"\u25a0", "Press  STOP  at any time to end and see your score"},
        }, buildProgressDiagram());

        // ── 7. COUNTDOWN MODE ─────────────────────────────────────────────────
        addCountdownSection(content2);

        // ── 8. TIPS ───────────────────────────────────────────────────────────
        addRichSection(content2, "\uD83D\uDCA1  TIPS & STRATEGY", Theme.SUCCESS, new String[][]{
            {"\uD83D\uDCA1", "ONE mode: builds stronger memory — recommended for beginners"},
            {"\uD83D\uDCA1", "ALL mode: harder, trains your visual memory capacity"},
            {"\uD83D\uDCA1", "On large grids: scan row by row, top to bottom"},
            {"\uD83D\uDCA1", "Hard Countdown: ignore orange flickering — only blue cells count"},
            {"\uD83D\uDCA1", "Don't rush clicks — accuracy matters more than speed"},
            {"\uD83D\uDCA1", "Switch to ALL mode once ONE feels too easy"},
        }, null);

        // Scroll wrapper — both axes enabled
        JScrollPane scroll = new JScrollPane(content2);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getHorizontalScrollBar().setUnitIncrement(22);

        root.add(topBar,  BorderLayout.NORTH);
        root.add(scroll,  BorderLayout.CENTER);
        screen.setContentPane(root);
        screen.setVisible(true);
    }

    // ── Rich section with optional diagram panel ──────────────────────────────
    private void addBanner(JPanel parent, String heading, String sub, Color color) {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.alpha(color, 35), getWidth(), 0, Theme.alpha(Theme.PURPLE, 30));
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(Theme.alpha(color, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel h = new JLabel(heading, SwingConstants.CENTER);
        h.setFont(new Font("Courier New", Font.BOLD, 15));
        h.setForeground(color);
        JLabel s = new JLabel(sub, SwingConstants.CENTER);
        s.setFont(Theme.MONO_SM); s.setForeground(Theme.TEXT_DIM);
        JPanel col = new JPanel(); col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS)); col.setOpaque(false);
        h.setAlignmentX(Component.CENTER_ALIGNMENT); s.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(h); col.add(Box.createVerticalStrut(4)); col.add(s);
        banner.add(col, BorderLayout.CENTER);
        parent.add(banner);
        parent.add(Box.createVerticalStrut(12));
    }

    private void addRichSection(JPanel parent, String heading, Color color, String[][] rows, JPanel diagram) {
        // Outer card
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE2);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(Theme.alpha(color, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: heading + rows
        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setOpaque(false);

        // Heading row with colored left bar
        JPanel headingRow = new JPanel(new BorderLayout(10, 0));
        headingRow.setOpaque(false);
        headingRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel colorBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, color, 0, getHeight(), Theme.alpha(color, 60));
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
            }
        };
        colorBar.setOpaque(false); colorBar.setPreferredSize(new Dimension(4, 20));

        JLabel h = new JLabel(heading);
        h.setFont(new Font("Courier New", Font.BOLD, 13));
        h.setForeground(color.brighter());
        headingRow.add(colorBar, BorderLayout.WEST);
        headingRow.add(h, BorderLayout.CENTER);
        left.add(headingRow);

        // Thin divider line
        left.add(Box.createVerticalStrut(8));
        JPanel divLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth() * 2 / 3, 0, Theme.alpha(color, 0));
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), 1);
            }
        };
        divLine.setOpaque(false); divLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divLine.setPreferredSize(new Dimension(300, 1));
        left.add(divLine);
        left.add(Box.createVerticalStrut(8));

        // Rows
        for (String[] row : rows) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            rowPanel.setOpaque(false);
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Icon badge
            JLabel icon = new JLabel(row[0]);
            icon.setFont(new Font("Courier New", Font.BOLD, 11));
            icon.setForeground(color);
            icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            icon.setPreferredSize(new Dimension(28, 18));

            JLabel text = new JLabel(row[1]);
            text.setFont(new Font("Courier New", Font.PLAIN, 12));
            text.setForeground(Theme.TEXT_MUTED);

            rowPanel.add(icon); rowPanel.add(text);
            left.add(rowPanel);
        }

        card.add(left, BorderLayout.CENTER);
        if (diagram != null) card.add(diagram, BorderLayout.EAST);

        parent.add(card);
        parent.add(Box.createVerticalStrut(12));
    }

    private void addCountdownSection(JPanel parent) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE2);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(Theme.alpha(Theme.PURPLE, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setOpaque(false);

        JPanel headingRow = new JPanel(new BorderLayout(10, 0));
        headingRow.setOpaque(false); headingRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, Theme.PURPLE, 0, getHeight(), Theme.alpha(Theme.PURPLE, 60));
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
            }
        };
        bar.setOpaque(false); bar.setPreferredSize(new Dimension(4, 20));
        JLabel h = new JLabel("\u23f1  COUNTDOWN MODE  \u2014  3 DIFFICULTIES");
        h.setFont(new Font("Courier New", Font.BOLD, 13)); h.setForeground(Theme.PURPLE_BRIGHT);
        headingRow.add(bar, BorderLayout.WEST); headingRow.add(h, BorderLayout.CENTER);
        left.add(headingRow);
        left.add(Box.createVerticalStrut(8));

        JPanel divLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                GradientPaint gp = new GradientPaint(0, 0, Theme.PURPLE, getWidth() * 2 / 3, 0, Theme.alpha(Theme.PURPLE, 0));
                ((Graphics2D) g).setPaint(gp); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divLine.setOpaque(false); divLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); divLine.setPreferredSize(new Dimension(300, 1));
        left.add(divLine); left.add(Box.createVerticalStrut(10));

        // 3 difficulty rows
        Color[] dc = {new Color(0,200,110), Theme.WARNING, Theme.DANGER};
        String[] dl = {"EASY", "MEDIUM", "HARD"};
        String[] dt = {"7 seconds", "12 seconds", "20 seconds"};
        String[][] dd = {
            {"4\u00d74 grid", "Few cells flash", "Long flash time", "No decoys", "Beginner friendly"},
            {"4\u00d74 to 6\u00d76", "More cells flash", "Moderate flash", "No decoys", "Real pressure"},
            {"Starts 6\u00d76", "Many cells fast", "Very short flash", "DECOYS ACTIVE", "Brutal difficulty"}
        };

        JPanel diffRow = new JPanel(new GridLayout(1, 3, 10, 0));
        diffRow.setOpaque(false);

        for (int i = 0; i < 3; i++) {
            final Color c = dc[i];
            JPanel box = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.alpha(c, 22));
                    g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                    g2.setColor(Theme.alpha(c, 100));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                }
            };
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setOpaque(false);
            box.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            JLabel name = new JLabel(dl[i], SwingConstants.CENTER);
            name.setFont(new Font("Courier New", Font.BOLD, 14)); name.setForeground(c.brighter());
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel time = new JLabel(dt[i], SwingConstants.CENTER);
            time.setFont(new Font("Courier New", Font.BOLD, 11)); time.setForeground(c);
            time.setAlignmentX(Component.CENTER_ALIGNMENT);
            time.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));

            box.add(name); box.add(time);

            // Mini divider
            JPanel md = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    ((Graphics2D)g).setColor(Theme.alpha(c, 80)); g.fillRect(0, 0, getWidth(), 1);
                }
            };
            md.setOpaque(false); md.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); md.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(md); box.add(Box.createVerticalStrut(6));

            for (String d : dd[i]) {
                JLabel dl2 = new JLabel("\u25b8 " + d);
                dl2.setFont(new Font("Courier New", Font.PLAIN, 10)); dl2.setForeground(Theme.TEXT_MUTED);
                dl2.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
                dl2.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.add(dl2);
            }
            diffRow.add(box);
        }

        left.add(diffRow);

        // Timer bar visual
        left.add(Box.createVerticalStrut(12));
        JPanel timerNote = new JPanel(new BorderLayout(10, 0));
        timerNote.setOpaque(false);
        JLabel timerIcon = new JLabel("\u23f1");
        timerIcon.setFont(new Font("Courier New", Font.BOLD, 11)); timerIcon.setForeground(Theme.PURPLE_BRIGHT);
        timerIcon.setPreferredSize(new Dimension(22, 14));
        JLabel timerText = new JLabel("Timer bar shrinks live  \u00b7  turns orange below 1/3  \u00b7  turns RED below 5s  \u00b7  decoy cells flash ORANGE (ignore them)");
        timerText.setFont(new Font("Courier New", Font.PLAIN, 11)); timerText.setForeground(Theme.TEXT_DIM);
        timerNote.add(timerIcon, BorderLayout.WEST); timerNote.add(timerText, BorderLayout.CENTER);
        left.add(timerNote);

        card.add(left, BorderLayout.CENTER);
        parent.add(card);
        parent.add(Box.createVerticalStrut(12));
    }

    // ── Diagrams ──────────────────────────────────────────────────────────────
    private JPanel buildGridDiagram() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int margin = 8, gap = 4, cell = 28;
                int cols = 4, rows = 4;
                boolean[][] lit = {
                    {false, true,  false, false},
                    {false, false, true,  false},
                    {true,  false, false, false},
                    {false, false, true,  false}
                };

                // Background
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(Theme.alpha(Theme.BLUE, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);

                // Label
                g2.setFont(new Font("Courier New", Font.BOLD, 9));
                g2.setColor(Theme.TEXT_MUTED);
                g2.drawString("PATTERN", margin, margin + 9);

                int startY = margin + 16;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        int x = margin + c * (cell + gap);
                        int y = startY + r * (cell + gap);
                        // Shadow
                        g2.setColor(new Color(0,0,0,80));
                        g2.fillRoundRect(x+2, y+3, cell, cell, 6, 6);
                        if (lit[r][c]) {
                            // Glow
                            g2.setColor(Theme.alpha(Theme.BLUE_BRIGHT, 60));
                            g2.fillRoundRect(x-3, y-3, cell+6, cell+6, 10, 10);
                            GradientPaint gp = new GradientPaint(x, y, Theme.CELL_FLASH.brighter(), x, y+cell, Theme.CELL_FLASH);
                            g2.setPaint(gp);
                        } else {
                            GradientPaint gp = new GradientPaint(x, y, Theme.CELL_OFF.brighter(), x, y+cell, Theme.CELL_OFF);
                            g2.setPaint(gp);
                        }
                        g2.fillRoundRect(x, y, cell, cell, 6, 6);
                        g2.setColor(lit[r][c] ? Theme.BLUE_BRIGHT : Theme.BORDER_CLR);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(x, y, cell, cell, 6, 6);
                    }
                }
                // Arrow + label
                int arrowX = margin + 4 * (cell + gap) + 6;
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(new Font("Courier New", Font.PLAIN, 8));
                g2.drawString("LIT = remember", arrowX - 40, startY + 2 * (cell + gap) + 6);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(165, 168); }
            @Override public Dimension getMinimumSize()   { return new Dimension(165, 168); }
        };
        d.setOpaque(false);
        return d;
    }

    private JPanel buildFlashDiagram() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int margin = 8, gap = 4, cell = 24;

                // Background
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(Theme.alpha(Theme.BLUE, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);

                boolean[] p = {true, false, true, false, false, true, false, true, false};
                int cols = 3;

                // ALL label
                g2.setFont(new Font("Courier New", Font.BOLD, 9));
                g2.setColor(Theme.BLUE_BRIGHT);
                g2.drawString("ALL  \u2014  all lit at once", margin, margin + 10);
                int sy = margin + 16;
                for (int i = 0; i < 9; i++) {
                    int r = i/cols, c = i%cols;
                    int x = margin + c*(cell+gap), y = sy + r*(cell+gap);
                    g2.setColor(new Color(0,0,0,70)); g2.fillRoundRect(x+2,y+2,cell,cell,5,5);
                    g2.setColor(p[i] ? Theme.CELL_FLASH : Theme.CELL_OFF); g2.fillRoundRect(x,y,cell,cell,5,5);
                    g2.setColor(p[i] ? Theme.BLUE_BRIGHT : Theme.BORDER_CLR);
                    g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(x,y,cell,cell,5,5);
                }

                // Separator
                int sepY = sy + 3*(cell+gap) + 6;
                g2.setColor(Theme.alpha(Theme.BORDER_CLR, 100));
                g2.drawLine(margin, sepY, getWidth()-margin, sepY);

                // ONE label
                g2.setFont(new Font("Courier New", Font.BOLD, 9));
                g2.setColor(Theme.PURPLE_BRIGHT);
                g2.drawString("ONE  \u2014  one at a time", margin, sepY + 12);
                int sy2 = sepY + 18;
                for (int i = 0; i < 9; i++) {
                    int r = i/cols, c = i%cols;
                    int x = margin + c*(cell+gap), y = sy2 + r*(cell+gap);
                    g2.setColor(new Color(0,0,0,70)); g2.fillRoundRect(x+2,y+2,cell,cell,5,5);
                    // Only first lit cell shows (as if mid-sequence)
                    boolean showLit = (i == 0);
                    g2.setColor(showLit ? Theme.CELL_FLASH : Theme.CELL_OFF); g2.fillRoundRect(x,y,cell,cell,5,5);
                    g2.setColor(showLit ? Theme.BLUE_BRIGHT : Theme.BORDER_CLR);
                    g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(x,y,cell,cell,5,5);
                }
                // Arrow
                g2.setFont(new Font("Courier New", Font.PLAIN, 8));
                g2.setColor(Theme.TEXT_MUTED);
                g2.drawString("\u25ba one by one...", margin + 2, sy2 + 3*(cell+gap) + 10);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(165, 230); }
            @Override public Dimension getMinimumSize()   { return new Dimension(165, 230); }
        };
        d.setOpaque(false);
        return d;
    }

    private JPanel buildProgressDiagram() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(Theme.alpha(Theme.BLUE, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);

                int mx = 10, my = 12;
                g2.setFont(new Font("Courier New", Font.BOLD, 9));
                g2.setColor(Theme.BLUE_BRIGHT);
                g2.drawString("GRID GROWS OVER TIME", mx, my);

                // Draw progression: small grid -> bigger
                int[][] sizes = {{4,4},{5,5},{6,6},{8,8}};
                String[] labels = {"Rd 1","Rd 3","Rd 5","Rd 7+"};
                Color[] cols2 = {new Color(0,200,110), Theme.WARNING, new Color(255,120,0), Theme.DANGER};
                int cx = mx;
                int cellSz = 6, gap2 = 2;
                int startY = my + 12;

                for (int gi = 0; gi < 4; gi++) {
                    int gs = sizes[gi][0];
                    int gridW = gs * (cellSz + gap2);
                    // Draw mini grid
                    for (int r = 0; r < gs; r++) {
                        for (int c = 0; c < gs; c++) {
                            int x = cx + c*(cellSz+gap2);
                            int y = startY + r*(cellSz+gap2);
                            g2.setColor(Theme.alpha(cols2[gi], 120)); g2.fillRect(x,y,cellSz,cellSz);
                            g2.setColor(Theme.alpha(cols2[gi], 180));
                            g2.setStroke(new BasicStroke(0.5f)); g2.drawRect(x,y,cellSz,cellSz);
                        }
                    }
                    // Label below
                    g2.setFont(new Font("Courier New", Font.PLAIN, 8));
                    g2.setColor(cols2[gi]);
                    g2.drawString(labels[gi], cx, startY + gs*(cellSz+gap2) + 10);
                    g2.drawString(gs+"x"+gs, cx, startY + gs*(cellSz+gap2) + 18);

                    cx += gridW + 10;

                    // Arrow between grids
                    if (gi < 3) {
                        g2.setColor(Theme.TEXT_MUTED);
                        int arY = startY + sizes[gi][0]*(cellSz+gap2)/2;
                        g2.setFont(new Font("Courier New", Font.BOLD, 9));
                        g2.drawString("\u25ba", cx - 8, arY + 4);
                    }
                }
            }
            @Override public Dimension getPreferredSize() { return new Dimension(165, 110); }
            @Override public Dimension getMinimumSize()   { return new Dimension(165, 110); }
        };
        d.setOpaque(false);
        return d;
    }

        // ── Shared card maker ────────────────────────────────────────────────────
    private JPanel makeCard(String badge, String subtitle, Color color,
                             String[] features, String btnLabel, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlassPanel((Graphics2D) g, 0, 0, getWidth(), getHeight(), color);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 22));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel badgeLbl = new JLabel(badge, SwingConstants.CENTER);
        badgeLbl.setFont(new Font("Courier New", Font.BOLD, 34));
        badgeLbl.setForeground(color.brighter());
        badgeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(subtitle, SwingConstants.CENTER);
        subLbl.setFont(Theme.MONO_SM);
        subLbl.setForeground(Theme.TEXT_DIM);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLbl.setBorder(BorderFactory.createEmptyBorder(3, 0, 12, 0));

        top.add(badgeLbl); top.add(subLbl); top.add(makeDivider(color));
        top.add(Box.createVerticalStrut(10));

        JPanel fp = new JPanel();
        fp.setLayout(new BoxLayout(fp, BoxLayout.Y_AXIS));
        fp.setOpaque(false);
        for (String f : features) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
            row.setOpaque(false);
            JLabel dot = new JLabel("\u25b8");
            dot.setFont(new Font("Courier New", Font.BOLD, 11)); dot.setForeground(color.brighter());
            JLabel txt = new JLabel(f); txt.setFont(Theme.MONO_SM); txt.setForeground(Theme.TEXT_MUTED);
            row.add(dot); row.add(txt); fp.add(row);
        }

        JButton btn = new JButton(btnLabel) {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), color, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 44));
        btn.addActionListener(action);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        btnRow.add(btn);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(fp, BorderLayout.CENTER);

        card.add(center, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void paintBg(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint space = new GradientPaint(0, 0, Theme.BG, w, h, Theme.BG2);
        g2.setPaint(space); g2.fillRect(0, 0, w, h);
        Theme.drawStars(g2, w, h, 99L);
        RadialGradientPaint neb = new RadialGradientPaint(w / 2f, h / 3f, 300f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{Theme.alpha(Theme.PURPLE, 45), Theme.alpha(Theme.BLUE_DEEP, 25), Theme.alpha(Theme.BG, 0)});
        g2.setPaint(neb); g2.fillRect(0, 0, w, h);
    }

    private JButton makeBackBtn() {
        JButton btn = new JButton("\u2190  BACK") {
            @Override protected void paintComponent(Graphics g) {
                Theme.drawGlowButton((Graphics2D) g, 0, 0, getWidth(), getHeight(), Theme.SURFACE3, getModel().isRollover());
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.BTN_SM); btn.setForeground(Theme.TEXT_DIM);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 36));
        return btn;
    }

    private JPanel makeDivider(Color color) {
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, Theme.alpha(color, 0), getWidth() / 2, 0, color);
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), 1);
            }
        };
        div.setOpaque(false);
        div.setPreferredSize(new Dimension(100, 1));
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return div;
    }

    private String fmt(int v) { return v > 0 ? v + " pts" : "\u2014"; }
}

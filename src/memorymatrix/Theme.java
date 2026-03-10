package memorymatrix;

import java.awt.*;

public class Theme {
    // Deep space backgrounds
    public static final Color BG         = new Color(4, 4, 14);
    public static final Color BG2        = new Color(8, 8, 22);
    public static final Color SURFACE    = new Color(12, 14, 32);
    public static final Color SURFACE2   = new Color(18, 20, 44);
    public static final Color SURFACE3   = new Color(26, 28, 58);
    public static final Color BORDER_CLR = new Color(50, 55, 100);

    // Blues and purples
    public static final Color BLUE        = new Color(60, 140, 255);
    public static final Color BLUE_BRIGHT = new Color(100, 180, 255);
    public static final Color BLUE_DEEP   = new Color(20, 80, 200);
    public static final Color PURPLE      = new Color(160, 80, 255);
    public static final Color PURPLE_BRIGHT = new Color(200, 130, 255);
    public static final Color PURPLE_DEEP = new Color(100, 40, 180);
    public static final Color CYAN        = new Color(0, 210, 255);
    public static final Color CYAN_DEEP   = new Color(0, 140, 200);
    public static final Color VIOLET      = new Color(120, 60, 220);

    // Status colors
    public static final Color SUCCESS = new Color(0, 220, 130);
    public static final Color DANGER  = new Color(255, 60, 90);
    public static final Color WARNING = new Color(255, 180, 0);

    // Text
    public static final Color TEXT_MAIN  = new Color(210, 215, 240);
    public static final Color TEXT_MUTED = new Color(80, 85, 130);
    public static final Color TEXT_DIM   = new Color(120, 125, 170);

    // Cell states
    public static final Color CELL_OFF     = new Color(14, 16, 38);
    public static final Color CELL_FLASH   = new Color(100, 180, 255);
    public static final Color CELL_ACTIVE  = new Color(60, 140, 255);
    public static final Color CELL_CORRECT = new Color(0, 220, 130);
    public static final Color CELL_WRONG   = new Color(255, 60, 90);
    public static final Color CELL_MISSED  = new Color(255, 180, 0);
    public static final Color CELL_HOVER   = new Color(30, 35, 70);

    public static final Color GOLD   = new Color(255, 210, 0);
    public static final Color SILVER = new Color(185, 185, 200);
    public static final Color BRONZE = new Color(200, 120, 40);

    // Fonts
    public static final Font TITLE   = new Font("Courier New", Font.BOLD, 28);
    public static final Font HEADING = new Font("Courier New", Font.BOLD, 20);
    public static final Font MONO_LG = new Font("Courier New", Font.PLAIN, 16);
    public static final Font MONO_MD = new Font("Courier New", Font.PLAIN, 14);
    public static final Font MONO_SM = new Font("Courier New", Font.PLAIN, 12);
    public static final Font MONO_XS = new Font("Courier New", Font.PLAIN, 10);
    public static final Font STAT_XL = new Font("Courier New", Font.BOLD, 42);
    public static final Font STAT_LG = new Font("Courier New", Font.BOLD, 28);
    public static final Font STAT_MD = new Font("Courier New", Font.BOLD, 20);
    public static final Font BTN     = new Font("Courier New", Font.BOLD, 13);
    public static final Font BTN_SM  = new Font("Courier New", Font.BOLD, 11);

    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    // Draw a glassy 3D panel
    public static void drawGlassPanel(Graphics2D g2, int x, int y, int w, int h, Color accent) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Deep shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(x + 4, y + 6, w, h, 18, 18);
        // Main fill
        GradientPaint bg = new GradientPaint(x, y, SURFACE2, x, y + h, SURFACE);
        g2.setPaint(bg);
        g2.fillRoundRect(x, y, w, h, 18, 18);
        // Inner highlight (top edge - glass effect)
        GradientPaint shine = new GradientPaint(x, y, alpha(Color.WHITE, 28), x, y + h / 3, alpha(Color.WHITE, 0));
        g2.setPaint(shine);
        g2.fillRoundRect(x + 1, y + 1, w - 2, h / 3, 18, 18);
        // Outer glow
        g2.setColor(alpha(accent, 45));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 18, 18);
        // Sharp border
        g2.setColor(alpha(accent, 160));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, w - 1, h - 1, 18, 18);
    }

    // Draw a glowing 3D button bg
    public static void drawGlowButton(Graphics2D g2, int x, int y, int w, int h, Color color, boolean hover) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(x + 3, y + 4, w, h, 12, 12);
        // Fill
        Color top = hover ? color.brighter() : color;
        Color bot = hover ? color : alpha(color, 180);
        GradientPaint gp = new GradientPaint(x, y, top, x, y + h, bot);
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        // Top shine
        g2.setColor(alpha(Color.WHITE, hover ? 60 : 35));
        g2.fillRoundRect(x + 2, y + 2, w - 4, h / 3, 10, 10);
        // Glow
        if (hover) {
            g2.setColor(alpha(color, 80));
            g2.setStroke(new BasicStroke(4f));
            g2.drawRoundRect(x - 1, y - 1, w + 2, h + 2, 14, 14);
        }
    }

    // Draw a starfield into a graphics context
    public static void drawStars(Graphics2D g2, int w, int h, long seed) {
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 0; i < 120; i++) {
            int sx = rng.nextInt(w);
            int sy = rng.nextInt(h);
            int size = rng.nextInt(3);
            int brightness = 40 + rng.nextInt(140);
            g2.setColor(new Color(brightness, brightness, Math.min(255, brightness + 60)));
            if (size == 0) g2.fillRect(sx, sy, 1, 1);
            else g2.fillOval(sx, sy, size, size);
        }
    }
}

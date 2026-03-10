package memorymatrix;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    private float alpha = 0f;
    private int   dots  = 0;
    private int   bar   = 0;

    public SplashScreen() {
        setSize(560, 320);
        setLocationRelativeTo(null);

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                // Deep space bg
                GradientPaint space = new GradientPaint(0, 0, Theme.BG, 0, getHeight(), Theme.BG2);
                g2.setPaint(space);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Stars
                Theme.drawStars(g2, getWidth(), getHeight(), 42L);

                // Nebula glow behind title
                RadialGradientPaint nebula = new RadialGradientPaint(
                    getWidth() / 2f, 100f, 160f,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{Theme.alpha(Theme.PURPLE, 60), Theme.alpha(Theme.BLUE, 30), Theme.alpha(Theme.BG, 0)}
                );
                g2.setPaint(nebula);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Border
                g2.setColor(Theme.alpha(Theme.BLUE, 50));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 20, 20);
                g2.setColor(Theme.BLUE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);

                // Title glow
                g2.setFont(new Font("Courier New", Font.BOLD, 58));
                FontMetrics fm = g2.getFontMetrics();
                String t = "MEMORY";
                int tx = (getWidth() - fm.stringWidth(t)) / 2;
                g2.setColor(Theme.alpha(Theme.PURPLE, 60));
                g2.drawString(t, tx + 3, 98);
                g2.setColor(Theme.BLUE_BRIGHT);
                g2.drawString(t, tx, 95);

                g2.setFont(new Font("Courier New", Font.BOLD, 36));
                fm = g2.getFontMetrics();
                String t2 = "MATRIX";
                int tx2 = (getWidth() - fm.stringWidth(t2)) / 2;
                g2.setColor(Theme.alpha(Theme.PURPLE, 80));
                g2.drawString(t2, tx2 + 2, 135);
                g2.setColor(Theme.PURPLE_BRIGHT);
                g2.drawString(t2, tx2, 133);

                // Tagline
                g2.setFont(new Font("Courier New", Font.PLAIN, 12));
                g2.setColor(Theme.TEXT_MUTED);
                String sub = "test your memory  \u00b7  beat your best";
                fm = g2.getFontMetrics();
                g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, 158);

                // Progress bar track
                int bw = 380, bh = 7, bx = (getWidth() - bw) / 2, by = 192;
                g2.setColor(Theme.SURFACE3);
                g2.fillRoundRect(bx, by, bw, bh, bh, bh);

                // Fill
                int fw2 = (int)(bw * bar / 100.0);
                if (fw2 > 0) {
                    GradientPaint gp = new GradientPaint(bx, by, Theme.BLUE, bx + bw, by, Theme.PURPLE);
                    g2.setPaint(gp);
                    g2.fillRoundRect(bx, by, fw2, bh, bh, bh);
                    // Glow tip
                    g2.setColor(Theme.alpha(Theme.CYAN, 150));
                    g2.fillOval(bx + fw2 - bh, by - 2, bh + 4, bh + 4);
                }

                // Percent + loading text
                g2.setFont(new Font("Courier New", Font.BOLD, 11));
                g2.setColor(Theme.TEXT_DIM);
                String pct = bar + "%";
                fm = g2.getFontMetrics();
                g2.drawString(pct, (getWidth() - fm.stringWidth(pct)) / 2, 216);

                g2.setFont(new Font("Courier New", Font.PLAIN, 11));
                g2.setColor(Theme.TEXT_MUTED);
                String ld = "Loading" + ".".repeat(dots);
                fm = g2.getFontMetrics();
                g2.drawString(ld, (getWidth() - fm.stringWidth(ld)) / 2, 238);

                g2.setFont(new Font("Courier New", Font.PLAIN, 10));
                g2.setColor(new Color(40, 40, 70));
                g2.drawString("v1.0", getWidth() - 40, getHeight() - 10);
            }
        };
        p.setBackground(Theme.BG);
        add(p);
        setVisible(true);

        new Timer(20, e -> { alpha = Math.min(1f, alpha + 0.05f); p.repaint(); if (alpha >= 1f) ((Timer)e.getSource()).stop(); }).start();
        new Timer(350, e -> { dots = (dots + 1) % 4; p.repaint(); }).start();
        Timer barT = new Timer(15, null);
        barT.addActionListener(e -> {
            bar = Math.min(100, bar + 1); p.repaint();
            if (bar >= 100) {
                barT.stop();
                new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); dispose(); new HomeScreen(); }) {{ setRepeats(false); start(); }};
            }
        });
        barT.start();
    }
}

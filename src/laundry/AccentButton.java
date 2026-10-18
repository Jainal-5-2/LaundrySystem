package laundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;

public class AccentButton extends JButton {

    private final Color base;
    private final Color hover;
    private final Color pressed;

    private float shadowAlpha = 0f;
    private boolean hovering = false;

    public AccentButton(String text) {
        this(text, Theme.ACCENT, Theme.ACCENT_HOVER);
    }

    public AccentButton(String text, Color base, Color hover) {
        super(text);
        this.base = base;
        this.hover = hover;
        this.pressed = darken(base, 0.12f);

        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(getFont().deriveFont(Font.BOLD, 13f));
        setBorder(new EmptyBorder(8, 20, 8, 20));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovering = true;
                animateShadow(1f);
            }
            @Override public void mouseExited(MouseEvent e) {
                hovering = false;
                animateShadow(0f);
            }
        });
    }

    private void animateShadow(float target) {
        // Simple immediate snap — good enough and no timer complexity.
        shadowAlpha = target;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = h; // fully pill-shaped

        // Soft drop shadow when hovered
        if (shadowAlpha > 0f) {
            g2.setColor(new Color(0, 0, 0, (int) (28 * shadowAlpha)));
            g2.fill(new RoundRectangle2D.Float(0, 2, w, h - 2, arc, arc));
        }

        Color fill;
        if (!isEnabled()) {
            fill = Theme.BORDER_SUBTLE;
        } else if (getModel().isPressed()) {
            fill = pressed;
        } else if (hovering || getModel().isRollover()) {
            fill = hover;
        } else {
            fill = base;
        }

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        g2.dispose();

        super.paintComponent(g);
    }

    private static Color darken(Color c, float amount) {
        int r = Math.max(0, (int) (c.getRed() * (1 - amount)));
        int g = Math.max(0, (int) (c.getGreen() * (1 - amount)));
        int b = Math.max(0, (int) (c.getBlue() * (1 - amount)));
        return new Color(r, g, b);
    }
}
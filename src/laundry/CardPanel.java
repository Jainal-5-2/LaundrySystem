package laundry;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 22;

        for (int i = 12; i >= 1; i--) {
            float alpha = 0.04f * (13 - i) / 13f;
            g2.setColor(new Color(0f, 0f, 0f, alpha));
            g2.fillRoundRect(i, i + 3, w - i * 2, h - i * 2, arc + i, arc + i);
        }

        g2.setColor(Theme.BG_CARD);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

        g2.setColor(Theme.BORDER_SUBTLE);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
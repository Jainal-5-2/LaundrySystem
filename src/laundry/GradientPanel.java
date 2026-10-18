package laundry;

import javax.swing.*;
import java.awt.*;

public class GradientPanel extends JPanel {

    public GradientPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(
                0, 0, Theme.BG_TOP,
                0, getHeight(), Theme.BG_BOTTOM));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        super.paintComponent(g);
    }
}
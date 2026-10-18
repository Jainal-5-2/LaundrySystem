package laundry;

import javax.swing.*;
import java.awt.Point;

public final class Shake {
    private Shake() {}

    public static void apply(JComponent c) {
        final Point origin = c.getLocation();
        final int[] offsets = {-10, 10, -8, 8, -5, 5, -2, 2, 0};
        final int[] i = {0};

        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            c.setLocation(origin.x + offsets[i[0]], origin.y);
            i[0]++;
            if (i[0] >= offsets.length) {
                c.setLocation(origin);
                ((Timer) e.getSource()).stop();
            }
        });
        t.start();
    }
}
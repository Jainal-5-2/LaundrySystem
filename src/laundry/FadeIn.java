package laundry;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.awt.Window;

public final class FadeIn {

    private FadeIn() {}

    /** Default duration and frame pacing. */
    private static final int DURATION_MS = 220;
    private static final int FRAME_MS    = 16;   // ~60 fps

    public static void apply(Window w) {
        apply(w, DURATION_MS);
    }

    public static void apply(Window w, int durationMs) {
        if (w == null) return;

        // Bail early if translucency isn't available — avoids try/catch noise
        if (GraphicsEnvironment.isHeadless()
                || !w.isDisplayable()
                || !isTranslucencySupported(w)) {
            return;
        }

        try {
            w.setOpacity(0f);
        } catch (Exception ex) {
            return;
        }

        final long start = System.currentTimeMillis();
        final int duration = Math.max(1, durationMs);

        Timer timer = new Timer(FRAME_MS, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float t = Math.min(1f, elapsed / (float) duration);

            // Ease-out cubic: fast at first, gentle at the end
            float eased = 1f - (float) Math.pow(1f - t, 3f);

            try {
                w.setOpacity(eased);
            } catch (Exception ignored) {
                ((Timer) e.getSource()).stop();
                return;
            }
            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.setCoalesce(true);
        timer.start();
    }

    private static boolean isTranslucencySupported(Window w) {
        try {
            return w.getGraphicsConfiguration()
                    .getDevice()
                    .isWindowTranslucencySupported(
                            java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT);
        } catch (Exception ex) {
            return false;
        }
    }
}
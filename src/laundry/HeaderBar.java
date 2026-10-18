package laundry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HeaderBar extends JPanel {

    public HeaderBar(String title, Runnable onLogout) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 12, 14));

        // Left: dot accent + app title
        JLabel dot = new JLabel("●");
        dot.setForeground(Theme.ACCENT);
        dot.setFont(dot.getFont().deriveFont(16f));
        dot.setBorder(new EmptyBorder(0, 0, 0, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(dot);
        left.add(titleLabel);

        // Right: logout button — soft pill
        AccentButton logoutBtn = new AccentButton(
                "Logout",
                Theme.ACCENT_SOFT,
                Theme.ACCENT_SOFT_HOVER);
        logoutBtn.setForeground(Theme.ACCENT);
        logoutBtn.setPreferredSize(new Dimension(96, 34));
        logoutBtn.setFont(logoutBtn.getFont().deriveFont(Font.BOLD, 13f));
        if (onLogout != null) logoutBtn.addActionListener(e -> onLogout.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(logoutBtn);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }
}
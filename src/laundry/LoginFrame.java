package laundry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    // ----- Modern light palette -----
    private static final Color BG_LEFT_TOP    = new Color(0x6366F1); // indigo
    private static final Color BG_LEFT_BOTTOM = new Color(0x8B5CF6); // violet
    private static final Color BG_RIGHT       = new Color(0xFAFAFB);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color TEXT_PRIMARY   = new Color(0x111827);
    private static final Color TEXT_SECONDARY = new Color(0x6B7280);
    private static final Color TEXT_MUTED     = new Color(0x9CA3AF);
    private static final Color INPUT_BG       = new Color(0xF3F4F6);
    private static final Color INPUT_BORDER   = new Color(0xE5E7EB);
    private static final Color ACCENT         = new Color(0x6366F1);
    private static final Color ACCENT_HOVER   = new Color(0x4F46E5);
    private static final Color SUCCESS        = new Color(0x10B981);
    private static final Color ERROR          = new Color(0xEF4444);

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel statusLabel = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Laundry - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(880, 560);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(760, 520));

        // ---------- Root: two-column split layout ----------
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_RIGHT);

        GridBagConstraints rc = new GridBagConstraints();
        rc.gridy = 0;
        rc.fill = GridBagConstraints.BOTH;
        rc.weighty = 1.0;

        // Left branding panel
        rc.gridx = 0;
        rc.weightx = 0.42;
        root.add(buildBrandPanel(), rc);

        // Right form panel
        rc.gridx = 1;
        rc.weightx = 0.58;
        root.add(buildFormPanel(), rc);

        setContentPane(root);
        FadeIn.apply(this);
        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
    }

    // ==================================================================
    // Left: gradient branding panel
    // ==================================================================
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                        0, 0, BG_LEFT_TOP,
                        getWidth(), getHeight(), BG_LEFT_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // soft decorative circles
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
                g2.setColor(Color.WHITE);
                g2.fillOval(-80, getHeight() - 220, 320, 320);
                g2.fillOval(getWidth() - 140, -80, 260, 260);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(48, 44, 48, 44));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1.0; g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Logo mark
        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fill(new RoundRectangle2D.Float(0, 0, 48, 48, 14, 14));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawLine(14, 18, 34, 18);
                g2.drawLine(14, 24, 34, 24);
                g2.drawLine(14, 30, 28, 30);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(48, 48); }
        };

        g.gridy = 0; g.insets = new Insets(0, 0, 40, 0);
        panel.add(logo, g);

        JLabel headline = new JLabel("<html>Fresh clothes,<br>zero hassle.</html>");
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.gridy = 1; g.insets = new Insets(0, 0, 14, 0);
        panel.add(headline, g);

        JLabel tagline = new JLabel(
                "<html>Manage orders, customers and<br>pickups, all from one dashboard.</html>");
        tagline.setForeground(new Color(255, 255, 255, 200));
        tagline.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g.gridy = 2; g.insets = new Insets(0, 0, 40, 0);
        panel.add(tagline, g);

        // small feature bullets
        String[] features = {"Real-time order tracking", "Customer management", "Simple reporting"};
        for (int i = 0; i < features.length; i++) {
            JLabel f = new JLabel("•  " + features[i]);
            f.setForeground(new Color(255, 255, 255, 220));
            f.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g.gridy = 3 + i; g.insets = new Insets(0, 0, 10, 0);
            panel.add(f, g);
        }

        // push everything to top
        g.gridy = 6; g.weighty = 1.0; g.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), g);

        return panel;
    }

    // ==================================================================
    // Right: form panel
    // ==================================================================
    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_RIGHT);
        wrapper.setBorder(new EmptyBorder(40, 48, 40, 48));

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // soft shadow
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fill(new RoundRectangle2D.Float(0, 6, getWidth(), getHeight() - 6, 24, 24));
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(36, 36, 36, 36));
        card.setPreferredSize(new Dimension(400, 460));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome back");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subtitle = new JLabel("Sign in to your admin account");
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        styleField(usernameField);
        styleField(passwordField);
        usernameField.putClientProperty("JTextField.placeholderText", "Username");
        passwordField.putClientProperty("JTextField.placeholderText", "Password");

        PillButton loginBtn = new PillButton("Sign in", ACCENT, ACCENT_HOVER, Color.WHITE);
        PillButton registerBtn = new PillButton("Create account",
                new Color(0xEEF2FF), new Color(0xE0E7FF), ACCENT);
        loginBtn.setPreferredSize(new Dimension(0, 46));
        registerBtn.setPreferredSize(new Dimension(0, 46));

        statusLabel.setForeground(ERROR);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        g.gridy = 0; g.insets = new Insets(0, 0, 6, 0);   card.add(title, g);
        g.gridy = 1; g.insets = new Insets(0, 0, 28, 0);  card.add(subtitle, g);
        g.gridy = 2; g.insets = new Insets(0, 0, 14, 0);  card.add(usernameField, g);
        g.gridy = 3; g.insets = new Insets(0, 0, 22, 0);  card.add(passwordField, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 12, 0);  card.add(loginBtn, g);
        g.gridy = 5; g.insets = new Insets(0, 0, 18, 0);  card.add(registerBtn, g);
        g.gridy = 6; g.insets = new Insets(0, 0, 0, 0);   card.add(statusLabel, g);

        wrapper.add(card);
        getRootPane().setDefaultButton(loginBtn);
        loginBtn.addActionListener(e -> doLogin(loginBtn));
        registerBtn.addActionListener(e -> doRegister());

        return wrapper;
    }

    // ------------------------------------------------------------------
    // Input styling — rounded, light, focused accent ring
    // ------------------------------------------------------------------
    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(0, 46));
        f.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);
        f.setOpaque(false);
        f.setBackground(INPUT_BG);
        f.setBorder(fieldBorder(INPUT_BORDER, 2));

        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(fieldBorder(ACCENT, 2));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(fieldBorder(INPUT_BORDER, 2));
            }
        });
    }

    private javax.swing.border.Border fieldBorder(Color color, int pad) {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(color, 10, 1),
                new EmptyBorder(6, 14, 6, 14));
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------
    private void doLogin(JComponent shakeTarget) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(ERROR);
            statusLabel.setText("Please enter your username and password.");
            Shake.apply(shakeTarget);
            return;
        }

        try {
            if (userDAO.login(username, password)) {
                statusLabel.setText(" ");
                dispose();
                SwingUtilities.invokeLater(() -> {
                    MainFrame mf = new MainFrame();
                    mf.setVisible(true);
                    FadeIn.apply(mf);
                });
            } else {
                statusLabel.setForeground(ERROR);
                statusLabel.setText("Invalid username or password.");
                passwordField.setText("");
                Shake.apply(shakeTarget);
            }
        } catch (SQLException ex) {
            statusLabel.setForeground(ERROR);
            statusLabel.setText("Database error. Check your connection.");
            Shake.apply(shakeTarget);
        }
    }

    // ------------------------------------------------------------------
    // Register — modal dialog, matching light theme
    // ------------------------------------------------------------------
    private void doRegister() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        styleField(userField);
        styleField(passField);
        styleField(confirmField);

        userField.putClientProperty("JTextField.placeholderText", "Username");
        passField.putClientProperty("JTextField.placeholderText", "Password (min 6 chars)");
        confirmField.putClientProperty("JTextField.placeholderText", "Confirm password");

        JLabel dialogStatus = new JLabel(" ");
        dialogStatus.setForeground(ERROR);
        dialogStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dialogStatus.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel form = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(28, 28, 28, 28));
        form.setPreferredSize(new Dimension(380, 380));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create account");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

        JLabel sub = new JLabel("Choose a username and password");
        sub.setForeground(TEXT_SECONDARY);
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);   form.add(title, g);
        g.gridy = 1; g.insets = new Insets(0, 0, 20, 0);  form.add(sub, g);
        g.gridy = 2; g.insets = new Insets(0, 0, 12, 0);  form.add(userField, g);
        g.gridy = 3; g.insets = new Insets(0, 0, 12, 0);  form.add(passField, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 12, 0);  form.add(confirmField, g);
        g.gridy = 5; g.insets = new Insets(0, 0, 16, 0);  form.add(dialogStatus, g);

        JDialog dialog = new JDialog(this, "Register", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        PillButton createBtn = new PillButton("Create", ACCENT, ACCENT_HOVER, Color.WHITE);
        PillButton cancelBtn = new PillButton("Cancel",
                new Color(0xF3F4F6), new Color(0xE5E7EB), TEXT_SECONDARY);
        createBtn.setPreferredSize(new Dimension(0, 42));
        cancelBtn.setPreferredSize(new Dimension(0, 42));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(createBtn);

        g.gridy = 6; g.insets = new Insets(0, 0, 0, 0);
        form.add(buttons, g);

        // Outer wrapper adds padding so the rounded corners don't get clipped
        JPanel dialogRoot = new JPanel(new GridBagLayout());
        dialogRoot.setOpaque(false);
        dialogRoot.setBorder(new EmptyBorder(16, 16, 16, 16));
        dialogRoot.add(form);

        dialog.setContentPane(dialogRoot);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        final boolean[] created = {false};

        cancelBtn.addActionListener(e -> dialog.dispose());

        createBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (username.isEmpty()) {
                dialogStatus.setText("Username is required.");
                Shake.apply(createBtn);
                return;
            }
            if (password.length() < 6) {
                dialogStatus.setText("Password must be at least 6 characters.");
                Shake.apply(createBtn);
                return;
            }
            if (!password.equals(confirm)) {
                dialogStatus.setText("Passwords do not match.");
                Shake.apply(createBtn);
                return;
            }

            try {
                if (userDAO.register(username, password)) {
                    created[0] = true;
                    dialog.dispose();
                } else {
                    dialogStatus.setText("Username already taken.");
                    Shake.apply(createBtn);
                }
            } catch (SQLException ex) {
                dialogStatus.setText("Database error while registering.");
                Shake.apply(createBtn);
            }
        });

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.getRootPane().setDefaultButton(createBtn);
        SwingUtilities.invokeLater(userField::requestFocusInWindow);

        dialog.setVisible(true);

        if (created[0]) {
            statusLabel.setForeground(SUCCESS);
            statusLabel.setText("Account created. You can log in now.");
        }
    }

    // ==================================================================
    // Helper: rounded border
    // ==================================================================
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                    x + thickness / 2f, y + thickness / 2f,
                    w - thickness, h - thickness,
                    radius, radius));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    // ==================================================================
    // Helper: modern pill button with hover
    // ==================================================================
    private static class PillButton extends JButton {
        private final Color base, hover, fg;
        private boolean hovering;

        PillButton(String text, Color base, Color hover, Color fg) {
            super(text);
            this.base = base;
            this.hover = hover;
            this.fg = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(fg);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovering = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovering ? hover : base);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
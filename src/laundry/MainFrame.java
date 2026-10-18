package laundry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Laundry Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);
        setBackground(Theme.BG_BOTTOM);

        // ---------- Content panels ----------
        OrderPanel orders = new OrderPanel();
        ReportPanel reports = new ReportPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));
        tabs.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tabs.putClientProperty("JTabbedPane.tabInsets", new Insets(6, 18, 6, 18));
        tabs.putClientProperty("JTabbedPane.tabAreaInsets", new Insets(6, 4, 0, 4));
        tabs.putClientProperty("JTabbedPane.tabHeight", 38);
        tabs.putClientProperty("JTabbedPane.showTabSeparators", false);
        tabs.putClientProperty("JTabbedPane.tabsOpaque", false);
        DashboardPanel dashboard = new DashboardPanel();

        tabs.addTab("Dashboard", dashboard);
        tabs.addTab("Orders", orders);
        tabs.addTab("Customers", new CustomerPanel());
        tabs.addTab("Services & Pricing", new ServicePanel());
        tabs.addTab("Reports", reports);

        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c == orders) {
                orders.refreshAll();
            } else if (c == reports) {
                reports.generate();
            } else if (c == dashboard) {
                dashboard.refresh();
            }
        });

        // ---------- Header ----------
        HeaderBar header = new HeaderBar("Laundry Management", this::logout);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_SUBTLE);
        sep.setBackground(Theme.BORDER_SUBTLE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.CENTER);
        top.add(sep, BorderLayout.SOUTH);

        // ---------- Root ----------
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_BOTTOM);
        root.setBorder(new EmptyBorder(14, 16, 16, 16));
        root.add(top, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Log out of your admin account?",
                "Confirm logout",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame lf = new LoginFrame();
            lf.setVisible(true);
            FadeIn.apply(lf);
        });
    }
}

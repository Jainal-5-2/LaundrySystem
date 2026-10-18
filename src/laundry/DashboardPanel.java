package laundry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardPanel extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();
    private final BarChart chart = new BarChart();
    private final JLabel chartTitle = new JLabel("Sales: last 14 days");

    private final JLabel kpiOrders = new JLabel("0");
    private final JLabel kpiSales = new JLabel("PHP 0.00");
    private final JLabel kpiUnpaid = new JLabel("PHP 0.00");
    private final JLabel kpiReady = new JLabel("0");

    private int rangeDays = 14;
    private final JToggleButton[] rangeBtns = new JToggleButton[3];

    private final JPanel recentList = new JPanel();

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(14, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        refresh();
    }

    // ------------------------------------------------------------------ header

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(Theme.TEXT_PRIMARY);

        AccentButton refresh = new AccentButton("Refresh",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        refresh.setForeground(Theme.ACCENT);
        refresh.setPreferredSize(new Dimension(110, 38));
        refresh.addActionListener(e -> refresh());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(refresh);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ------------------------------------------------------------------ body

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel kpiRow = buildKpiRow();
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, kpiRow.getPreferredSize().height));

        JPanel chartCard = buildChartCard();
        chartCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel recentCard = buildRecentCard();
        recentCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        recentCard.setPreferredSize(new Dimension(0, 320));
        recentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        body.add(kpiRow);
        body.add(Box.createVerticalStrut(14));
        body.add(chartCard);
        body.add(Box.createVerticalStrut(14));
        body.add(recentCard);
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.add(kpiCard("Today's Orders", kpiOrders, Theme.TEXT_PRIMARY));
        row.add(kpiCard("Today's Sales", kpiSales, Theme.ACCENT));
        row.add(kpiCard("Outstanding", kpiUnpaid, Theme.ERROR));
        row.add(kpiCard("Ready for Pickup", kpiReady, Theme.SUCCESS));
        return row;
    }

    private JPanel kpiCard(String label, JLabel value, Color valueColor) {
        JPanel card = new RoundedCard(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        l.setForeground(Theme.TEXT_SECONDARY);

        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        value.setForeground(valueColor);

        card.add(l, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChartCard() {
        JPanel card = new RoundedCard(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        chartTitle.setFont(chartTitle.getFont().deriveFont(Font.BOLD, 14f));
        chartTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel toggles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toggles.setOpaque(false);
        rangeBtns[0] = rangeToggle("7d", 7);
        rangeBtns[1] = rangeToggle("14d", 14);
        rangeBtns[2] = rangeToggle("30d", 30);
        rangeBtns[1].setSelected(true);
        toggles.add(rangeBtns[0]);
        toggles.add(rangeBtns[1]);
        toggles.add(rangeBtns[2]);

        head.add(chartTitle, BorderLayout.WEST);
        head.add(toggles, BorderLayout.EAST);

        chart.setPreferredSize(new Dimension(0, 220));

        card.add(head, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JToggleButton rangeToggle(String label, int days) {
        JToggleButton b = new RangeChip(label);
        b.addActionListener(e -> {
            rangeDays = days;
            for (JToggleButton other : rangeBtns) {
                other.setSelected(other == b);
            }
            chartTitle.setText("Sales — last " + rangeDays + " days");
            loadChart();
        });
        return b;
    }

    private JPanel buildRecentCard() {
        JPanel card = new RoundedCard(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Recent Orders");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(Theme.TEXT_PRIMARY);

        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setOpaque(false);

        JScrollPane sp = new JScrollPane(recentList);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------------ refresh

    public void refresh() {
        try {
            ReportDAO.Kpis k = reportDAO.todayKpis();
            kpiOrders.setText(Fmt.integer(k.orders));
            kpiSales.setText(Fmt.money(k.sales));
            kpiUnpaid.setText(Fmt.money(k.unpaid));
            kpiReady.setText(Fmt.integer(reportDAO.countByStatus("READY")));
        } catch (SQLException e) {
            DB.error(this, e);
        }

        loadChart();
        loadRecent();
    }

    private void loadChart() {
        try {
            List<ReportDAO.DailyRow> rows = reportDAO.lastNDays(rangeDays);
            chart.setData(rows, "Sales");
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void loadRecent() {
        recentList.removeAll();
        try {
            List<ReportDAO.RecentRow> rows = reportDAO.recentOrders(10);
            if (rows.isEmpty()) {
                JLabel none = new JLabel("No orders yet.");
                none.setForeground(Theme.TEXT_MUTED);
                none.setBorder(new EmptyBorder(8, 8, 8, 8));
                recentList.add(none);
            } else {
                for (ReportDAO.RecentRow r : rows) {
                    recentList.add(buildRecentRow(r));
                }
            }
        } catch (SQLException e) {
            DB.error(this, e);
        }
        recentList.revalidate();
        recentList.repaint();
    }

    private JPanel buildRecentRow(ReportDAO.RecentRow r) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("#" + r.id + "  ·  " + r.customer);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 13f));

        JLabel sub = new JLabel(r.status + "  ·  " + r.when);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 11f));

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        JLabel total = new JLabel(Fmt.money(r.total));
        total.setForeground(Theme.TEXT_PRIMARY);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 13f));

        row.add(left, BorderLayout.CENTER);
        row.add(total, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    // ------------------------------------------------------------------ small components

    private static class RoundedCard extends JPanel {
        RoundedCard(java.awt.LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Float(0, 4, getWidth(), getHeight() - 4, 16, 16));
            g2.setColor(Theme.BG_CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RangeChip extends JToggleButton {
        RangeChip(String label) {
            super(label);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, 11f));
            setBorder(new EmptyBorder(5, 12, 5, 12));
            setForeground(Theme.TEXT_SECONDARY);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean sel = isSelected();
            g2.setColor(sel ? Theme.ACCENT : Theme.BG_INPUT);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    getHeight(), getHeight()));
            g2.dispose();
            setForeground(sel ? Color.WHITE : Theme.TEXT_SECONDARY);
            super.paintComponent(g);
        }
    }
}
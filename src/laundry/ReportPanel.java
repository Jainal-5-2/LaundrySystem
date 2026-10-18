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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.jdesktop.swingx.JXDatePicker;

public class ReportPanel extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();

    private final JXDatePicker dpFrom = new JXDatePicker();
    private final JXDatePicker dpTo = new JXDatePicker();

    private final JLabel kpiOrders = new JLabel("0");
    private final JLabel kpiSales = new JLabel("PHP 0.00");
    private final JLabel kpiCollected = new JLabel("PHP 0.00");
    private final JLabel kpiUnpaid = new JLabel("PHP 0.00");

    private final JTable dailyTable = new JTable();
    private final JTable serviceTable = new JTable();

    public ReportPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(14, 16, 16, 16));

        add(buildTop(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        // Default range: this month
        dpFrom.setDate(toDate(LocalDate.now().withDayOfMonth(1)));
        dpTo.setDate(toDate(LocalDate.now()));

        SwingUtilities.invokeLater(this::generate);
    }

    private static final java.text.NumberFormat MONEY_FMT;
    private static final java.text.NumberFormat INT_FMT;

    static {
        MONEY_FMT = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
        MONEY_FMT.setMinimumFractionDigits(2);
        MONEY_FMT.setMaximumFractionDigits(2);
        INT_FMT = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
    }

    // ------------------------------------------------------------------ top
    private JPanel buildTop() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Reports");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(Theme.TEXT_PRIMARY);

        // ---- Preset chips ----
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(presetChip("Today", 0));
        chips.add(presetChip("This Week", 7));
        chips.add(presetChip("This Month", -1));
        chips.add(presetChip("Last 30 Days", 30));
        chips.add(presetChip("This Year", -2));

        // ---- Date pickers + Generate ----
        JPanel pickers = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pickers.setOpaque(false);
        pickers.add(muted("From"));
        styleDatePicker(dpFrom);
        pickers.add(dpFrom);
        pickers.add(muted("To"));
        styleDatePicker(dpTo);
        pickers.add(dpTo);

        AccentButton gen = new AccentButton("Generate");
        gen.setPreferredSize(new Dimension(120, 36));
        gen.addActionListener(e -> generate());
        pickers.add(gen);

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(chips, BorderLayout.NORTH);
        center.add(pickers, BorderLayout.SOUTH);

        outer.add(title, BorderLayout.NORTH);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    private JToggleButton presetChip(String label, int days) {
        PresetChip chip = new PresetChip(label);
        chip.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            LocalDate from;
            if (days == 0) {
                from = today;
            } else if (days == -1) {
                from = today.withDayOfMonth(1);
            } else if (days == -2) {
                from = today.withDayOfYear(1);
            } else {
                from = today.minusDays(days - 1);
            }
            dpFrom.setDate(toDate(from));
            dpTo.setDate(toDate(today));
            generate();
        });
        return chip;
    }

    // ------------------------------------------------------------------ body
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel kpiRow = buildKpiRow();
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, kpiRow.getPreferredSize().height));

        JPanel dailyCard = buildDailyCard();
        dailyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        dailyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JPanel serviceCard = buildServiceCard();
        serviceCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        serviceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        body.add(kpiRow);
        body.add(Box.createVerticalStrut(14));
        body.add(dailyCard);
        body.add(Box.createVerticalStrut(14));
        body.add(serviceCard);
        body.add(Box.createVerticalGlue());

        // Wrap the whole thing so it scrolls when the window is short
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
        row.add(kpiCard("Orders", kpiOrders, Theme.TEXT_PRIMARY));
        row.add(kpiCard("Sales", kpiSales, Theme.ACCENT));
        row.add(kpiCard("Collected", kpiCollected, Theme.SUCCESS));
        row.add(kpiCard("Unpaid", kpiUnpaid, Theme.ERROR));
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

    private JPanel buildDailyCard() {
        JPanel card = new RoundedCard(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Daily Breakdown");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(Theme.TEXT_PRIMARY);

        styleTable(dailyTable);
        dailyTable.setAutoCreateRowSorter(true);

        JScrollPane sp = new JScrollPane(dailyTable);
        styleScroll(sp);
        sp.setPreferredSize(new Dimension(0, 220));
        sp.setMinimumSize(new Dimension(0, 220));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildServiceCard() {
        JPanel card = new RoundedCard(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Sales by Service");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(Theme.TEXT_PRIMARY);

        styleTable(serviceTable);
        serviceTable.setAutoCreateRowSorter(true);

        JScrollPane sp = new JScrollPane(serviceTable);
        styleScroll(sp);

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------------ data
    public void generate() {
        Date fromDate = dpFrom.getDate();
        Date toDate = dpTo.getDate();
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Please select both dates.");
            return;
        }
        LocalDate fromLd = toLocalDate(fromDate);
        LocalDate toLd = toLocalDate(toDate);
        if (fromLd.isAfter(toLd)) {
            JOptionPane.showMessageDialog(this, "'From' must be before 'To'.");
            return;
        }
        java.sql.Date from = java.sql.Date.valueOf(fromLd);
        java.sql.Date to = java.sql.Date.valueOf(toLd);

        try {
            // KPIs
            ReportDAO.Kpis k = reportDAO.kpis(from, to);
            kpiOrders.setText(Fmt.integer(k.orders));
            kpiSales.setText(Fmt.money(k.sales));
            kpiCollected.setText(Fmt.money(k.collected));
            kpiUnpaid.setText(Fmt.money(k.unpaid));

            // Daily table
            List<ReportDAO.DailyRow> daily = reportDAO.dailyBreakdown(from, to);
            DefaultTableModel dm = new DefaultTableModel(
                    new String[]{"Date", "Orders", "Sales", "Collected", "Unpaid"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (ReportDAO.DailyRow d : daily) {
                dm.addRow(new Object[]{d.date, d.orders, d.sales, d.collected, d.unpaid});
            }
            dailyTable.setModel(dm);
            dailyTable.setAutoCreateRowSorter(true);
            styleTable(dailyTable);
            attachMoney(dailyTable, 2, 3, 4);

            // Service table
            List<ReportDAO.ServiceRow> services = reportDAO.salesByService(from, to);
            DefaultTableModel sm = new DefaultTableModel(
                    new String[]{"Service", "Orders", "Qty", "Revenue"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (ReportDAO.ServiceRow s : services) {
                sm.addRow(new Object[]{s.service, s.orders, s.qty, s.revenue});
            }
            serviceTable.setModel(sm);
            styleTable(serviceTable);
            attachMoney(serviceTable, 3);

        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    // ------------------------------------------------------------------ date helpers
    private static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ------------------------------------------------------------------ styling
    private JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private void styleDatePicker(JXDatePicker dp) {
        dp.setPreferredSize(new Dimension(150, 34));
        dp.setFont(dp.getFont().deriveFont(13f));
        dp.setFormats("yyyy-MM-dd");
        dp.setBackground(Theme.BG_INPUT);
        dp.setForeground(Theme.TEXT_PRIMARY);

        JFormattedTextField editor = dp.getEditor();
        editor.setBackground(Theme.BG_INPUT);
        editor.setForeground(Theme.TEXT_PRIMARY);
        editor.setCaretColor(Theme.ACCENT);
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(4, 8, 4, 8)));

        JButton btn = dp.getComponent(1) instanceof JButton ? (JButton) dp.getComponent(1) : null;
        if (btn != null) {
            btn.setBackground(Theme.BG_INPUT);
            btn.setForeground(Theme.TEXT_SECONDARY);
            btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true));
            btn.setFocusPainted(false);
        }
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true));
        sp.getViewport().setBackground(Theme.BG_CARD);
        sp.setOpaque(false);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(Theme.BG_CARD);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setSelectionBackground(Theme.ACCENT_SOFT);
        t.setSelectionForeground(Theme.TEXT_PRIMARY);
        t.setFont(t.getFont().deriveFont(13f));
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setBackground(Theme.BG_BOTTOM);
        h.setForeground(Theme.TEXT_SECONDARY);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 12f));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer base = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.BG_CARD : new Color(0xF9FAFB));
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        t.setDefaultRenderer(Object.class, base);
    }

    private void attachMoney(JTable t, int... columns) {
        DefaultTableCellRenderer money = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String text;
                if (value instanceof Number) {
                    text = Fmt.money(((Number) value).doubleValue());
                } else {
                    text = value == null ? "" : value.toString();
                }
                Component c = super.getTableCellRendererComponent(
                        table, text, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.BG_CARD : new Color(0xF9FAFB));
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 12));
                return c;
            }
        };
        for (int col : columns) {
            t.getColumnModel().getColumn(col).setCellRenderer(money);
        }
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

    private static class PresetChip extends JToggleButton {

        PresetChip(String label) {
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
            g2.setColor(Theme.BG_INPUT);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

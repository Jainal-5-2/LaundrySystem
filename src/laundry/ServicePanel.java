package laundry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class ServicePanel extends JPanel {

    private final JTextField tfName = new JTextField(18);
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"PER_KILO", "PER_ITEM"});
    private final JTextField tfPrice = new JTextField(8);
    private final JTable table = new JTable();
    private int selectedId = -1;

    public ServicePanel() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---------- Form ----------
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 4, 4, 4),
                sectionBorder("Service Details")));

        form.add(mutedLabel("Service"));
        styleTextField(tfName);
        form.add(tfName);

        form.add(mutedLabel("Pricing"));
        styleCombo(cbType);
        form.add(cbType);

        form.add(mutedLabel("Price (PHP)"));
        styleTextField(tfPrice);
        form.add(tfPrice);

        AccentButton btnAdd = new AccentButton("Add");
        AccentButton btnUpdate = new AccentButton("Update");
        AccentButton btnDelete = new AccentButton("Delete",
                Theme.ERROR_SOFT, new Color(0xFCA5A5));
        btnDelete.setForeground(Theme.ERROR);
        AccentButton btnClear = new AccentButton("Clear",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        btnClear.setForeground(Theme.ACCENT);

        btnAdd.setPreferredSize(new Dimension(90, 34));
        btnUpdate.setPreferredSize(new Dimension(90, 34));
        btnDelete.setPreferredSize(new Dimension(90, 34));
        btnClear.setPreferredSize(new Dimension(90, 34));

        form.add(btnAdd);
        form.add(btnUpdate);
        form.add(btnDelete);
        form.add(btnClear);

        btnAdd.addActionListener(e -> add());
        btnUpdate.addActionListener(e -> update());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clear());

        // ---------- Table ----------
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pick();
            }
        });

        // ---------- Titled wrapper for the table ----------
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 4, 4, 4),
                sectionBorder("Services & Pricing")));

        JScrollPane sp = new JScrollPane(table);
        styleScroll(sp);
        tableWrap.add(sp, BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(tableWrap, BorderLayout.CENTER);
        refresh();
    }

    // ---------------------------------------------------------------- styling helpers

    private javax.swing.border.Border sectionBorder(String title) {
        javax.swing.border.TitledBorder tb = BorderFactory.createTitledBorder(title);
        tb.setTitleColor(Theme.TEXT_PRIMARY);
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD, 13f));
        return tb;
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private void styleTextField(JTextField f) {
        f.setBackground(Theme.BG_INPUT);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(2, 6, 2, 6)));
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

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.BG_CARD : new Color(0xF9FAFB));
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        t.setDefaultRenderer(Object.class, r);

        // Price column right-aligned + formatted
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = String.format("PHP %.2f", ((Number) value).doubleValue());
                }
                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.BG_CARD : new Color(0xF9FAFB));
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 12));
                return c;
            }
        };
        // Applied per refresh(), since setModel() resets columns.
        t.putClientProperty("priceRenderer", priceRenderer);
    }

    // ---------------------------------------------------------------- data

    private void refresh() {
        try {
            table.setModel(DB.query("SELECT id AS ID, name AS Service, pricing_type AS Pricing, "
                    + "price AS Price FROM services ORDER BY name"));
            styleTable(table);

            // Attach the price renderer after the model is set
            Object r = table.getClientProperty("priceRenderer");
            if (r instanceof DefaultTableCellRenderer && table.getColumnCount() >= 4) {
                table.getColumnModel().getColumn(3).setCellRenderer((DefaultTableCellRenderer) r);
            }
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void pick() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = ((Number) table.getValueAt(row, 0)).intValue();
        tfName.setText(Objects.toString(table.getValueAt(row, 1), ""));
        cbType.setSelectedItem(Objects.toString(table.getValueAt(row, 2), "PER_KILO"));
        tfPrice.setText(Objects.toString(table.getValueAt(row, 3), ""));
    }

    private void clear() {
        selectedId = -1;
        tfName.setText("");
        tfPrice.setText("");
        cbType.setSelectedIndex(0);
        table.clearSelection();
    }

    /** Returns the price, or -1 if the form is invalid. */
    private double readForm() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Service name is required.");
            return -1;
        }
        try {
            double p = Double.parseDouble(tfPrice.getText().trim());
            if (p < 0) {
                throw new NumberFormatException();
            }
            return p;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid price.");
            return -1;
        }
    }

    private void add() {
        double price = readForm();
        if (price < 0) {
            return;
        }
        try {
            DB.update("INSERT INTO services(name, pricing_type, price) VALUES(?,?,?)",
                    tfName.getText().trim(), cbType.getSelectedItem(), price);
            clear();
            refresh();
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void update() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Select a service first.");
            return;
        }
        double price = readForm();
        if (price < 0) {
            return;
        }
        try {
            DB.update("UPDATE services SET name=?, pricing_type=?, price=? WHERE id=?",
                    tfName.getText().trim(), cbType.getSelectedItem(), price, selectedId);
            clear();
            refresh();
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void delete() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Select a service first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this service?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            DB.update("DELETE FROM services WHERE id=?", selectedId);
            clear();
            refresh();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete: this service is used in existing orders.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
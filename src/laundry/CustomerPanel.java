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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class CustomerPanel extends JPanel {

    private final JTextField tfName = new JTextField(15);
    private final JTextField tfPhone = new JTextField(12);
    private final JTextField tfAddress = new JTextField(20);
    private final JTable table = new JTable();
    private int selectedId = -1;

    public CustomerPanel() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---------- Form ----------
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 4, 4, 4),
                sectionBorder("Customer Details")));

        form.add(mutedLabel("Name"));
        styleTextField(tfName);
        form.add(tfName);

        form.add(mutedLabel("Phone"));
        styleTextField(tfPhone);
        form.add(tfPhone);

        form.add(mutedLabel("Address"));
        styleTextField(tfAddress);
        form.add(tfAddress);

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

        // ---------- Wrapper for the table with a titled border ----------
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 4, 4, 4),
                sectionBorder("Customers")));

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
    }

    // ---------------------------------------------------------------- data

    private void refresh() {
        try {
            table.setModel(DB.query("SELECT id AS ID, name AS Name, phone AS Phone, "
                    + "address AS Address FROM customers ORDER BY name"));
            styleTable(table);
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
        tfPhone.setText(Objects.toString(table.getValueAt(row, 2), ""));
        tfAddress.setText(Objects.toString(table.getValueAt(row, 3), ""));
    }

    private void clear() {
        selectedId = -1;
        tfName.setText("");
        tfPhone.setText("");
        tfAddress.setText("");
        table.clearSelection();
    }

    private boolean valid() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return false;
        }
        return true;
    }

    private void add() {
        if (!valid()) {
            return;
        }
        try {
            DB.update("INSERT INTO customers(name, phone, address) VALUES(?,?,?)",
                    tfName.getText().trim(), tfPhone.getText().trim(), tfAddress.getText().trim());
            clear();
            refresh();
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void update() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer first.");
            return;
        }
        if (!valid()) {
            return;
        }
        try {
            DB.update("UPDATE customers SET name=?, phone=?, address=? WHERE id=?",
                    tfName.getText().trim(), tfPhone.getText().trim(), tfAddress.getText().trim(), selectedId);
            clear();
            refresh();
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void delete() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this customer?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            DB.update("DELETE FROM customers WHERE id=?", selectedId);
            clear();
            refresh();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete: this customer has existing orders.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
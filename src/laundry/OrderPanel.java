package laundry;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class OrderPanel extends JPanel {

    // ------------------------------------------------------------------ state
    private final JTable ordersTable = new JTable();
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * Currently applied status filter — null = all.
     */
    private String statusFilter = null;
    private final List<FilterChip> filterChips = new ArrayList<>();

    // Detail widgets
    private final JLabel detailTitle = new JLabel("No order selected");
    private final JLabel detailCustomer = new JLabel("—");
    private final JLabel detailPhone = new JLabel("—");
    private final JLabel detailDate = new JLabel("—");
    private final StatusPill statusPill = new StatusPill();
    private final JComboBox<String> statusCombo
            = new JComboBox<>(new String[]{"RECEIVED", "WASHING", "READY", "CLAIMED"});

    /**
     * Container for line-item rows (replaces the old JTable).
     */
    private final JPanel itemsList = new JPanel();

    private final JLabel lblTotal = new JLabel("PHP 0.00");
    private final JLabel lblPaid = new JLabel("PHP 0.00");
    private final JLabel lblBalance = new JLabel("PHP 0.00");

    private final CardLayout detailCards = new CardLayout();
    private final JPanel detailRoot = new JPanel(detailCards);

    private boolean suppressSelectionEvents = false;

    // ------------------------------------------------------------------ ctor
    public OrderPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildOrdersListPanel(), buildDetailPanel());
        split.setResizeWeight(0.42);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Theme.BORDER_SUBTLE);
                        g.fillRect(getWidth() / 2 - 1, 0, 2, getHeight());
                    }
                };
            }
        });
        add(split, BorderLayout.CENTER);

        styleTable(ordersTable);

        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || suppressSelectionEvents) {
                return;
            }
            int id = currentOrderId();
            if (id > 0) {
                loadOrderDetail(id);
            } else {
                detailCards.show(detailRoot, "empty");
            }
        });

        statusCombo.addActionListener(e -> {
            if (suppressSelectionEvents) {
                return;
            }
            if (statusCombo.getSelectedItem() == null) {
                return;
            }
            int id = currentOrderId();
            if (id > 0) {
                updateStatus(id);
            }
        });

        refreshAll();
    }

    // ------------------------------------------------------------------ toolbar
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        AccentButton btnNew = new AccentButton("+  New Order");
        btnNew.setPreferredSize(new Dimension(150, 40));
        btnNew.addActionListener(e -> openNewOrderDialog());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(btnNew);

        AccentButton btnPay = new AccentButton("Add Payment",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        btnPay.setForeground(Theme.ACCENT);
        AccentButton btnPrint = new AccentButton("Print Receipt",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        btnPrint.setForeground(Theme.ACCENT);
        AccentButton btnRefresh = new AccentButton("Refresh",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        btnRefresh.setForeground(Theme.ACCENT);

        btnPay.setPreferredSize(new Dimension(140, 40));
        btnPrint.setPreferredSize(new Dimension(140, 40));
        btnRefresh.setPreferredSize(new Dimension(100, 40));

        btnPay.addActionListener(e -> addPayment());
        btnPrint.addActionListener(e -> {
            int id = currentOrderId();
            if (id > 0) {
                showReceipt(id);
            } else {
                JOptionPane.showMessageDialog(this, "Select an order first.");
            }
        });
        btnRefresh.addActionListener(e -> refreshAll());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnPay);
        right.add(btnPrint);
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ------------------------------------------------------------------ orders list + filter
    private JPanel buildOrdersListPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel heading = new JLabel("Orders");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setBorder(new EmptyBorder(0, 4, 8, 0));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chips.setOpaque(false);
        chips.setBorder(new EmptyBorder(0, 0, 8, 0));

        chips.add(makeChip("All", null));
        chips.add(makeChip("Received", "RECEIVED"));
        chips.add(makeChip("Washing", "WASHING"));
        chips.add(makeChip("Ready", "READY"));
        chips.add(makeChip("Claimed", "CLAIMED"));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(chips, BorderLayout.SOUTH);

        JScrollPane sp = new JScrollPane(ordersTable);
        styleScroll(sp);

        p.add(top, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private FilterChip makeChip(String label, String status) {
        FilterChip chip = new FilterChip(label, status);
        if (status == null) {
            chip.setSelected(true);
        }
        chip.addActionListener(e -> {
            statusFilter = status;
            for (FilterChip c : filterChips) {
                c.setSelected(c == chip);
            }
            refreshOrders();
            detailCards.show(detailRoot, "empty");
        });
        filterChips.add(chip);
        return chip;
    }

    // ------------------------------------------------------------------ detail panel
    private JPanel buildDetailPanel() {
        // ----- Empty state -----
        JPanel empty = new JPanel(new GridBagLayout());
        empty.setOpaque(false);
        JLabel emptyMsg = new JLabel("<html><div style='text-align:center;'>"
                + "Select an order to view details<br>"
                + "<span style='color:#9CA3AF;font-size:11px;'>"
                + "or create a new order to get started</span></div></html>");
        emptyMsg.setForeground(Theme.TEXT_SECONDARY);
        emptyMsg.setFont(emptyMsg.getFont().deriveFont(14f));
        empty.add(emptyMsg);

        // ----- Detail content (stacked, not stretched) -----
        JPanel detailContent = new JPanel();
        detailContent.setLayout(new BoxLayout(detailContent, BoxLayout.Y_AXIS));
        detailContent.setOpaque(false);
        detailContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        detailTitle.setFont(detailTitle.getFont().deriveFont(Font.BOLD, 20f));
        detailTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(detailTitle, BorderLayout.WEST);
        statusPill.setBorder(new EmptyBorder(0, 0, 0, 4));
        headerRow.add(statusPill, BorderLayout.EAST);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerRow.getPreferredSize().height));

        // ----- Customer card -----
        JPanel customerCard = buildCustomerCard();
        customerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, customerCard.getPreferredSize().height));

        // ----- Items section -----
        JLabel itemsHeading = sectionLabel("Items");
        itemsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
        itemsList.setOpaque(false);
        itemsList.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane itemsScroll = new JScrollPane(itemsList);
        styleScroll(itemsScroll);
        itemsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemsScroll.setPreferredSize(new Dimension(0, 200));
        itemsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // ----- Totals card -----
        JPanel totalsCard = buildTotalsCard();
        totalsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalsCard.getPreferredSize().height));

        detailContent.add(headerRow);
        detailContent.add(Box.createVerticalStrut(16));
        detailContent.add(customerCard);
        detailContent.add(Box.createVerticalStrut(16));
        detailContent.add(itemsHeading);
        detailContent.add(Box.createVerticalStrut(6));
        detailContent.add(itemsScroll);
        detailContent.add(Box.createVerticalStrut(16));
        detailContent.add(totalsCard);
        detailContent.add(Box.createVerticalGlue());

        // ----- Wrap in a scroll pane so nothing squeezes -----
        JScrollPane detailScroll = new JScrollPane(detailContent);
        detailScroll.setBorder(null);
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.getVerticalScrollBar().setUnitIncrement(16);

        // ----- Card swap -----
        detailRoot.setOpaque(false);
        detailRoot.add(empty, "empty");
        detailRoot.add(detailScroll, "detail");
        detailCards.show(detailRoot, "empty");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(4, 4, 4, 4));
        wrapper.add(detailRoot, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildCustomerCard() {
        JPanel card = new RoundedCard(new GridBagLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 6, 12);
        card.add(fieldLabel("Customer"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 6, 0);
        detailCustomer.setForeground(Theme.TEXT_PRIMARY);
        detailCustomer.setFont(detailCustomer.getFont().deriveFont(13f));
        card.add(detailCustomer, g);

        g.gridx = 0;
        g.weightx = 0;
        g.gridy = 1;
        g.insets = new Insets(0, 0, 6, 12);
        card.add(fieldLabel("Phone"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 6, 0);
        detailPhone.setForeground(Theme.TEXT_PRIMARY);
        detailPhone.setFont(detailPhone.getFont().deriveFont(13f));
        card.add(detailPhone, g);

        g.gridx = 0;
        g.weightx = 0;
        g.gridy = 2;
        g.insets = new Insets(0, 0, 6, 12);
        card.add(fieldLabel("Date"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 6, 0);
        detailDate.setForeground(Theme.TEXT_PRIMARY);
        detailDate.setFont(detailDate.getFont().deriveFont(13f));
        card.add(detailDate, g);

        g.gridx = 0;
        g.weightx = 0;
        g.gridy = 3;
        g.insets = new Insets(0, 0, 0, 12);
        card.add(fieldLabel("Status"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 0, 0);
        styleCombo(statusCombo);
        statusCombo.setPreferredSize(new Dimension(160, 32));
        card.add(statusCombo, g);

        return card;
    }

    private JPanel buildTotalsCard() {
        JPanel card = new RoundedCard(new GridBagLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.weightx = 0;
        g.gridy = 0;
        g.insets = new Insets(0, 0, 6, 12);
        card.add(fieldLabel("Total"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 6, 0);
        g.anchor = GridBagConstraints.EAST;
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        lblTotal.setForeground(Theme.TEXT_PRIMARY);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(lblTotal, g);

        g.gridx = 0;
        g.weightx = 0;
        g.gridy = 1;
        g.insets = new Insets(0, 0, 6, 12);
        g.anchor = GridBagConstraints.WEST;
        card.add(fieldLabel("Paid"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 6, 0);
        g.anchor = GridBagConstraints.EAST;
        lblPaid.setForeground(Theme.SUCCESS);
        lblPaid.setFont(lblPaid.getFont().deriveFont(Font.BOLD, 14f));
        lblPaid.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(lblPaid, g);

        g.gridx = 0;
        g.weightx = 0;
        g.gridy = 2;
        g.insets = new Insets(6, 0, 0, 12);
        g.anchor = GridBagConstraints.WEST;
        card.add(fieldLabel("Balance"), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.insets = new Insets(6, 0, 0, 0);
        g.anchor = GridBagConstraints.EAST;
        lblBalance.setFont(lblBalance.getFont().deriveFont(Font.BOLD, 16f));
        lblBalance.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(lblBalance, g);

        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(12f));
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    /**
     * One row in the items list: name + qty × unit on the left, subtotal on the
     * right.
     */
    private JPanel buildItemRow(OrderDAO.OrderItem it) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel name = new JLabel(it.service);
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setFont(name.getFont().deriveFont(Font.PLAIN, 13f));

        String qtyText = (it.qty == Math.floor(it.qty) && !Double.isInfinite(it.qty))
                ? String.valueOf((long) it.qty)
                : String.format("%.2f", it.qty);
        JLabel detail = new JLabel(qtyText + " × " + String.format("PHP %.2f", it.unitPrice));
        detail.setForeground(Theme.TEXT_MUTED);
        detail.setFont(detail.getFont().deriveFont(Font.PLAIN, 11f));

        left.add(name);
        left.add(Box.createVerticalStrut(2));
        left.add(detail);

        JLabel subtotal = new JLabel(String.format("PHP %.2f", it.subtotal));
        subtotal.setForeground(Theme.TEXT_PRIMARY);
        subtotal.setFont(subtotal.getFont().deriveFont(Font.BOLD, 13f));
        subtotal.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(left, BorderLayout.CENTER);
        row.add(subtotal, BorderLayout.EAST);

        // Prevent BoxLayout from stretching the row vertically
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    // ------------------------------------------------------------------ new order dialog
    private void openNewOrderDialog() {
        JComboBox<Item> cbCustomer = new JComboBox<>();
        JComboBox<Item> cbService = new JComboBox<>();
        JSpinner spQty = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 1000.0, 0.5));
        DefaultTableModel cartModel = new DefaultTableModel(
                new String[]{"Service", "Qty", "Unit Price", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        ArrayList<Item> cartServices = new ArrayList<>();
        JTable cartTable = new JTable(cartModel);
        JLabel cartTotal = new JLabel("PHP 0.00");
        JTextField tfPaid = new JTextField("0", 8);

        styleTable(cartTable);
        styleTextField(tfPaid);

        JLabel dlgStatus = new JLabel(" ");
        dlgStatus.setForeground(Theme.ERROR);
        dlgStatus.setFont(dlgStatus.getFont().deriveFont(12f));

        try {
            DefaultTableModel c = DB.query("SELECT id, name FROM customers ORDER BY name");
            for (int i = 0; i < c.getRowCount(); i++) {
                cbCustomer.addItem(new Item(((Number) c.getValueAt(i, 0)).intValue(),
                        String.valueOf(c.getValueAt(i, 1)), 0));
            }
            DefaultTableModel s = DB.query("SELECT id, name, pricing_type, price FROM services ORDER BY name");
            for (int i = 0; i < s.getRowCount(); i++) {
                double price = ((Number) s.getValueAt(i, 3)).doubleValue();
                String unit = "PER_KILO".equals(s.getValueAt(i, 2)) ? "/kg" : "/item";
                cbService.addItem(new Item(((Number) s.getValueAt(i, 0)).intValue(),
                        String.format("%s (PHP %.2f%s)", s.getValueAt(i, 1), price, unit), price));
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
            return;
        }

        styleCombo(cbCustomer);
        styleCombo(cbService);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("New Order");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Select a customer, add items, then save.");
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setFont(sub.getFont().deriveFont(12f));

        g.gridy = 0;
        g.insets = new Insets(0, 0, 4, 0);
        content.add(title, g);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 18, 0);
        content.add(sub, g);

        JPanel pickers = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pickers.setOpaque(false);
        pickers.add(fieldLabel("Customer"));
        cbCustomer.setPreferredSize(new Dimension(160, 32));
        pickers.add(cbCustomer);
        pickers.add(fieldLabel("Service"));
        cbService.setPreferredSize(new Dimension(220, 32));
        pickers.add(cbService);
        pickers.add(fieldLabel("Kg / Qty"));
        spQty.setPreferredSize(new Dimension(80, 32));
        pickers.add(spQty);

        AccentButton btnAddItem = new AccentButton("Add");
        AccentButton btnRemove = new AccentButton("Remove",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        btnRemove.setForeground(Theme.ACCENT);
        btnAddItem.setPreferredSize(new Dimension(80, 32));
        btnRemove.setPreferredSize(new Dimension(90, 32));
        pickers.add(btnAddItem);
        pickers.add(btnRemove);

        g.gridy = 2;
        g.insets = new Insets(0, 0, 12, 0);
        content.add(pickers, g);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        styleScroll(cartScroll);
        cartScroll.setPreferredSize(new Dimension(0, 200));
        g.gridy = 3;
        g.insets = new Insets(0, 0, 12, 0);
        content.add(cartScroll, g);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        cartTotal.setFont(cartTotal.getFont().deriveFont(Font.BOLD, 15f));
        cartTotal.setForeground(Theme.TEXT_PRIMARY);
        totalRow.add(cartTotal, BorderLayout.WEST);

        JPanel paidBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        paidBox.setOpaque(false);
        paidBox.add(fieldLabel("Paid now (PHP)"));
        tfPaid.setPreferredSize(new Dimension(100, 32));
        paidBox.add(tfPaid);
        totalRow.add(paidBox, BorderLayout.EAST);

        g.gridy = 4;
        g.insets = new Insets(0, 0, 6, 0);
        content.add(totalRow, g);

        g.gridy = 5;
        g.insets = new Insets(0, 0, 14, 0);
        content.add(dlgStatus, g);

        AccentButton saveBtn = new AccentButton("Save Order");
        AccentButton cancelBtn = new AccentButton("Cancel",
                Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
        cancelBtn.setForeground(Theme.ACCENT);
        saveBtn.setPreferredSize(new Dimension(140, 38));
        cancelBtn.setPreferredSize(new Dimension(110, 38));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        btnAddItem.addActionListener(e -> {
            Item s = (Item) cbService.getSelectedItem();
            if (s == null) {
                dlgStatus.setText("No services available.");
                return;
            }
            double qty = ((Number) spQty.getValue()).doubleValue();
            cartServices.add(s);
            cartModel.addRow(new Object[]{s.label, qty, s.price, round2(qty * s.price)});
            cartTotal.setText(String.format("PHP %.2f", cartTotal(cartModel)));
            dlgStatus.setText(" ");
        });
        btnRemove.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            cartServices.remove(row);
            cartModel.removeRow(row);
            cartTotal.setText(String.format("PHP %.2f", cartTotal(cartModel)));
        });

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "New Order",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel card = new RoundedCard(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel cardTop = new JPanel(new BorderLayout());
        cardTop.setOpaque(false);
        cardTop.add(content, BorderLayout.CENTER);
        cardTop.add(buttons, BorderLayout.SOUTH);

        card.add(cardTop, BorderLayout.CENTER);

        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(card);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setSize(Math.max(720, dialog.getWidth()), dialog.getHeight());
        dialog.setLocationRelativeTo(this);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            Item cust = (Item) cbCustomer.getSelectedItem();
            if (cust == null) {
                dlgStatus.setText("Select a customer (add one in the Customers tab).");
                return;
            }
            if (cartModel.getRowCount() == 0) {
                dlgStatus.setText("Add at least one item.");
                return;
            }
            double total = cartTotal(cartModel);
            double paid;
            try {
                paid = round2(Double.parseDouble(tfPaid.getText().trim()));
                if (paid < 0 || paid > total) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                dlgStatus.setText("Paid amount must be between 0 and the total.");
                return;
            }

            List<Object[]> rows = new ArrayList<>();
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                rows.add(new Object[]{
                    cartModel.getValueAt(i, 0),
                    cartModel.getValueAt(i, 1),
                    cartModel.getValueAt(i, 2),
                    cartModel.getValueAt(i, 3)});
            }

            int newOrderId;
            try {
                newOrderId = orderDAO.createOrder(cust.id, cartServices, rows, total, paid);
            } catch (SQLException ex) {
                DB.error(this, ex);
                return;
            }

            dialog.dispose();

            refreshOrders();
            selectOrderById(newOrderId);
            showReceipt(newOrderId);
        });

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.getRootPane().setDefaultButton(saveBtn);

        dialog.setVisible(true);
    }

    // ------------------------------------------------------------------ data
    public void refreshAll() {
        refreshOrders();
    }

    private void refreshOrders() {
        try {
            ordersTable.setModel(orderDAO.listOrders(statusFilter));
            styleTable(ordersTable);
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private int currentOrderId() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            return -1;
        }
        int modelRow = ordersTable.convertRowIndexToModel(row);
        Object v = ordersTable.getModel().getValueAt(modelRow, 0);
        return v instanceof Number ? ((Number) v).intValue() : -1;
    }

    private void selectOrderById(int id) {
        suppressSelectionEvents = true;
        try {
            for (int r = 0; r < ordersTable.getRowCount(); r++) {
                Object v = ordersTable.getValueAt(r, 0);
                if (v instanceof Number && ((Number) v).intValue() == id) {
                    ordersTable.setRowSelectionInterval(r, r);
                    ordersTable.scrollRectToVisible(ordersTable.getCellRect(r, 0, true));
                    break;
                }
            }
        } finally {
            suppressSelectionEvents = false;
        }
        loadOrderDetail(id);
    }

    private void loadOrderDetail(int orderId) {
        try {
            OrderDAO.OrderDetail d = orderDAO.findById(orderId);
            if (d == null) {
                detailCards.show(detailRoot, "empty");
                return;
            }

            detailTitle.setText("Order #" + d.id);
            detailCustomer.setText(d.customer);
            detailPhone.setText(d.phone.isEmpty() ? "—" : d.phone);
            detailDate.setText(d.date);
            statusPill.setStatus(d.status);

            suppressSelectionEvents = true;
            try {
                statusCombo.setSelectedItem(d.status);
            } finally {
                suppressSelectionEvents = false;
            }

            lblTotal.setText(String.format("PHP %.2f", d.total));
            lblPaid.setText(String.format("PHP %.2f", d.paid));
            lblBalance.setText(String.format("PHP %.2f", d.balance));
            lblBalance.setForeground(d.balance > 0.001 ? Theme.ERROR
                    : d.balance < -0.001 ? Theme.WARNING : Theme.SUCCESS);

            // ---- Line items ----
            itemsList.removeAll();
            List<OrderDAO.OrderItem> items = orderDAO.findItems(orderId);
            if (items.isEmpty()) {
                JLabel none = new JLabel("No items on this order.");
                none.setForeground(Theme.TEXT_MUTED);
                none.setBorder(new EmptyBorder(8, 8, 8, 8));
                itemsList.add(none);
            } else {
                for (OrderDAO.OrderItem it : items) {
                    itemsList.add(buildItemRow(it));
                }
            }
            itemsList.revalidate();
            itemsList.repaint();

            detailCards.show(detailRoot, "detail");
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    // ------------------------------------------------------------------ actions
    private void updateStatus(int id) {
        try {
            orderDAO.updateStatus(id, Objects.toString(statusCombo.getSelectedItem(), "RECEIVED"));
            refreshOrders();
            selectOrderById(id);
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private void addPayment() {
        int id = currentOrderId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }
        double balance;
        try {
            balance = orderDAO.getBalance(id);
        } catch (SQLException e) {
            DB.error(this, e);
            return;
        }
        if (balance <= 0.001) {
            JOptionPane.showMessageDialog(this, "This order is already fully paid.");
            return;
        }
        String s = JOptionPane.showInputDialog(this,
                String.format("Balance: PHP %.2f%nPayment amount:", balance),
                String.format("%.2f", balance));
        if (s == null) {
            return;
        }
        try {
            double amt = round2(Double.parseDouble(s.trim()));
            if (amt <= 0 || amt > balance + 0.001) {
                JOptionPane.showMessageDialog(this, "Amount must be between 0 and the balance.");
                return;
            }
            orderDAO.addPayment(id, amt);
            refreshOrders();
            selectOrderById(id);
            showReceipt(id);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount.");
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    // ------------------------------------------------------------------ receipt
    private void showReceipt(int orderId) {
        try {
            OrderDAO.OrderDetail d = orderDAO.findById(orderId);
            if (d == null) {
                return;
            }
            List<OrderDAO.OrderItem> items = orderDAO.findItems(orderId);

            JTextArea area = new JTextArea(buildReceipt(d, items));
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setEditable(false);
            area.setBackground(Theme.BG_INPUT);
            area.setForeground(Theme.TEXT_PRIMARY);
            area.setCaretColor(Theme.ACCENT);
            area.setBorder(new EmptyBorder(14, 16, 14, 16));

            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(460, 420));
            sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true));
            sp.getViewport().setBackground(Theme.BG_INPUT);

            JDialog dialog = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Receipt - Order #" + orderId,
                    java.awt.Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));

            JPanel card = new RoundedCard(new BorderLayout(10, 10));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));
            card.add(sp, BorderLayout.CENTER);

            AccentButton printBtn = new AccentButton("Print");
            AccentButton closeBtn = new AccentButton("Close",
                    Theme.ACCENT_SOFT, Theme.ACCENT_SOFT_HOVER);
            closeBtn.setForeground(Theme.ACCENT);
            printBtn.setPreferredSize(new Dimension(110, 36));
            closeBtn.setPreferredSize(new Dimension(110, 36));

            JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
            buttons.setOpaque(false);
            buttons.add(closeBtn);
            buttons.add(printBtn);
            card.add(buttons, BorderLayout.SOUTH);

            JPanel root = new JPanel(new GridBagLayout());
            root.setOpaque(false);
            root.setBorder(new EmptyBorder(16, 16, 16, 16));
            root.add(card);
            dialog.setContentPane(root);
            dialog.pack();
            dialog.setLocationRelativeTo(this);

            printBtn.addActionListener(e -> {
                try {
                    area.print();
                } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(dialog, "Print failed: " + ex.getMessage());
                }
            });
            closeBtn.addActionListener(e -> dialog.dispose());
            dialog.getRootPane().registerKeyboardAction(
                    e -> dialog.dispose(),
                    KeyStroke.getKeyStroke("ESCAPE"),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            dialog.setVisible(true);
        } catch (SQLException e) {
            DB.error(this, e);
        }
    }

    private String buildReceipt(OrderDAO.OrderDetail d, List<OrderDAO.OrderItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("        LAUNDRY SHOP\n");
        sb.append("------------------------------------\n");
        sb.append(String.format("Order #:  %d%n", d.id));
        sb.append(String.format("Date:     %s%n", d.date));
        sb.append(String.format("Customer: %s%n", d.customer));
        sb.append(String.format("Phone:    %s%n", d.phone.isEmpty() ? "-" : d.phone));
        sb.append(String.format("Status:   %s%n", d.status));
        sb.append("------------------------------------\n");
        for (OrderDAO.OrderItem it : items) {
            sb.append(String.format("%s%n", it.service));
            sb.append(String.format("   %.1f x %.2f = %.2f%n", it.qty, it.unitPrice, it.subtotal));
        }
        sb.append("------------------------------------\n");
        sb.append(String.format("TOTAL:    PHP %.2f%n", d.total));
        sb.append(String.format("PAID:     PHP %.2f%n", d.paid));
        sb.append(String.format("BALANCE:  PHP %.2f%n", d.balance));
        sb.append("------------------------------------\n");
        sb.append("      Thank you!\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ styling helpers
    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(2, 8, 2, 8)));
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
        t.setRowHeight(30);
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
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        t.setDefaultRenderer(Object.class, base);

        DefaultTableCellRenderer money = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = String.format("PHP %.2f", ((Number) value).doubleValue());
                }
                Component c = super.getTableCellRendererComponent(
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
        t.putClientProperty("moneyRenderer", money);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double cartTotal(DefaultTableModel m) {
        double t = 0;
        for (int i = 0; i < m.getRowCount(); i++) {
            t += ((Number) m.getValueAt(i, 3)).doubleValue();
        }
        return round2(t);
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

    private static class StatusPill extends JLabel {

        StatusPill() {
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD, 11f));
            setBorder(new EmptyBorder(4, 12, 4, 12));
            setStatus("RECEIVED");
        }

        void setStatus(String status) {
            Color bg, fg;
            switch (status == null ? "" : status.toUpperCase()) {
                case "WASHING":
                    bg = new Color(0xDBEAFE);
                    fg = new Color(0x1D4ED8);
                    break;
                case "READY":
                    bg = new Color(0xD1FAE5);
                    fg = new Color(0x047857);
                    break;
                case "CLAIMED":
                    bg = new Color(0xE5E7EB);
                    fg = new Color(0x374151);
                    break;
                default:
                    bg = new Color(0xFEF3C7);
                    fg = new Color(0xB45309);
                    break;
            }
            putClientProperty("pillBg", bg);
            putClientProperty("pillFg", fg);
            setForeground(fg);
            setText(status);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = (Color) getClientProperty("pillBg");
            if (bg != null) {
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        getHeight(), getHeight()));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class FilterChip extends JToggleButton {

        private final String status;

        FilterChip(String label, String status) {
            super(label);
            this.status = status;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, 11f));
            setBorder(new EmptyBorder(5, 12, 5, 12));
            setForeground(Theme.TEXT_SECONDARY);
        }

        String status() {
            return status;
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

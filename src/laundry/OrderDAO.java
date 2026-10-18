package laundry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Data access for orders, order items, and payments.
 * All SQL related to orders lives here so the UI stays query-free.
 */
public class OrderDAO {

    /** Lightweight carrier for a row from the orders list. */
    public static class OrderRow {
        public int id;
        public String customer;
        public String status;
        public double total;
        public double balance;
        public String date;
    }

    /** Carrier for the detail view of one order. */
    public static class OrderDetail {
        public int id;
        public String customer;
        public String phone;
        public String status;
        public double total;
        public double paid;
        public double balance;
        public String date;
    }

    /** One line item on an order. */
    public static class OrderItem {
        public String service;
        public double qty;
        public double unitPrice;
        public double subtotal;
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    /** Returns the orders list, optionally filtered by status.
     *  Pass null or "" for "all statuses". */
    public DefaultTableModel listOrders(String statusFilter) throws SQLException {
        String sql =
                "SELECT o.id AS ID, c.name AS Customer, o.status AS Status, "
                + "o.total AS Total, (o.total - o.paid) AS Balance, "
                + "DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') AS Date "
                + "FROM orders o JOIN customers c ON c.id = o.customer_id ";
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql += "WHERE o.status = ? ";
        }
        sql += "ORDER BY o.id DESC";

        if (statusFilter != null && !statusFilter.isEmpty()) {
            return DB.query(sql, statusFilter);
        }
        return DB.query(sql);
    }

    /** Fetches the header info for a single order, or null if it doesn't exist. */
    public OrderDetail findById(int orderId) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT o.id, c.name, c.phone, o.status, o.total, o.paid, "
                + "DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') "
                + "FROM orders o JOIN customers c ON c.id = o.customer_id "
                + "WHERE o.id = ?", orderId);
        if (m.getRowCount() == 0) return null;

        OrderDetail d = new OrderDetail();
        d.id = ((Number) m.getValueAt(0, 0)).intValue();
        d.customer = String.valueOf(m.getValueAt(0, 1));
        d.phone = m.getValueAt(0, 2) == null ? "" : String.valueOf(m.getValueAt(0, 2));
        d.status = String.valueOf(m.getValueAt(0, 3));
        d.total = ((Number) m.getValueAt(0, 4)).doubleValue();
        d.paid = ((Number) m.getValueAt(0, 5)).doubleValue();
        d.balance = d.total - d.paid;
        d.date = String.valueOf(m.getValueAt(0, 6));
        return d;
    }

    /** Returns the line items for an order. */
    public List<OrderItem> findItems(int orderId) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT s.name, i.qty, i.unit_price, i.subtotal "
                + "FROM order_items i JOIN services s ON s.id = i.service_id "
                + "WHERE i.order_id = ?", orderId);
        java.util.ArrayList<OrderItem> out = new java.util.ArrayList<>();
        for (int r = 0; r < m.getRowCount(); r++) {
            OrderItem it = new OrderItem();
            it.service = String.valueOf(m.getValueAt(r, 0));
            it.qty = ((Number) m.getValueAt(r, 1)).doubleValue();
            it.unitPrice = ((Number) m.getValueAt(r, 2)).doubleValue();
            it.subtotal = ((Number) m.getValueAt(r, 3)).doubleValue();
            out.add(it);
        }
        return out;
    }

    /** Convenience for the Add Payment dialog. */
    public double getBalance(int orderId) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT (total - paid) FROM orders WHERE id = ?", orderId);
        return ((Number) m.getValueAt(0, 0)).doubleValue();
    }

    // ------------------------------------------------------------------
    // Commands
    // ------------------------------------------------------------------

    public void updateStatus(int orderId, String status) throws SQLException {
        DB.update("UPDATE orders SET status=? WHERE id=?", status, orderId);
    }

    /** Inserts a new order with its items and optional initial payment.
     *  Returns the new order id. Runs in a single transaction. */
    public int createOrder(int customerId,
                           List<Item> services,
                           List<Object[]> cartRows,
                           double total,
                           double paid) throws SQLException {
        int newId;
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orders(customer_id, total, paid) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.setDouble(3, paid);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        k.next();
                        newId = k.getInt(1);
                    }
                }
                for (int i = 0; i < services.size(); i++) {
                    Object[] row = cartRows.get(i);
                    DB.exec(c, "INSERT INTO order_items(order_id, service_id, qty, unit_price, subtotal) "
                            + "VALUES(?,?,?,?,?)",
                            newId, services.get(i).id,
                            row[1], row[2], row[3]);
                }
                if (paid > 0) {
                    DB.exec(c, "INSERT INTO payments(order_id, amount) VALUES(?,?)", newId, paid);
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
        return newId;
    }

    /** Records a payment and updates the order total. Transactional. */
    public void addPayment(int orderId, double amount) throws SQLException {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                DB.exec(c, "INSERT INTO payments(order_id, amount) VALUES(?,?)", orderId, amount);
                DB.exec(c, "UPDATE orders SET paid = paid + ? WHERE id = ?", amount, orderId);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
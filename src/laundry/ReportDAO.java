package laundry;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * All reporting and dashboard queries. Read-only — no writes here.
 */
public class ReportDAO {

    /** Small KPI container. */
    public static class Kpis {
        public long orders;
        public double sales;
        public double collected;
        public double unpaid;
    }

    /** One row of the daily breakdown. */
    public static class DailyRow {
        public String date;
        public long orders;
        public double sales;
        public double collected;
        public double unpaid;
    }

    /** One row of the sales-by-service breakdown. */
    public static class ServiceRow {
        public String service;
        public long orders;
        public double qty;
        public double revenue;
    }

    /** One row of the recent-orders list on the dashboard. */
    public static class RecentRow {
        public int id;
        public String customer;
        public String status;
        public double total;
        public String when;
    }

    // ------------------------------------------------------------------
    // Reports (date-range)
    // ------------------------------------------------------------------

    public Kpis kpis(java.sql.Date from, java.sql.Date to) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT COUNT(*), COALESCE(SUM(total),0), COALESCE(SUM(paid),0) "
                + "FROM orders WHERE DATE(created_at) BETWEEN ? AND ?", from, to);
        Kpis k = new Kpis();
        k.orders = ((Number) m.getValueAt(0, 0)).longValue();
        k.sales = ((Number) m.getValueAt(0, 1)).doubleValue();
        k.collected = ((Number) m.getValueAt(0, 2)).doubleValue();
        k.unpaid = k.sales - k.collected;
        return k;
    }

    public List<DailyRow> dailyBreakdown(java.sql.Date from, java.sql.Date to) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT DATE(created_at), COUNT(*), SUM(total), SUM(paid), SUM(total - paid) "
                + "FROM orders WHERE DATE(created_at) BETWEEN ? AND ? "
                + "GROUP BY DATE(created_at) ORDER BY DATE(created_at) DESC", from, to);
        List<DailyRow> out = new ArrayList<>();
        for (int r = 0; r < m.getRowCount(); r++) {
            DailyRow d = new DailyRow();
            d.date = String.valueOf(m.getValueAt(r, 0));
            d.orders = ((Number) m.getValueAt(r, 1)).longValue();
            d.sales = ((Number) m.getValueAt(r, 2)).doubleValue();
            d.collected = ((Number) m.getValueAt(r, 3)).doubleValue();
            d.unpaid = ((Number) m.getValueAt(r, 4)).doubleValue();
            out.add(d);
        }
        return out;
    }

    public List<ServiceRow> salesByService(java.sql.Date from, java.sql.Date to) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT s.name, COUNT(DISTINCT i.order_id), SUM(i.qty), SUM(i.subtotal) "
                + "FROM order_items i "
                + "JOIN services s ON s.id = i.service_id "
                + "JOIN orders o ON o.id = i.order_id "
                + "WHERE DATE(o.created_at) BETWEEN ? AND ? "
                + "GROUP BY s.id, s.name ORDER BY SUM(i.subtotal) DESC", from, to);
        List<ServiceRow> out = new ArrayList<>();
        for (int r = 0; r < m.getRowCount(); r++) {
            ServiceRow s = new ServiceRow();
            s.service = String.valueOf(m.getValueAt(r, 0));
            s.orders = ((Number) m.getValueAt(r, 1)).longValue();
            s.qty = ((Number) m.getValueAt(r, 2)).doubleValue();
            s.revenue = ((Number) m.getValueAt(r, 3)).doubleValue();
            out.add(s);
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Dashboard
    // ------------------------------------------------------------------

    public Kpis todayKpis() throws SQLException {
        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
        return kpis(today, today);
    }

    public long countByStatus(String status) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT COUNT(*) FROM orders WHERE status = ?", status);
        return ((Number) m.getValueAt(0, 0)).longValue();
    }

    /** Daily totals for the last N days, oldest first — convenient for the chart. */
    public List<DailyRow> lastNDays(int n) throws SQLException {
        java.sql.Date to = java.sql.Date.valueOf(LocalDate.now());
        java.sql.Date from = java.sql.Date.valueOf(LocalDate.now().minusDays(n - 1));
        List<DailyRow> rows = dailyBreakdown(from, to);
        // Reverse so oldest is first for the chart's X axis
        java.util.Collections.reverse(rows);
        return rows;
    }

    public List<RecentRow> recentOrders(int limit) throws SQLException {
        DefaultTableModel m = DB.query(
                "SELECT o.id, c.name, o.status, o.total, "
                + "TIMESTAMPDIFF(MINUTE, o.created_at, NOW()) AS mins "
                + "FROM orders o JOIN customers c ON c.id = o.customer_id "
                + "ORDER BY o.id DESC LIMIT " + limit);
        List<RecentRow> out = new ArrayList<>();
        for (int r = 0; r < m.getRowCount(); r++) {
            RecentRow rr = new RecentRow();
            rr.id = ((Number) m.getValueAt(r, 0)).intValue();
            rr.customer = String.valueOf(m.getValueAt(r, 1));
            rr.status = String.valueOf(m.getValueAt(r, 2));
            rr.total = ((Number) m.getValueAt(r, 3)).doubleValue();
            long mins = ((Number) m.getValueAt(r, 4)).longValue();
            rr.when = humanAgo(mins);
            out.add(rr);
        }
        return out;
    }

    private static String humanAgo(long minutes) {
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        long days = hours / 24;
        return days + " day" + (days == 1 ? "" : "s") + " ago";
    }
}
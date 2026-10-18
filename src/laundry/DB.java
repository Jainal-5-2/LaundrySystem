package laundry;

import java.awt.Component;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class DB {

    // Change these to match your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/laundry_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Runs a SELECT and returns a read-only table model. */
    public static DefaultTableModel query(String sql, Object... params) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                DefaultTableModel m = new DefaultTableModel() {
                    @Override
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                };
                for (int i = 1; i <= n; i++) {
                    m.addColumn(md.getColumnLabel(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 1; i <= n; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    m.addRow(row);
                }
                return m;
            }
        }
    }

    /** Runs INSERT/UPDATE/DELETE on its own connection. */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection c = get()) {
            return exec(c, sql, params);
        }
    }

    /** Runs INSERT/UPDATE/DELETE on a given connection (for transactions). */
    public static int exec(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }

    public static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

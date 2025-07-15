package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class DatabaseConnector {

    private static String jdbcUrl;
    private static String username;
    private static String password;

    static {
        try {
            loadConfiguration();
        } catch (IOException e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
        }
    }

    private static void loadConfiguration() throws IOException {
        Properties props = new Properties();

        try (InputStream inputStream = DatabaseConnector.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
            } else {
                File file = new File("database.properties");
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        props.load(fis);
                    }
                } else {
                    throw new IOException("Cannot find database.properties in classpath or project root");
                }
            }
        }

        jdbcUrl = props.getProperty("jdbc.url");
        username = props.getProperty("jdbc.username");
        password = props.getProperty("jdbc.password");

        if (jdbcUrl == null || username == null || password == null) {
            throw new IOException("Database properties are not properly defined");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    public static ResultSet executeQuery(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            return rs;
        } catch (SQLException e) {
            closeResources(rs, stmt, conn);
            throw e;
        }
    }

    public static ResultSet executeQueryWithParams(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            rs = pstmt.executeQuery();

            return rs;
        } catch (SQLException e) {
            closeResources(rs, pstmt, conn);
            throw e;
        }
    }

    public static int executeUpdate(String sql) throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public static int executeUpdateWithParams(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate();
        }
    }

    public static long executeInsertAndReturnId(String sql, Object... params) throws SQLException {
        long generatedId = -1;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getLong(1);
                    }
                }
            }
        }
        return generatedId;
    }

    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // log the exception
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // log the exception
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // log the exception
            }
        }
    }
}

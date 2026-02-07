package com.skylink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {
    private static HikariDataSource ds;

    public static void initFromEnv() throws SQLException {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String db = System.getenv().getOrDefault("DB_NAME", "skylinkdb");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASS", "Sahil@2003");

        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port, db);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(4);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
        ensureSchema();
    }

    private static void ensureSchema() throws SQLException {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                    "booking_id VARCHAR(64) PRIMARY KEY, " +
                    "flight_number VARCHAR(64), " +
                    "seat_number VARCHAR(32), " +
                    "passenger_name VARCHAR(255), " +
                    "booking_date TIMESTAMP, " +
                    "cancelled BOOLEAN DEFAULT FALSE) ENGINE=InnoDB");
        }
    }

    public static void insertBooking(Booking booking) {
        if (ds == null) {
            // DB not configured/available; skip persistence
            System.err.println("Database not available — skipping insertBooking persistence");
            return;
        }
        String sql = "REPLACE INTO bookings(booking_id, flight_number, seat_number, passenger_name, booking_date, cancelled) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId());
            ps.setString(2, booking.getFlightNumber());
            ps.setString(3, booking.getSeatNumber());
            ps.setString(4, booking.getPassengerName());
            ps.setTimestamp(5, new Timestamp(booking.getBookingDate().getTime()));
            ps.setBoolean(6, booking.isCancelled());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void markCancelled(String bookingId) {
        if (ds == null) {
            System.err.println("Database not available — skipping markCancelled");
            return;
        }
        String sql = "UPDATE bookings SET cancelled = TRUE WHERE booking_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Booking> loadBookings() {
        List<Booking> result = new ArrayList<>();
        if (ds == null) {
            // No DB available — return empty list
            return result;
        }
        String sql = "SELECT booking_id, flight_number, seat_number, passenger_name, booking_date, cancelled FROM bookings WHERE cancelled = FALSE";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                String flight = rs.getString(2);
                String seat = rs.getString(3);
                String passenger = rs.getString(4);
                Date date = rs.getTimestamp(5);
                boolean cancelled = rs.getBoolean(6);
                result.add(new Booking(id, flight, seat, passenger, date, cancelled));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}

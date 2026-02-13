package com.university.parking.database;

import com.university.parking.model.Fine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {
    
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS fines (" +
                "fine_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "license_plate TEXT NOT NULL," +
                "reason TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "issued_at TEXT NOT NULL," +
                "is_paid INTEGER DEFAULT 0," +
                "ticket_id TEXT" +
                ");";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Fine table error: " + e.getMessage());
        }
    }

    public void insertFine(Fine fine) {
        String sql = "INSERT INTO fines(license_plate, reason, amount, issued_at, is_paid, ticket_id) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fine.getLicensePlate());
            pstmt.setString(2, fine.getReason());
            pstmt.setDouble(3, fine.getAmount());
            pstmt.setString(4, fine.getIssuedAt().toString());
            pstmt.setInt(5, fine.isPaid() ? 1 : 0);
            pstmt.setString(6, fine.getTicketId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Fine> getUnpaidFines(String licensePlate) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines WHERE license_plate = ? AND is_paid = 0";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Fine fine = new Fine(
                        rs.getString("license_plate"),
                        rs.getString("reason"),
                        rs.getDouble("amount"),
                        rs.getString("ticket_id")
                );
                fine.setFineId(rs.getInt("fine_id"));
                fine.setPaid(rs.getInt("is_paid") == 1);
                fines.add(fine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }

    public void markFineAsPaid(int fineId) {
        String sql = "UPDATE fines SET is_paid = 1 WHERE fine_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fineId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Fine> getAllUnpaidFines() {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT * FROM fines WHERE is_paid = 0";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Fine fine = new Fine(
                        rs.getString("license_plate"),
                        rs.getString("reason"),
                        rs.getDouble("amount"),
                        rs.getString("ticket_id")
                );
                fine.setFineId(rs.getInt("fine_id"));
                fine.setPaid(false);
                fines.add(fine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }
}

package com.university.parking.database;

import com.university.parking.model.Fine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public void insertFine(Fine fine) {
        String sql = "INSERT OR IGNORE INTO fines(license_plate, reason, amount, issued_at, is_paid, ticket_id, scheme_used) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("Inserting fine for: " + fine.getLicensePlate());

            pstmt.setString(1, fine.getLicensePlate());
            pstmt.setString(2, fine.getReason());
            pstmt.setDouble(3, fine.getAmount());
            pstmt.setString(4, fine.getIssuedAt().toString());
            pstmt.setInt(5, fine.isPaid() ? 1 : 0);
            pstmt.setString(6, fine.getTicketId());
            pstmt.setString(7, fine.getSchemeUsed()); 
            pstmt.executeUpdate();

            int result = pstmt.executeUpdate();
            System.out.println("Insert result: " + result);

            if (result > 0) {
                System.out.println("✅ Fine inserted successfully!");
            } else {
                System.out.println("⚠️ Fine not inserted (可能已存在重复记录)");
            }

        } catch (SQLException e) {
            System.out.println("❌ INSERT FAILED! Error: " + e.getMessage());
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
                        rs.getString("ticket_id"),
                        rs.getString("scheme_used") 
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
                        rs.getString("ticket_id"),
                        rs.getString("scheme_used")  
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
package com.university.parking.database;

import java.sql.*;

public class TicketDAO {

    public void createTicket(String spotId, String plate, String vType, long entryTime) {
        String sql = "INSERT INTO tickets(spot_id, license_plate, vehicle_type, entry_time) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, spotId);
            pstmt.setString(2, plate);
            pstmt.setString(3, vType);
            pstmt.setLong(4, entryTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Find active ticket (not paid yet) for a plate
    public ResultSet findActiveTicket(String plate) {
        String sql = "SELECT * FROM tickets WHERE license_plate = ? AND is_paid = 0";
        try {
            Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plate);
            return pstmt.executeQuery(); // Caller must close connection
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void markTicketPaid(int ticketId) {
        String sql = "UPDATE tickets SET is_paid = 1 WHERE ticket_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ticketId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
package com.university.parking.database;

import com.university.parking.model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    
    public void insertPayment(Payment payment) {
        String sql = "INSERT INTO payments(license_plate, amount, payment_method, payment_time, cash_tendered, change_amount, ticket_id) " +
                     "VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, payment.getLicensePlate());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setString(3, payment.getPaymentMethod());
            pstmt.setString(4, payment.getPaymentTime().toString());
            pstmt.setDouble(5, payment.getCashTendered());
            pstmt.setDouble(6, payment.getChangeAmount());
            pstmt.setInt(7, payment.getTicketId());
            int result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Payment> getPaymentsByPlate(String licensePlate) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE license_plate = ? ORDER BY payment_time DESC";
        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Payment payment = new Payment(
                    rs.getString("license_plate"),
                    rs.getDouble("amount"),
                    rs.getString("payment_method"),
                    rs.getInt("ticket_id")
                );
                payment.setPaymentId(rs.getInt("payment_id"));
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public double getTotalParkingRevenue() {
        String sql = "SELECT SUM(amount) FROM payments";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            System.out.println("Error getting parking revenue: " + e.getMessage());
            return 0.0;
        }
    }

    public double getParkingRevenueByPeriod(String startDate, String endDate) {
        String sql = "SELECT SUM(amount) FROM payments WHERE payment_time BETWEEN ? AND ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}

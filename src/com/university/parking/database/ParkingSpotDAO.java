package com.university.parking.database;

import com.university.parking.structure.ParkingSpot;
import com.university.parking.structure.SpotType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpotDAO {

    
    public void saveAllSpots(List<ParkingSpot> allSpots) {
        String sql = "INSERT OR REPLACE INTO parking_spots(spot_id, type, is_occupied, hourly_rate) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (ParkingSpot spot : allSpots) {
                pstmt.setString(1, spot.getSpotId());
                pstmt.setString(2, spot.getType().toString());
                pstmt.setInt(3, spot.isOccupied() ? 1 : 0);
                pstmt.setDouble(4, spot.getHourlyRate()); 
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("All spots saved to database successfully.");
        } catch (SQLException e) {
            System.out.println("Save Error: " + e.getMessage());
        }
    }

    public void updateSpotStatus(String spotId, boolean occupied) {
        String sql = "UPDATE parking_spots SET is_occupied = ? WHERE spot_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, occupied ? 1 : 0);
            pstmt.setString(2, spotId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update Error: " + e.getMessage());
        }
    }
}
package com.university.parking.logic;

import com.university.parking.database.DatabaseManager;
import com.university.parking.database.ParkingSpotDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.structure.SpotType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ParkingService {
    private ParkingSpotDAO spotDAO = new ParkingSpotDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private FineStrategy fineStrategy = new FixedFineStrategy(); // Default strategy

    // Entry Logic
    public String parkVehicle(String plate, String vehicleType) {
        // 1. Determine required spot type
        SpotType requiredType = SpotType.REGULAR;
        if (vehicleType.equalsIgnoreCase("Motorcycle")) requiredType = SpotType.COMPACT;
        if (vehicleType.equalsIgnoreCase("SUV") || vehicleType.equalsIgnoreCase("Truck")) requiredType = SpotType.REGULAR;
        
        // 2. Find available spot (Simplified: searching DB directly)
        String foundSpotId = findAvailableSpotId(requiredType.toString());
        
        if (foundSpotId == null) return "NO_SPOTS";

        // 3. Occupy Spot
        spotDAO.updateSpotStatus(foundSpotId, true);

        // 4. Generate Ticket
        ticketDAO.createTicket(foundSpotId, plate, vehicleType, System.currentTimeMillis());

        return foundSpotId;
    }

    // Exit Logic
    public String processExit(String plate) {
        try {
            ResultSet rs = ticketDAO.findActiveTicket(plate);
            if (rs != null && rs.next()) {
                int ticketId = rs.getInt("ticket_id");
                String spotId = rs.getString("spot_id");
                long entryTime = rs.getLong("entry_time");
                
                // Calculate Duration
                long durationMillis = System.currentTimeMillis() - entryTime;
                long hours = (long) Math.ceil(durationMillis / (1000.0 * 60 * 60)); 
                if (hours == 0) hours = 1; // Minimum 1 hour

                // Calculate Fee (Fetch rate from Spot ID - simplfied here for demo)
                // In production, fetch specific rate for spot_id from DB. Assuming 5.0 for now.
                double rate = getRateForSpot(spotId); 
                double parkingFee = hours * rate;

                // Calculate Fine (Strategy Pattern)
                double fine = fineStrategy.calculateFine(hours);

                double total = parkingFee + fine;

                // Close DB resources
                rs.getStatement().getConnection().close();

                return String.format(
                    "Ticket ID: %d\nSpot: %s\nDuration: %d hours\nParking Fee: RM %.2f\nFine: RM %.2f\nTOTAL DUE: RM %.2f",
                    ticketId, spotId, hours, parkingFee, fine, total
                );
            }
            return "Vehicle not found.";
        } catch (SQLException e) {
            return "Error processing exit.";
        }
    }

    public void completePayment(String plate) {
        try {
            ResultSet rs = ticketDAO.findActiveTicket(plate);
            if (rs != null && rs.next()) {
                String spotId = rs.getString("spot_id");
                int ticketId = rs.getInt("ticket_id");
                
                // Free the spot
                spotDAO.updateSpotStatus(spotId, false);
                // Mark ticket paid
                ticketDAO.markTicketPaid(ticketId);
                
                rs.getStatement().getConnection().close();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Helper to find spot ID from DB
    private String findAvailableSpotId(String type) {
        // Simple query to get first available spot of type
        String sql = "SELECT spot_id FROM parking_spots WHERE type = ? AND is_occupied = 0 LIMIT 1";
        try (java.sql.Connection conn = DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("spot_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    private double getRateForSpot(String spotId) {
         // Logic to fetch rate from DB based on spotId
         return 5.0; // Default fallback
    }
}
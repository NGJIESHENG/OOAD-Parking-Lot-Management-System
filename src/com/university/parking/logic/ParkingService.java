package com.university.parking.logic;

import com.university.parking.database.DatabaseManager;
import com.university.parking.database.ParkingSpotDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.structure.SpotType;
import com.university.parking.structure.Ticket; // Import the new class
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingService {
    private ParkingSpotDAO spotDAO = new ParkingSpotDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private FineStrategy fineStrategy = new FixedFineStrategy();

    public String parkVehicle(String plate, String vehicleType) {
        SpotType requiredType = SpotType.REGULAR;
        if (vehicleType.equalsIgnoreCase("Motorcycle")) requiredType = SpotType.COMPACT;
        if (vehicleType.equalsIgnoreCase("SUV") || vehicleType.equalsIgnoreCase("Truck")) requiredType = SpotType.REGULAR;
        
        String foundSpotId = findAvailableSpotId(requiredType.toString());
        
        if (foundSpotId == null) return "NO_SPOTS";

        spotDAO.updateSpotStatus(foundSpotId, true);
        ticketDAO.createTicket(foundSpotId, plate, vehicleType, System.currentTimeMillis());

        return foundSpotId;
    }

    public String processExit(String plate) {
        Ticket ticket = ticketDAO.findActiveTicket(plate);
            
        if (ticket != null) {
            long durationMillis = System.currentTimeMillis() - ticket.getEntryTime();
            long hours = (long) Math.ceil(durationMillis / (1000.0 * 60 * 60)); 
            if (hours == 0) hours = 1;

            double rate = 5.0; // Default rate
            double parkingFee = hours * rate;
            double fine = fineStrategy.calculateFine(hours);
            double total = parkingFee + fine;

            return String.format(
                "Ticket ID: %d\nSpot: %s\nDuration: %d hours\nParking Fee: RM %.2f\nFine: RM %.2f\nTOTAL DUE: RM %.2f",
                ticket.getTicketId(), ticket.getSpotId(), hours, parkingFee, fine, total
            );
        }
        return "Vehicle not found.";
    }

    public void completePayment(String plate) {
        Ticket ticket = ticketDAO.findActiveTicket(plate);
        
        if (ticket != null) {
            spotDAO.updateSpotStatus(ticket.getSpotId(), false);
            ticketDAO.markTicketPaid(ticket.getTicketId());
        }
    }

    private String findAvailableSpotId(String type) {
        String sql = "SELECT spot_id FROM parking_spots WHERE type = ? AND is_occupied = 0 LIMIT 1";
        try (java.sql.Connection conn = DatabaseManager.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("spot_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
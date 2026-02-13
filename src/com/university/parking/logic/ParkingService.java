package com.university.parking.logic;

import com.university.parking.database.DatabaseManager;
import com.university.parking.database.ParkingSpotDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.model.Ticket;
import com.university.parking.model.VehicleType;  
import com.university.parking.structure.SpotType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ParkingService {
    private ParkingSpotDAO spotDAO = new ParkingSpotDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private FineStrategy fineStrategy = new FixedFineStrategy();

    public String parkVehicle(String plate, String typeStr) {
        if (ticketDAO.findActiveTicket(plate) != null) {
            return "ALREADY_PARKED";
        }

        VehicleType vType;
        try {
            vType = VehicleType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "INVALID_TYPE";
        }

        String foundSpotId = findSuitableSpot(vType);
        if (foundSpotId == null) return "NO_SPOTS";

        spotDAO.updateSpotStatus(foundSpotId, true);
        ticketDAO.createTicket(foundSpotId, plate, vType.toString(), System.currentTimeMillis());

        return foundSpotId;
    }

    public String processExit(String plate) {
        Ticket ticket = ticketDAO.findActiveTicket(plate);
            
        if (ticket != null) {
            long durationMillis = System.currentTimeMillis() - ticket.getEntryTime();
            long hours = (long) Math.ceil(durationMillis / (1000.0 * 60 * 60)); 
            if (hours == 0) hours = 1;

            double rate = 5.0; 
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

    private String findSuitableSpot(VehicleType vType) {
        String sql = "SELECT spot_id, type FROM parking_spots WHERE is_occupied = 0";
        try (java.sql.Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String spotId = rs.getString("spot_id");
                SpotType spotType = SpotType.valueOf(rs.getString("type"));
                
                if (vType.canParkIn(spotType)) {
                    return spotId;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
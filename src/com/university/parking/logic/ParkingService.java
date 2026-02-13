package com.university.parking.logic;

import com.university.parking.database.DatabaseManager;
import com.university.parking.database.FineDAO;
import com.university.parking.database.ParkingSpotDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.model.Ticket;
import com.university.parking.model.VehicleType; 
import com.university.parking.structure.SpotType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ParkingService {
    private ParkingSpotDAO spotDAO = new ParkingSpotDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private FineDAO fineDAO = new FineDAO();
    private FineManager fineManager;
    private FineStrategy fineStrategy = new FixedFineStrategy();

    public ParkingService() {
        this.fineManager = new FineManager();
        this.fineStrategy = new FixedFineStrategy();
        FineDAO.createTable();  
        fineManager.startOverstayDetection(); 
    }

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
        if (ticket == null) return "Vehicle not found.";

        long durationMillis = System.currentTimeMillis() - ticket.getEntryTime();
        long hours = (long) Math.ceil(durationMillis / (1000.0 * 60 * 60)); 
        if (hours == 0) hours = 1;


        double rate = getRateForSpot(ticket.getSpotId());
        double parkingFee = hours * rate;

        if (hours > 24) {
            fineManager.issueFine(plate, "OVERSTAY (over 24 hours)", hours, String.valueOf(ticket.getTicketId()));
        }
            
        double unpaidFines = fineManager.getTotalUnpaidFines(plate);
        double total = parkingFee + unpaidFines;

        return String.format(
            "Parking Bill\n" +
            "License Plate Number: %s\n" +
            "Parking Location: %s\n" +
            "Parking Duration: %d 小时\n" +
            "Parking Fee: RM %.2f\n" +
            "Unpaid Fine: RM %.2f\n" +
            "------------------------\n" +
            "Total Payable: RM %.2f\n" +
            "=============================",
            plate, ticket.getSpotId(), hours, parkingFee, unpaidFines, total
        );
        
    }

    private double getRateForSpot(String spotId) {
        String sql = "SELECT hourly_rate FROM parking_spots WHERE spot_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, spotId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("hourly_rate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 5.0;
    }

    public void completePayment(String plate) {
        Ticket ticket = ticketDAO.findActiveTicket(plate);
        if (ticket != null) {
            spotDAO.updateSpotStatus(ticket.getSpotId(), false);
            ticketDAO.markTicketPaid(ticket.getTicketId());
            fineManager.payAllFines(plate);
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
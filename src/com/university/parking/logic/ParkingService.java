package com.university.parking.logic;

import com.university.parking.database.DatabaseManager;
import com.university.parking.database.FineDAO;
import com.university.parking.database.ParkingSpotDAO;
import com.university.parking.database.PaymentDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.Payment;
import com.university.parking.model.Ticket;
import com.university.parking.model.VehicleType;
import com.university.parking.structure.SpotType;
import java.sql.*;
import java.util.List;

public class ParkingService {
    private ParkingSpotDAO spotDAO = new ParkingSpotDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private FineDAO fineDAO = new FineDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private FineManager fineManager;

    public ParkingService() {
        this.fineManager = new FineManager(); 
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

        String currentTicketId = String.valueOf(ticket.getTicketId());

        boolean hasOverstayFine = false;
        List<Fine> existingFines = fineDAO.getUnpaidFines(plate); 
        System.out.println("Found " + existingFines.size() + " unpaid fines for " + plate);

        for (Fine fine : existingFines) {
            String fineTicketId = fine.getTicketId(); 
            System.out.println("Fine ticket ID: '" + fineTicketId + "', current ticket: '" + currentTicketId + "'");
            
            if (fineTicketId != null && fineTicketId.trim().equals(currentTicketId) && 
                fine.getReason().contains("OVERSTAY")) {
                hasOverstayFine = true;
                System.out.println("Found existing overstay fine for this ticket!");  
                break;
            }
        }

        if (hours > 24 && !hasOverstayFine) {
            System.out.println("Issuing new fine for ticket: " + currentTicketId);
            fineManager.issueFine(plate, "OVERSTAY (Exceeded 24 hours)", hours, currentTicketId);
        }
        
        double unpaidFines = fineManager.getTotalUnpaidFines(plate);
        double total = parkingFee + unpaidFines;

        return String.format(
            "========== PARKING BILL ==========\n" +
            "License Plate: %s\n" +
            "Parking Spot: %s\n" +
            "Duration: %d hours\n" +
            "Parking Fee: RM %.2f\n" +
            "Total Fines: RM %.2f (Scheme: %s)\n" +
            "------------------------\n" +
            "TOTAL DUE: RM %.2f\n" +
            "==================================",
            plate, ticket.getSpotId(), hours, parkingFee, 
            unpaidFines, fineManager.getCurrentStrategyName(),
            total
        );
    }

    public String processPayment(String plate, String paymentMethod, double cashTendered) {
        Ticket ticket = ticketDAO.findActiveTicket(plate);
        if (ticket == null) return "Vehicle not found.";
        
        long durationMillis = System.currentTimeMillis() - ticket.getEntryTime();
        long hours = (long) Math.ceil(durationMillis / (1000.0 * 60 * 60));
        if (hours == 0) hours = 1;
        
        double rate = getRateForSpot(ticket.getSpotId());
        double parkingFee = hours * rate;
        double unpaidFines = fineManager.getTotalUnpaidFines(plate);
        double total = parkingFee + unpaidFines;
     
        Payment payment = new Payment(plate, total, paymentMethod, ticket.getTicketId());
        
        if (paymentMethod.equals("CASH")) {
            payment.setCashPayment(cashTendered);
            if (cashTendered < total) {
                return "Insufficient Cash! Amount Due: RM" + String.format("%.2f", total) + 
                       ", Tendered: RM" + String.format("%.2f", cashTendered);
            }
        }
        
        paymentDAO.insertPayment(payment);
  
        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("           PAYMENT RECEIPT              \n");
        receipt.append("═══════════════════════════════════════\n");
        receipt.append(String.format("License Plate: %s\n", plate));
        receipt.append(String.format("Entry Time: %s\n", new java.util.Date(ticket.getEntryTime()).toString()));
        receipt.append(String.format("Exit Time: %s\n", new java.util.Date().toString()));
        receipt.append(String.format("Duration: %d hours\n", hours));
        receipt.append(String.format("Rate: RM %.2f/hour\n", rate));
        receipt.append(String.format("Parking Fee: RM %.2f (%d × RM %.2f)\n", parkingFee, hours, rate));
        receipt.append(String.format("Unpaid Fines: RM %.2f\n", unpaidFines));
        receipt.append("───────────────────────────────────────\n");
        receipt.append(String.format("TOTAL PAID: RM %.2f\n", total));
        receipt.append(String.format("Payment Method: %s\n", paymentMethod.equals("CASH") ? "CASH" : "CREDIT CARD"));
        
        if (paymentMethod.equals("CASH")) {
            receipt.append(String.format("Cash Tendered: RM %.2f\n", payment.getCashTendered()));
            receipt.append(String.format("Change: RM %.2f\n", payment.getChangeAmount()));
        }
        
        receipt.append(String.format("Payment Time: %s\n", payment.getPaymentTime().toString()));
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("      THANK YOU! DRIVE SAFELY          \n");
        receipt.append("═══════════════════════════════════════\n");
        
        return receipt.toString();
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
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String spotId = rs.getString("spot_id");
                SpotType spotType = SpotType.valueOf(rs.getString("type"));
                if (vType.canParkIn(spotType)) {
                    return spotId;
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
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

    public void setFineStrategy(String strategyType) {
        fineManager.setFineStrategy(strategyType);
    }

    public String getCurrentFineStrategyName() {
        return fineManager.getCurrentStrategyName();
    }
}
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

    private long calculateDuration(long entryTime) {
        long durationMillis = System.currentTimeMillis() - entryTime;
        long hours = (long) Math.ceil(durationMillis / (1000.0 * 60 * 60));
        if (hours == 0) hours = 1;
        return hours;
    }

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
            "Hourly Rate: RM %.2f\n" +
            "Parking Fee: RM %.2f\n" +
            "Total Fines: RM %.2f (Scheme: %s)\n" +
            "------------------------\n" +
            "TOTAL DUE: RM %.2f\n" +
            "==================================",
            plate, ticket.getSpotId(), hours, rate, 
            parkingFee,
            unpaidFines, fineManager.getCurrentStrategyName(),
            total
        );
    }

    public String processPayment(String plate, String paymentMethod, double cashTendered, boolean payFines) {
        System.out.println("\n========== PROCESSING PAYMENT ==========");
        System.out.println("Plate: " + plate);
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("Cash Tendered: RM" + cashTendered);
        System.out.println("Pay Fines: " + payFines);

        Ticket ticket = ticketDAO.findActiveTicket(plate);
        if (ticket == null) {
            return "Vehicle not found.";
        }
        System.out.println("✅ Ticket found: ID=" + ticket.getTicketId() + ", Spot=" + ticket.getSpotId());
            
        long hours = calculateDuration(ticket.getEntryTime());

        double rate = getRateForSpot(ticket.getSpotId());
        double parkingFee = hours * rate;
        
        double total = parkingFee;
        double finesAmount = fineManager.getTotalUnpaidFines(plate);
        
        String currentStrategy = fineManager.getCurrentStrategyType();
        
        // Option A and B
        if (currentStrategy.equals("FIXED") || currentStrategy.equals("PROGRESSIVE")) {
            total += finesAmount;
            payFines = true; 
        }
        // Option C
        else if (currentStrategy.equals("HOURLY")) {
            if (payFines) {
                total += finesAmount;
            }
        }

        double change = 0.0;
        if (paymentMethod.equals("CASH")) {
            if (cashTendered < total) {
                return "Insufficient Cash! Need RM" + String.format("%.2f", total) + 
                    ", Tendered: RM" + String.format("%.2f", cashTendered);
            }
            change = cashTendered - total;
        }

    Payment payment = new Payment(plate, parkingFee, paymentMethod, ticket.getTicketId());
        
        if (paymentMethod.equals("CASH")) {
            payment.setCashPayment(cashTendered);
            System.out.println("Cash payment: Tendered RM" + cashTendered + ", Change RM" + payment.getChangeAmount());
        }

        paymentDAO.insertPayment(payment);
        System.out.println("✅ Payment inserted to database");
        
        // Pay fines if selected
        if (payFines && finesAmount > 0) {
            fineManager.payAllFines(plate);
            System.out.println("✅ Fines paid for plate: " + plate);
        }
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════════════\n");
        receipt.append("           PAYMENT RECEIPT              \n");
        receipt.append("═══════════════════════════════════════\n");
        receipt.append(String.format("License Plate: %s\n", plate));
        receipt.append(String.format("Entry Time: %s\n", new java.util.Date(ticket.getEntryTime()).toString()));
        receipt.append(String.format("Exit Time: %s\n", new java.util.Date().toString()));
        receipt.append(String.format("Duration: %d hours\n", hours));
        receipt.append(String.format("Parking Fee: RM%.2f\n", parkingFee));
        
        if (finesAmount > 0) {
            if (payFines) {
                receipt.append(String.format("Fines Paid: RM%.2f\n", finesAmount));
            } else {
                receipt.append(String.format("⚠️ Fines Outstanding: RM%.2f (Will be charged next visit)\n", finesAmount));
            }
        }
        receipt.append(String.format("Subtotal: RM%.2f\n", total));
        receipt.append("───────────────────────────────────────\n");
        
        if (paymentMethod.equals("CASH")) {
            receipt.append(String.format("Cash Tendered: RM%.2f\n", cashTendered));
            receipt.append(String.format("Change: RM%.2f\n", change));
        }
        
        receipt.append(String.format("Total Paid: RM%.2f\n", total));
        receipt.append(String.format("Payment Method: %s\n", paymentMethod));
        receipt.append("═══════════════════════════════════════\n");
        
        return receipt.toString();
    }

    public void completePayment(String plate) {
        Ticket ticket = ticketDAO.findActiveTicket(plate);
        if (ticket != null) {
            spotDAO.updateSpotStatus(ticket.getSpotId(), false);
            ticketDAO.markTicketPaid(ticket.getTicketId());
        }
    }

    public double getTotalUnpaidFines(String plate) {
        return fineManager.getTotalUnpaidFines(plate);
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
                double rate = rs.getDouble("hourly_rate");
                System.out.println("💰 Rate for spot " + spotId + ": RM" + rate);
                return rate;
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
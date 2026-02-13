package com.university.parking.logic;

import com.university.parking.database.FineDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.model.Fine;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FineManager {
    private FineStrategy strategy;
    private final FineDAO fineDAO;
    private final TicketDAO ticketDAO;
    private Timer timer;
    private String currentStrategyName;

    public FineManager() {
        this.fineDAO = new FineDAO();
        this.ticketDAO = new TicketDAO();
        this.strategy = new FixedFineStrategy();
        this.currentStrategyName = "Fixed penalty of RM50";  
    }

    public void setFineStrategy(String strategyType) {
        String oldStrategyName = this.currentStrategyName;
        
        switch (strategyType) {
            case "FIXED":
                this.strategy = new FixedFineStrategy();
                this.currentStrategyName = "Fixed penalty of RM50";
                break;
            case "PROGRESSIVE":
                this.strategy = new ProgressiveFineStrategy();
                this.currentStrategyName = "Progressive (incremental fines)";
                break;
            case "HOURLY":
                this.strategy = new HourlyFineStrategy();
                this.currentStrategyName = "Hourly (RM20 per hour)";
                break;
            default:
                this.strategy = new FixedFineStrategy();
                this.currentStrategyName = "Fixed penalty of RM50";
                break;
        }
        
        System.out.println("========== STRATEGY SWITCH ==========");
        System.out.println("Switched from: " + oldStrategyName);
        System.out.println("Switched to: " + currentStrategyName);
        System.out.println("Strategy object: " + this.strategy.getClass().getSimpleName());
        System.out.println("=====================================");
    }

    public String getCurrentStrategyName() {
        return currentStrategyName;
    }

    public String getCurrentStrategyType() {
        if (strategy == null) {
            return "FIXED";
        }
        
        if (strategy instanceof FixedFineStrategy) {
            return "FIXED";
        } else if (strategy instanceof ProgressiveFineStrategy) {
            return "PROGRESSIVE";
        } else if (strategy instanceof HourlyFineStrategy) {
            return "HOURLY";
        } else {
            return "FIXED";
        }
    }

    public FineStrategy getCurrentStrategy() {
        return this.strategy;
    }

    public static String[] getAvailableStrategies() {
        return new String[]{"FIXED", "PROGRESSIVE", "HOURLY"};
    }

    public void startOverstayDetection() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAllActiveTickets();
            }
        }, 0, 60 * 60 * 1000);
    }

    public void stopDetection() {
        if (timer != null) timer.cancel();
    }

    private void checkAllActiveTickets() {
    }

    public synchronized void issueFine(String licensePlate, String reason, long durationHours, String ticketId) {
        List<Fine> existingFines = fineDAO.getUnpaidFines(licensePlate);
        for (Fine fine : existingFines) {
            String fineTicketId = fine.getTicketId();
            if (fineTicketId != null && fineTicketId.trim().equals(ticketId) && 
                fine.getReason().contains("OVERSTAY")) {
                System.out.println("Fine already issued for ticket: " + ticketId + 
                                 " (ID: " + fine.getFineId() + ")");
                System.out.println("=====================================");
                return; 
            }
        }
        
        double amount = strategy.calculateFine(durationHours);
        System.out.println("Calculated amount: RM" + amount + " using " + strategy.getClass().getSimpleName());
        
        if (amount > 0) {
            Fine fine = new Fine(licensePlate, reason, amount, ticketId, getCurrentStrategyName());
            fineDAO.insertFine(fine);
            System.out.println("Fine issued: " + licensePlate + " - RM" + amount + 
                             " (Scheme: " + getCurrentStrategyName() + ")");
        } else {
            System.out.println("No fine issued - amount is 0");
        }
        System.out.println("=====================================");
    }

    public double getTotalUnpaidFines(String licensePlate) {
        double total = fineDAO.getUnpaidFines(licensePlate).stream()
                .mapToDouble(Fine::getAmount)
                .sum();
        System.out.println("🔍 Getting unpaid fines for " + licensePlate + ": RM" + total);
        return total;
    }

    public void payAllFines(String licensePlate) {
        System.out.println("💰 Paying all fines for: " + licensePlate);
        for (Fine fine : fineDAO.getUnpaidFines(licensePlate)) {
            fineDAO.markFineAsPaid(fine.getFineId());
            System.out.println("  - Paid fine ID: " + fine.getFineId() + ", Amount: RM" + fine.getAmount());
        }
    }

    public FineDAO getFineDAO() { return fineDAO; }
}
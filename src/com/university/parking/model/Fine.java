package com.university.parking.model;

import java.time.LocalDateTime;

public class Fine {
    private int fineId;
    private String licensePlate;
    private String reason;
    private double amount;
    private LocalDateTime issuedAt;
    private boolean isPaid;
    private String ticketId;
    private String schemeUsed;

    public Fine(String licensePlate, String reason, double amount, String ticketId, String schemeUsed) {
        this.licensePlate = licensePlate;
        this.reason = reason;
        this.amount = amount;
        this.ticketId = ticketId;
        this.schemeUsed = schemeUsed;  
        this.issuedAt = LocalDateTime.now();
        this.isPaid = false;
    }
    
    public Fine(String licensePlate, String reason, double amount, String ticketId) {
        this(licensePlate, reason, amount, ticketId, "UNKNOWN");  
    }

    // Getters and Setters
    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }
    public String getLicensePlate() { return licensePlate; }
    public String getReason() { return reason; }
    public double getAmount() { return amount; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public String getTicketId() { return ticketId; }
    public String getSchemeUsed() { return schemeUsed; }
    public void setSchemeUsed(String schemeUsed) { this.schemeUsed = schemeUsed; }
}
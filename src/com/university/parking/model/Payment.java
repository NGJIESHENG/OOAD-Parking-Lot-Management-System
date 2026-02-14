package com.university.parking.model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private String licensePlate;
    private double amount;
    private String paymentMethod; 
    private LocalDateTime paymentTime;
    private double cashTendered;    
    private double changeAmount;  
    private int ticketId;        
    
    public Payment(String licensePlate, double amount, String paymentMethod, int ticketId) {
        this.licensePlate = licensePlate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.ticketId = ticketId;
        this.paymentTime = LocalDateTime.now();
        this.cashTendered = 0;
        this.changeAmount = 0;
    }
    
    public void setCashPayment(double cashTendered) {
        this.cashTendered = cashTendered;
        this.changeAmount = cashTendered - amount;
        this.paymentMethod = "CASH";
    }
    
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public String getLicensePlate() { return licensePlate; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public double getCashTendered() { return cashTendered; }
    public double getChangeAmount() { return changeAmount; }
    public int getTicketId() { return ticketId; }
}

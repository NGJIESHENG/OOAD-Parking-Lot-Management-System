package com.university.parking.logic;

import com.university.parking.database.FineDAO;
import com.university.parking.database.TicketDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.Ticket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FineManager {
    private FineStrategy strategy;
    private final FineDAO fineDAO;
    private final TicketDAO ticketDAO;
    private Timer timer;

    public FineManager() {
        this.fineDAO = new FineDAO();
        this.ticketDAO = new TicketDAO();
        this.strategy = new FixedFineStrategy();
    }

    public void setFineStrategy(FineStrategy strategy) {
        this.strategy = strategy;
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

    public void issueFine(String licensePlate, String reason, long durationHours, String ticketId) {
        double amount = strategy.calculateFine(durationHours);
        if (amount > 0) {
            Fine fine = new Fine(licensePlate, reason, amount, ticketId);
            fineDAO.insertFine(fine);
            System.out.println("Fine issued: " + licensePlate + " - RM" + amount);
        }
    }

    public double getTotalUnpaidFines(String licensePlate) {
        return fineDAO.getUnpaidFines(licensePlate).stream()
                .mapToDouble(Fine::getAmount)
                .sum();
    }

    public void payAllFines(String licensePlate) {
        for (Fine fine : fineDAO.getUnpaidFines(licensePlate)) {
            fineDAO.markFineAsPaid(fine.getFineId());
        }
    }

    public FineDAO getFineDAO() { return fineDAO; }
}

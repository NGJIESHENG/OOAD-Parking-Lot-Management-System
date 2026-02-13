package com.university.parking.structure;

public class Ticket {
    private int ticketId;
    private String spotId;
    private String plate;
    private String vehicleType;
    private long entryTime;

    public Ticket(int ticketId, String spotId, String plate, String vehicleType, long entryTime) {
        this.ticketId = ticketId;
        this.spotId = spotId;
        this.plate = plate;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
    }

    public int getTicketId() { return ticketId; }
    public String getSpotId() { return spotId; }
    public long getEntryTime() { return entryTime; }
}
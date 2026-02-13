package com.university.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Vehicle {
    private String licensePlate;
    private VehicleType type;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
    }

    public void setExitTime() {
        this.exitTime = LocalDateTime.now();
    }

    public long getDurationInHours() {
        if (exitTime == null) return 0;
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        return (long) Math.ceil(minutes / 60.0);
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
}
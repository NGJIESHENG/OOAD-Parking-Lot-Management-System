package com.university.parking.structure;

public class ParkingSpot {
    private String spotId;
    private SpotType type;
    private boolean isOccupied;
    private double hourlyRate;
    private Vehicle currentVehicle; // Uncommented and utilized

    public ParkingSpot(String spotId, SpotType type, double hourlyRate){
        this.spotId = spotId;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
        this.currentVehicle = null;
    }

    public boolean park(Vehicle vehicle) {
        if (isOccupied) return false;
        this.currentVehicle = vehicle;
        this.isOccupied = true;
        return true;
    }

    public double leave() {
        if (!isOccupied || currentVehicle == null) return 0.0;
        
        currentVehicle.setExitTime();
        long hours = currentVehicle.getDurationInHours();
        double finalRate;

        // Requirement: Handicapped vehicles get RM2/hour flat rate regardless of spot
        if (currentVehicle.getType() == VehicleType.HANDICAPPED) {
            finalRate = 2.0;
        } else {
            finalRate = this.hourlyRate;
        }

        double fee = hours * finalRate;
        
        // Reset spot
        this.currentVehicle = null;
        this.isOccupied = false;
        
        return fee;
    }

    public boolean isOccupied() { return isOccupied; }
    public String getSpotId() { return this.spotId; }
    public SpotType getType() { return this.type; }
    public double getHourlyRate() { return this.hourlyRate; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
}
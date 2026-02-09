package com.university.parking.structure;

public class ParkingSpot{
    private String spotId;
    private SpotType type;
    private boolean isOccupied;
    private double hourlyRate;
    //private Vehicle currentVehicle;

    public ParkingSpot (String spotId, SpotType type, double hourlyRate){
        this.spotId = spotId;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this .isOccupied = false;
    }

    public boolean isOccupied (){
        return isOccupied;
    }

    public void setOccupied (boolean occupied){
        this.isOccupied = occupied;
    }

}
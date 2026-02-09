package com.university.parking.structure;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private String name;
    private List<Floor> floors;

    public ParkingLot(String name, int numberofFloors){
        this.name = name;
        this.floors = new ArrayList<>();

        for (int i = 1; i <= numberofFloors; i++){
            floors.add(new Floor(i));
        }
    }

    public double getOccupancyRate(){
        int totalSpots = 0;
        int occupiedSpots = 0;

        for (Floor floor : floors){
            for (ParkingSpot spot : floor.getSpots()) {
                totalSpots++;
                if (spot.isOccupied()){
                    occupiedSpots++;
                }
            }
        }
        return totalSpots == 0 ? 0 : ((double) occupiedSpots / totalSpots ) * 100;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public ParkingSpot findSpotById(String spotId){
        for (Floor floor : floors) {
            for (ParkingSpot spot: floor.getSpots()){
                if (spot.getSpotId().equals(spotId)){
                    return spot;
                }
            }
        }
        return null;
    }
}

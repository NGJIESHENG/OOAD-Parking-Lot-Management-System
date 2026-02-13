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
            Floor floor = new Floor(i);

            // Note: Fixed logical nesting from original file for cleaner structure
            for (int r = 1; r <= 20; r++) floor.addSpot(new ParkingSpot("F"+ i + "-R" + r, SpotType.REGULAR, 5.0));
            for (int c = 1; c <= 5; c++) floor.addSpot(new ParkingSpot("F"+ i + "-C" + c, SpotType.COMPACT, 2.0));
            for (int h = 1; h <= 3; h++) floor.addSpot(new ParkingSpot("F" + i + "-H" + h, SpotType.HANDICAPPED, 2.0));
            for (int res = 1; res <= 2; res++) floor.addSpot(new ParkingSpot("F" + i + "-RES" + res, SpotType.RESERVED, 10.0));
            
            this.floors.add(floor);
        }
    }

    // New method: Logic to park a vehicle based on type constraints
    public ParkingSpot parkVehicle(Vehicle vehicle) {
        ParkingSpot availableSpot = null;

        // Strategy based on Vehicle Type
        switch (vehicle.getType()) {
            case MOTORCYCLE:
                // Motorcycles -> Compact only
                availableSpot = findFirstAvailableSpot(SpotType.COMPACT);
                break;
            case CAR:
                // Cars -> Compact first (cheaper/smaller), then Regular
                availableSpot = findFirstAvailableSpot(SpotType.COMPACT);
                if (availableSpot == null) {
                    availableSpot = findFirstAvailableSpot(SpotType.REGULAR);
                }
                break;
            case SUV:
            case TRUCK:
                // SUV/Truck -> Regular only
                availableSpot = findFirstAvailableSpot(SpotType.REGULAR);
                break;
            case HANDICAPPED:
                // Handicapped -> Try Handicapped spot first, then any other
                availableSpot = findFirstAvailableSpot(SpotType.HANDICAPPED);
                if (availableSpot == null) availableSpot = findFirstAvailableSpot(SpotType.COMPACT);
                if (availableSpot == null) availableSpot = findFirstAvailableSpot(SpotType.REGULAR);
                if (availableSpot == null) availableSpot = findFirstAvailableSpot(SpotType.RESERVED);
                break;
        }

        if (availableSpot != null) {
            availableSpot.park(vehicle);
            return availableSpot;
        } else {
            System.out.println("No suitable spot found for " + vehicle.getType());
            return null;
        }
    }

    // Helper to find spot by type
    private ParkingSpot findFirstAvailableSpot(SpotType type) {
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (!spot.isOccupied() && spot.getType() == type) {
                    return spot;
                }
            }
        }
        return null;
    }

    // Existing methods below...
    public double getOccupancyRate(){
        int totalSpots = 0;
        int occupiedSpots = 0;
        for (Floor floor : floors){
            for (ParkingSpot spot : floor.getSpots()) {
                totalSpots++;
                if (spot.isOccupied()) occupiedSpots++;
            }
        }
        return totalSpots == 0 ? 0 : ((double) occupiedSpots / totalSpots ) * 100;
    }

    public List<Floor> getFloors() { return floors; }
    
    public ParkingSpot findSpotById(String spotId){
        for (Floor floor : floors) {
            for (ParkingSpot spot: floor.getSpots()){
                if (spot.getSpotId().equals(spotId)) return spot;
            }
        }
        return null;
    }
}
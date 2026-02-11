package com.university.parking;

import com.university.parking.database.DatabaseManager;
import com.university.parking.structure.ParkingLot;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing system...");
        DatabaseManager.createNewTable();

        ParkingLot myLot = new ParkingLot("Unviversity Parking", 5);
        System.out.println("Structure created: " + myLot.getFloors().size() + " floors.");

        System.out.printf("Current occupancy: %.2f%%\n", myLot.getOccupancyRate());
    }
}
    


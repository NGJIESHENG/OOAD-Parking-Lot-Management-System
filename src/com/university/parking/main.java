package com.university.parking;


import com.university.parking.database.DatabaseManager;
import com.university.parking.database.ParkingSpotDAO;
import com.university.parking.structure.Floor;
import com.university.parking.structure.ParkingLot;
import com.university.parking.structure.ParkingSpot;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {
        System.out.println("Initializing system...");
        DatabaseManager.initializeDatabase();

        ParkingLot myLot = new ParkingLot("University Parking", 5);
        System.out.println("Structure created: " + myLot.getFloors().size() + " floors.");

        System.out.printf("Current occupancy: %.2f%%\n", myLot.getOccupancyRate());

        List<ParkingSpot> allSpots = new ArrayList<>();
        for (Floor floor : myLot.getFloors()){
            allSpots.addAll(floor.getSpots());
        }

        ParkingSpotDAO dao = new ParkingSpotDAO();
        dao.saveAllSpots(allSpots);
    }
}
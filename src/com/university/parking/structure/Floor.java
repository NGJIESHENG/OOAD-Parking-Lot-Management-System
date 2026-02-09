package com.university.parking.structure;

import java.util.ArrayList;
import java.util.List;
public class Floor {
    private int floorNumber;
    private List<ParkingSpot> spots;

    public Floor (int floorNumber){
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }

    public void addSpot(ParkingSpot spot){
        spots.add(spot);
    }

    public List<ParkingSpot> getSpots(){
        return this.spots;
    }

    public int getFloorNumber(){
        return floorNumber;
    }

    
}

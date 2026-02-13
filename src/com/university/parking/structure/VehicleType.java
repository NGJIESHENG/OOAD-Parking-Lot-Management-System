package com.university.parking.structure;

public enum VehicleType {
    MOTORCYCLE,
    CAR,
    SUV,
    TRUCK,
    HANDICAPPED;

    // Helper to check if a vehicle fits in a specific spot type based on assignment rules
    public boolean canParkIn(SpotType spotType) {
        switch (this) {
            case MOTORCYCLE:
                return spotType == SpotType.COMPACT;
            case CAR:
                return spotType == SpotType.COMPACT || spotType == SpotType.REGULAR;
            case SUV:
            case TRUCK:
                return spotType == SpotType.REGULAR;
            case HANDICAPPED:
                return true; // Can park anywhere
            default:
                return false;
        }
    }
}
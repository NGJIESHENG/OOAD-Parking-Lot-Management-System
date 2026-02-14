package com.university.parking.logic;

public interface FineStrategy {
    double calculateFine(long durationHours);
    double calculateReservedFine(long durationHours);
}
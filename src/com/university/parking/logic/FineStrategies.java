package com.university.parking.logic;

// Option A: Fixed
class FixedFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        return (durationHours > 24) ? 50.0 : 0.0;
    }
}

// Option C: Hourly (Simplest implementation for brevity)
class HourlyFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        if (durationHours <= 24) return 0.0;
        return (durationHours - 24) * 20.0;
    }
}
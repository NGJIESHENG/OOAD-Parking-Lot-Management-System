package com.university.parking.logic;

// Option A: Fixed
class FixedFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        return (durationHours > 24) ? 50.0 : 0.0;
    }
}

// Option B: Progressive 
class ProgressiveFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        if (durationHours <= 24) return 0.0;
        
        double fine = 50.0; // 24-48
        
        if (durationHours > 48) {
            fine += 100.0; // 48-72
        }
        if (durationHours > 72) {
            fine += 150.0; // 72-96
        }
        if (durationHours > 96) {
            fine += 200.0; // >96
        }
        return fine;
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
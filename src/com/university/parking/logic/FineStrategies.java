package com.university.parking.logic;

// Option A: Fixed
class FixedFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        return (durationHours > 24) ? 50.0 : 0.0;
    }
    
    @Override
    public double calculateReservedFine(long durationHours) {
        return 50.0; 
    }
}

// Option B: Progressive 
class ProgressiveFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        if (durationHours <= 24) return 0.0;
        
        long extraHours = durationHours - 24;
        long numberOfFines = (extraHours + 23) / 24;
        
        double totalFine = 0.0;
        for (int i = 1; i <= numberOfFines; i++) {
            if (i == 1) totalFine += 50.0;
            else if (i == 2) totalFine += 100.0;
            else if (i == 3) totalFine += 150.0;
            else totalFine += 200.0;
        }
        return totalFine;
    }
    
    @Override
    public double calculateReservedFine(long durationHours) {
        long numberOfFines = (durationHours + 23) / 24;
        
        double totalFine = 0.0;
        for (int i = 1; i <= numberOfFines; i++) {
            if (i == 1) totalFine += 50.0;
            else if (i == 2) totalFine += 100.0;
            else if (i == 3) totalFine += 150.0;
            else totalFine += 200.0;
        }
        return totalFine;
    }
}

// Option C: Hourly
class HourlyFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
        if (durationHours <= 24) return 0.0;
        long overstayHours = durationHours - 24;
        return overstayHours * 20.0;
    }
    
    @Override
    public double calculateReservedFine(long durationHours) {
        return durationHours * 20.0;
    }
}
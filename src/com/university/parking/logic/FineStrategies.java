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
        
        double fine = 50.0; // First 24 hours
        
        if (durationHours > 24) {
            fine += 100.0;
        }
        
        if (durationHours > 48) {
            fine += 150.0;
        }
        
        if (durationHours > 72) {
            fine += 200.0;
        }
        
        System.out.println("Progressive fine calculation: " + durationHours + 
                         " hours -> RM" + fine);
        return fine;
    }
}

// Option C: Hourly
class HourlyFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long durationHours) {
         if (durationHours <= 24) return 0.0;
        
        long overstayHours = durationHours - 24;
        double fine = overstayHours * 20.0;
        
        System.out.println("Hourly fine calculation: " + durationHours + 
                         " hours (overstay: " + overstayHours + "h) -> RM" + fine);
        return fine;
    }
}
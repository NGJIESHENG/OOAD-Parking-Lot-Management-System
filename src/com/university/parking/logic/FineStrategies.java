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
        
        // Calculate how many complete 24-hour periods beyond the first 24 hours
        long extraHours = durationHours - 24;
        long numberOfFines = (extraHours + 23) / 24; // Round up to get number of fines
        
        double totalFine = 0.0;
        
        for (int i = 1; i <= numberOfFines; i++) {
            if (i == 1) { // First fine (25-48 hours)
                totalFine += 50.0;
                System.out.println("Fine #" + i + ": RM50 (25-48 hours)");
            } else if (i == 2) { // Second fine (49-72 hours)
                totalFine += 100.0;
                System.out.println("Fine #" + i + ": RM100 (49-72 hours)");
            } else if (i == 3) { // Third fine (73-96 hours)
                totalFine += 150.0;
                System.out.println("Fine #" + i + ": RM150 (73-96 hours)");
            } else { // Fourth fine and beyond (>96 hours)
                totalFine += 200.0;
                System.out.println("Fine #" + i + ": RM200 (>96 hours)");
            }
        }
        
        System.out.println("Total duration: " + durationHours + " hours, Number of fines: " + numberOfFines + 
                         ", Total fine: RM" + totalFine);
        return totalFine;
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
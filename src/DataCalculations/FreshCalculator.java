package DataCalculations;

import Storage.FreshnessLvl;
import java.time.LocalDate;

/**
 * Calculates and assigns freshness levels based on calculations from expiry date
 * updates product statuses(FRESH ,....)
 */

public class FreshCalculator {
    public static FreshnessLvl calculate(LocalDate currentDate, LocalDate expiryDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(currentDate, expiryDate);
        
        if (daysBetween <= 0) {
            return FreshnessLvl.EXPIRED;
        } else if (daysBetween <= 3) {
            return FreshnessLvl.ROTATE;
        } else {
            return FreshnessLvl.FRESH;
        }
    }
}
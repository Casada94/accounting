package asadaclan.accounting.entities;

import java.time.Year;

public class YearlyReport {
    private final Year year;

    public YearlyReport(Year year) {
        this.year = year;
    }

    public Year year() {
        return year;
    }
}

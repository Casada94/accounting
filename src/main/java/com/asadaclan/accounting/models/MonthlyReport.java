package asadaclan.accounting.entities;

import java.time.YearMonth;

public class MonthlyReport {
    private final YearMonth period;

    public MonthlyReport(YearMonth period) {
        this.period = period;
    }

    public YearMonth period() {
        return period;
    }
}

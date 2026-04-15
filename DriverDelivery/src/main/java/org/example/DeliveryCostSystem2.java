package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

class DeliveryCostSystem2 {

    private static final BigDecimal SECONDS_IN_HOUR = BigDecimal.valueOf(3600);

    // DriverId -> Driver
    private final Map<String, Driver> drivers = new HashMap<>();

    // Last paid timestamp
    private long lastPaidTime = 0;

    // ================= APIs =================

    public void addDriver(String driverId, BigDecimal hourlyRate) {
        if (driverId == null || hourlyRate == null) return;
        drivers.putIfAbsent(driverId, new Driver(driverId, hourlyRate));
    }

    public void recordDelivery(String driverId, long start, long end) {
        if (start >= end || start < 0) return;

        Driver driver = drivers.get(driverId);
        if (driver == null) return;

        driver.addInterval(start, end);

        // Maintain merged intervals to avoid double-pay
        driver.mergeIntervals();
    }

    public BigDecimal getTotalCost() {
        BigDecimal total = BigDecimal.ZERO;

        for (Driver driver : drivers.values()) {
            total = total.add(driver.computeCost(0, Long.MAX_VALUE));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public void payUpTo(long timestamp) {
        if (timestamp <= lastPaidTime) return;
        lastPaidTime = timestamp;
    }

    public BigDecimal getTotalUnpaid() {
        BigDecimal total = BigDecimal.ZERO;

        for (Driver driver : drivers.values()) {
            total = total.add(driver.computeCost(lastPaidTime, Long.MAX_VALUE));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public int getMaxActiveDrivers(long start, long end) {
        List<Event> events = new ArrayList<>();

        for (Driver driver : drivers.values()) {
            for (Interval interval : driver.intervals) {
                long s = Math.max(interval.start, start);
                long e = Math.min(interval.end, end);

                if (s < e) {
                    events.add(new Event(s, +1));
                    events.add(new Event(e, -1));
                }
            }
        }

        events.sort((a, b) -> {
            if (a.time == b.time) return a.type - b.type;
            return Long.compare(a.time, b.time);
        });

        int active = 0, max = 0;

        for (Event e : events) {
            active += e.type;
            max = Math.max(max, active);
        }

        return max;
    }

    // ================= Internal Classes =================

    static class Driver {
        String id;
        BigDecimal hourlyRate;
        List<Interval> intervals = new ArrayList<>();

        Driver(String id, BigDecimal rate) {
            this.id = id;
            this.hourlyRate = rate;
        }

        void addInterval(long start, long end) {
            intervals.add(new Interval(start, end));
        }

        void mergeIntervals() {
            if (intervals.isEmpty()) return;

            intervals.sort(Comparator.comparingLong(i -> i.start));

            List<Interval> merged = new ArrayList<>();
            Interval prev = intervals.get(0);

            for (int i = 1; i < intervals.size(); i++) {
                Interval curr = intervals.get(i);

                if (curr.start <= prev.end) {
                    prev.end = Math.max(prev.end, curr.end);
                } else {
                    merged.add(prev);
                    prev = curr;
                }
            }
            merged.add(prev);

            intervals = merged;
        }

        BigDecimal computeCost(long from, long to) {
            BigDecimal total = BigDecimal.ZERO;

            for (Interval interval : intervals) {
                long start = Math.max(interval.start, from);
                long end = Math.min(interval.end, to);

                if (start >= end) continue;

                long seconds = end - start;

                BigDecimal hours = BigDecimal.valueOf(seconds)
                        .divide(SECONDS_IN_HOUR, 10, RoundingMode.HALF_UP);

                total = total.add(hourlyRate.multiply(hours));
            }

            return total;
        }
    }

    static class Interval {
        long start;
        long end;

        Interval(long s, long e) {
            this.start = s;
            this.end = e;
        }
    }

    static class Event {
        long time;
        int type; // +1 start, -1 end

        Event(long t, int type) {
            this.time = t;
            this.type = type;
        }
    }
}

package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

class Interval {
    long start;
    long end;

    Interval(long s, long e) {
        this.start = s;
        this.end = e;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }
}

class Util {

    // ✅ Insert interval ONLY if no overlap
    public static List<Interval> insertIfNoOverlap(List<Interval> slots, long start, long end) {

        Interval newInterval = new Interval(start, end);

        // 🔴 Check overlap
        for (Interval curr : slots) {
            if (!(newInterval.end <= curr.start || newInterval.start >= curr.end)) {
                throw new IllegalArgumentException(
                        "Overlap detected between " + newInterval + " and " + curr
                );
            }
        }

        // ✅ Insert in sorted order
        List<Interval> result = new ArrayList<>();
        boolean inserted = false;

        for (Interval curr : slots) {
            if (!inserted && newInterval.start < curr.start) {
                result.add(newInterval);
                inserted = true;
            }
            result.add(curr);
        }

        if (!inserted) result.add(newInterval);

        return result;
    }

    // ✅ Build prefix sum
    public static List<Long> buildPrefix(List<Interval> intervals) {
        List<Long> prefix = new ArrayList<>();
        long sum = 0;
        for (Interval i : intervals) {
            sum += (i.end - i.start);
            prefix.add(sum);
        }
        return prefix;
    }

    // ✅ Optimized unpaid calculation (same as yours)
    public static long getIntervalAfterTimestampOptimized(
            List<Interval> intervals,
            List<Long> prefix,
            long timestamp
    ) {
        int n = intervals.size();
        if (n == 0) return 0;

        int left = 0, right = n - 1, idx = n;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (intervals.get(mid).end > timestamp) {
                idx = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        if (idx == n) return 0;

        Interval first = intervals.get(idx);
        long partial = first.end - Math.max(first.start, timestamp);

        long remaining = 0;
        if (idx + 1 < n) {
            long total = prefix.get(n - 1);
            long before = prefix.get(idx);
            remaining = total - before;
        }

        return partial + remaining;
    }
}

class Driver {
    String id;
    BigDecimal hourlyRate;
    List<Interval> slots = new ArrayList<>();
    List<Long> prefixSeconds = new ArrayList<>();
    BigDecimal totalCost = BigDecimal.ZERO;

    Driver(String id, BigDecimal rate) {
        this.id = id;
        this.hourlyRate = rate;
    }
}

public class NonOverlapping {

    HashMap<String, Driver> drivers = new HashMap<>();
    BigDecimal totalCost = BigDecimal.ZERO;
    long paidInterval = 0;

    public void addDriver(String driverId, BigDecimal hourlyRate) {
        if (driverId.isBlank() || hourlyRate.compareTo(BigDecimal.ZERO) <= 0) return;
        if (drivers.containsKey(driverId)) return;

        drivers.put(driverId, new Driver(driverId, hourlyRate));
    }

    public void recordDelivery(String driverId, long start, long end) {
        if (!drivers.containsKey(driverId) || start > end) return;

        Driver driver = drivers.get(driverId);

        // 🔥 Key change: NO MERGE → only insert if valid
        List<Interval> updatedSlots = Util.insertIfNoOverlap(driver.slots, start, end);

        driver.slots = updatedSlots;
        driver.prefixSeconds = Util.buildPrefix(updatedSlots);

        // ✅ Cost calculation
        long totalSeconds = driver.prefixSeconds.isEmpty() ? 0 :
                driver.prefixSeconds.get(driver.prefixSeconds.size() - 1);

        BigDecimal newCost = BigDecimal.valueOf(totalSeconds)
                .divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP)
                .multiply(driver.hourlyRate);

        BigDecimal delta = newCost.subtract(driver.totalCost);

        driver.totalCost = newCost;
        totalCost = totalCost.add(delta);
    }

    public BigDecimal getTotalCost() {
        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    public void payUpTo(long timestamp) {
        if (timestamp > paidInterval) {
            paidInterval = timestamp;
        }
    }

    public BigDecimal getTotalUnpaid() {
        BigDecimal unpaid = BigDecimal.ZERO;

        for (Driver driver : drivers.values()) {
            long duration = Util.getIntervalAfterTimestampOptimized(
                    driver.slots,
                    driver.prefixSeconds,
                    paidInterval
            );

            BigDecimal hours = BigDecimal.valueOf(duration)
                    .divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP);

            unpaid = unpaid.add(hours.multiply(driver.hourlyRate));
        }

        return unpaid.setScale(2, RoundingMode.HALF_UP);
    }
}
package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class Interval {
    long start;
    long end;

    Interval(long s, long e) {
        this.start = s;
        this.end = e;
    }
}

class MergedInterval {
    List<Interval> intervals;
    long totalIntervals;
    List<Long> prefixSeconds;

    public MergedInterval(List<Interval> intervals, long totalIntervals, List<Long> prefixSeconds) {
        this.intervals = intervals;
        this.totalIntervals = totalIntervals;
        this.prefixSeconds = prefixSeconds;
    }
}

class Util {
    public static MergedInterval mergeInterval(List<Interval> slots, long start, long end) {
        List<Interval> mergedInterval = new ArrayList<>();
        List<Long> prefixSeconds = new ArrayList<>();

        // getting intervals before start
        int i = 0;
        long totalIntervals = 0;
        while(i < slots.size() && slots.get(i).end < start) {
            totalIntervals += slots.get(i).end - slots.get(i).start;
            prefixSeconds.add(totalIntervals);
            mergedInterval.add(slots.get(i++));
        }

        if (i == slots.size()) {
            mergedInterval.add(new Interval(start, end));
            totalIntervals += end - start;
            prefixSeconds.add(totalIntervals);

            return new MergedInterval(mergedInterval, totalIntervals, prefixSeconds);
        }

        // merging intervals
        Interval newInterval = new Interval(start, end);
        Interval curr = slots.get(i);
        while(curr.start < newInterval.end) {
            newInterval.start = Math.min(newInterval.start, curr.start);
            newInterval.end = Math.max(newInterval.end, curr.end);
            ++i;
            if (i == slots.size()) {
                mergedInterval.add(newInterval);
                totalIntervals += newInterval.end - newInterval.start;
                prefixSeconds.add(totalIntervals);
                return new MergedInterval(mergedInterval, totalIntervals, prefixSeconds);
            }
            curr = slots.get(i);
        }

        // After
        mergedInterval.add(newInterval);
        totalIntervals += newInterval.end - newInterval.start;
        prefixSeconds.add(totalIntervals);

        while(i < slots.size()) {
            totalIntervals += slots.get(i).end - slots.get(i).start;
            prefixSeconds.add(totalIntervals);
            mergedInterval.add(slots.get(i++));
        }

        return new MergedInterval(mergedInterval, totalIntervals, prefixSeconds);
    }

    public static long getIntervalAfterTimestamp(List<Interval> intervals, List<Long> prefix, long timestamp) {
        long totalInterval = 0;
//        System.out.println("Driver change!");
        timestamp++;
        for (Interval interval : intervals) {
//            System.out.println("interval: " + interval.start + ", " + interval.end + ", " + timestamp);
            if (interval.end > timestamp) {
                totalInterval += interval.end - Math.max(interval.start, timestamp);
//                prefix.add(totalInterval);
            }
        }
        return totalInterval;
    }

    public static long getIntervalAfterTimestampOptimized(
            List<Interval> intervals,
            List<Long> prefix,
            long timestamp
    ) {
        int n = intervals.size();
        if (n == 0) return 0;

        // 🔍 Binary search: first interval with end > timestamp
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

        // ✅ Partial overlap from first interval
        Interval first = intervals.get(idx);
        long partial = first.end - Math.max(first.start, timestamp);

        // ✅ Remaining full intervals using prefix sum
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

    public List<Long> getPrefixSeconds() {
        return prefixSeconds;
    }

    public void setPrefixSeconds(List<Long> prefixSeconds) {
        this.prefixSeconds = prefixSeconds;
    }

    List<Long> prefixSeconds;

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    BigDecimal totalCost = BigDecimal.ZERO;

    public List<Interval> getSlots() {
        return slots;
    }

    public void setSlots(List<Interval> slots) {
        this.slots = slots;
    }

    List<Interval> slots;

    Driver(String id, BigDecimal rate) {
        this.id = id;
        this.hourlyRate = rate;
        this.slots = new ArrayList<>();
        this.prefixSeconds = new ArrayList<>();
    }

    private void rebuildPrefix() {
        prefixSeconds = new ArrayList<>();
        long sum = 0;
        for (Interval interval : slots) {
            sum += (interval.end - interval.start);
            prefixSeconds.add(sum);
        }
    }
}


public class DeliverySystem {
    HashMap<String, Driver> drivers = new HashMap<>();
    BigDecimal totalCost = BigDecimal.ZERO;
    long paidInterval = 0;

    public void addDriver(String driverId, BigDecimal hourlyRate) {
        if (driverId.isBlank()) return;
        Driver driver = drivers.get(driverId);
        if (hourlyRate.compareTo(BigDecimal.ZERO) <= 0) return;
        if (Objects.nonNull(driver)) return;
        drivers.put(driverId, new Driver(driverId, hourlyRate));
    }

    public void recordDelivery(String driverId, long start, long end) {
        if (drivers.get(driverId) == null) return;
        if (start > end) return;
        Driver driver = drivers.get(driverId);
        List<Interval> existingSlots = driver.getSlots();
        MergedInterval interval = Util.mergeInterval(existingSlots, start, end);
        drivers.get(driverId).setSlots(interval.intervals);
        BigDecimal intervalInHours = BigDecimal.valueOf(interval.totalIntervals).divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP);
        BigDecimal existingCost = driver.getTotalCost();
        BigDecimal newCost = intervalInHours.multiply(driver.hourlyRate);
        driver.setTotalCost(newCost);
        driver.setPrefixSeconds(interval.prefixSeconds);
        BigDecimal delta = newCost.subtract(existingCost);
        totalCost = totalCost.add(delta);
    }

    public BigDecimal getTotalCost() {
        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    public void payUpTo(long timestamp) {
        if (paidInterval > timestamp) return;
        this.paidInterval = timestamp;
    }

    public BigDecimal getTotalUnpaid() {
        BigDecimal unpaidAmount = BigDecimal.ZERO;
        for (Driver driver: drivers.values()) {
            long duration = Util.getIntervalAfterTimestampOptimized(driver.getSlots(), driver.prefixSeconds, paidInterval);
            BigDecimal intervalInHours = BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP);
            unpaidAmount = unpaidAmount.add((intervalInHours).multiply(drivers.get(driver.id).hourlyRate));
        }
        return unpaidAmount.setScale(2, RoundingMode.HALF_UP);
    }
}


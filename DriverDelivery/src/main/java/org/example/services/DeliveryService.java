package org.example.services;

import org.example.model.Delivery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DeliveryService {

    public HashMap<Integer, List<Delivery>> getDeliveryDb() {
        return deliveryDb;
    }

    HashMap<Integer, List<Delivery>> deliveryDb;

    public void recordDelivery(int driverId, int startTime, int endTime) {
        List<Delivery> deliveries = mergeInterval(driverId, startTime, endTime);
        deliveryDb.put(driverId, deliveries);
    }

    public long getTotalHour(int driverId) {
        List<Delivery> deliveries = deliveryDb.getOrDefault(driverId, new ArrayList<>());
        return deliveries.stream().mapToLong(delivery -> delivery.getEndTime() - delivery.getStartTime()).sum();
    }

    public List<Delivery> mergeInterval(int driverId, int startTime, int endTime) {
        List<Delivery> deliveries = deliveryDb.getOrDefault(driverId, new ArrayList<>());
        List<Delivery> newDeliveries = new ArrayList<>();

        deliveries.sort(Comparator.comparing(Delivery::getStartTime));

        int i = 0;
        int n = deliveries.size();

        // 1. Add all intervals before newInterval
        while (i < n && deliveries.get(i).getEndTime() < startTime) {
            newDeliveries.add(deliveries.get(i));
            i++;
        }

        // 2. Merge overlapping intervals
        while (i < n && deliveries.get(i).getStartTime() <= endTime) {
            startTime = Math.min(startTime, deliveries.get(i).getStartTime());
            endTime = Math.max(endTime, deliveries.get(i).getEndTime());
            i++;
        }

        newDeliveries.add(new Delivery(driverId, startTime, endTime));

        // 3. Add remaining intervals
        while (i < n) {
            newDeliveries.add(deliveries.get(i));
            i++;
        }

        return newDeliveries;
    }
}

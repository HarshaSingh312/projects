//package org.example;
//
//import org.example.services.DeliveryService;
//import org.example.services.DriverService;
//
//public class DeliveryCostTrackingService {
//
//    DriverService driverService = new DriverService();
//    DeliveryService deliveryService = new DeliveryService();
//    double totalCost = 0;
//
//    public boolean addDriver(int driverId, double hourlyRate) {
//        if (driverService.isDriverAdded(driverId)) return false;
//        driverService.addDriver(driverId, hourlyRate);
//        return true;
//    }
//
//    public boolean recordDelivery(int driverId, int startTime, int endTime) {
//        if (!driverService.isDriverAdded(driverId)) return false;
//        if (startTime >= endTime) return false;
//        deliveryService.recordDelivery(driverId, startTime, endTime);
//        long hour = deliveryService.getTotalHour(driverId);
//        double driverCost = hour * driverService.getHourlyCost(driverId);
//        driverService.setTotalCost(driverId, driverCost);
//        return true;
//    }
//
//    public double getTotalCost() {
//        totalCost = deliveryService.getDeliveryDb().keySet().stream().mapToDouble(driver -> driverService.getTotalCount(driver)).sum();
//        return totalCost;
//    }
//
//    public void payDriver(int driverId, double amount) throws Exception {
//        if (!driverService.isDriverAdded(driverId)) return;
//        double pendingAmount = driverService.getPendingAmount(driverId);
//        if (pendingAmount < amount) throw new Exception("Paid amount more than pending");
//        driverService.payAmount(driverId, amount);
//    }
//
//    public double getPendingAmount(int driverId) {
//        if (!driverService.isDriverAdded(driverId)) return -1;
//        return driverService.getPendingAmount(driverId);
//    }
//}

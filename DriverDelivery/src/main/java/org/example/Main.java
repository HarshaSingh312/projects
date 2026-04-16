package org.example;

import java.math.BigDecimal;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        DeliverySystem df = new DeliverySystem();

        // ------------------ Add Drivers ------------------
        df.addDriver("D1", BigDecimal.valueOf(10));
        df.addDriver("D2", BigDecimal.valueOf(20));

        // ------------------ D1 ------------------
        df.recordDelivery("D1", 0, 3600);
        df.recordDelivery("D1", 3600, 7200);
        df.recordDelivery("D1", 1800, 5400); // merge → [0,7200]

        df.recordDelivery("D1", 8000, 9000);  // separate
        df.recordDelivery("D1", 7000, 8500);  // merge → [7000,9000]

        df.recordDelivery("D1", 0, 3600);     // duplicate (no change)
        df.recordDelivery("D1", 5000, 1000);  // invalid (ignored)

        // Final D1 intervals:
        // [0,9000] = 9000 sec

        // ------------------ D2 ------------------
        df.recordDelivery("D2", 0, 3600);
        df.recordDelivery("D2", 4000, 8000);
        df.recordDelivery("D2", 3000, 5000); // merge → [0,8000]

        // ------------------ Output ------------------
        System.out.println("Total Cost: " + df.getTotalCost());

        System.out.println("Unpaid (no payment): " + df.getTotalUnpaid());

        df.payUpTo(3600);
        System.out.println("Unpaid after paying up to 3600: " + df.getTotalUnpaid());

        df.payUpTo(7500);
        System.out.println("Unpaid after paying up to 7500: " + df.getTotalUnpaid());

        df.payUpTo(20000);
        System.out.println("Unpaid after full payment: " + df.getTotalUnpaid());
    }
}

package org.example;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
//        SplitBook sb = new SplitBook();
//        sb.registerUser("A", "Ann");
//        sb.registerUser("B", "Ben");
//
////        One expense: total = 1000; each owes 500
//        sb.recordExpense(1, List.of("A", "B"), List.of(1000, 0));
//
//        Balances:
//        System.out.println(sb.listBalances());

//

// ["B owes A: 500.00"]

        SplitBook sb = new SplitBook();
        sb.registerUser("A", "Ann");
        sb.registerUser("B", "Ben");
        sb.registerUser("C", "Cam");
        sb.registerUser("D", "Dia");

// #1: [A,B], total=300, each owes 150
        sb.recordExpense(21, List.of("A", "B"), List.of(0, 300));

// #2: [B,C,D], total=90, each owes 30
        sb.recordExpense(22, List.of("B", "C", "D"), List.of(90, 0, 0));

// #3: [A,C], total=100, each owes 50 (both paid equally)
        sb.recordExpense(23, List.of("A", "C"), List.of(50, 200));

        Balances:
        System.out.println(sb.listBalances());
    }
}

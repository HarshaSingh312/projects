package org.example.services;

import org.example.model.Expense;
import org.example.model.User;

import java.util.*;

class ExpenseUser {
    String id;
    double amount;
    public ExpenseUser(String id, double amount) {
        this.id = id;
        this.amount = amount;
    }
    public double getAmount() {
        return amount;
    }
}

public class ExpenseService {

    HashMap<Integer, Expense> expenseDb = new HashMap<>();
    Map<String, Double> creditors = new HashMap<>();
    Map<String, Double> debitors = new HashMap<>();

    public void createExpense(Integer id, List<String> user, List<Integer> amount) {
        expenseDb.put(id, new Expense(id, user, amount));
        int totalAmount = amount.stream().mapToInt(Integer::intValue).sum();
        double preUserExpense = (double) totalAmount / user.size();
        for (int i =0; i<user.size(); i++) {
            if (amount.get(i) > preUserExpense) {
                double currentAmountInCredit = creditors.getOrDefault(user.get(i), (double) 0);
                double currentAmountInDebit = debitors.getOrDefault(user.get(i), (double) 0);
                double amountNeedToAdded = amount.get(i) - preUserExpense;
                if (currentAmountInCredit > 0) {
                    creditors.put(user.get(i), currentAmountInCredit + amountNeedToAdded);
                } else if (currentAmountInDebit > amountNeedToAdded) {
                    debitors.put(user.get(i), currentAmountInDebit - amountNeedToAdded);
                } else if (currentAmountInDebit < amountNeedToAdded) {
                    debitors.remove(user.get(i));
                    creditors.put(user.get(i), amountNeedToAdded - currentAmountInDebit);
                }
            } else if (amount.get(i) < preUserExpense) {
                double currentAmountInCredit = creditors.getOrDefault(user.get(i), (double) 0);
                double currentAmountInDebit = debitors.getOrDefault(user.get(i), (double) 0);
                double amountNeedToDebit = preUserExpense - amount.get(i);
                if (currentAmountInDebit > 0) {
                    debitors.put(user.get(i), currentAmountInDebit + amountNeedToDebit);
                } else if (currentAmountInCredit > amountNeedToDebit) {
                    creditors.put(user.get(i), currentAmountInCredit - amountNeedToDebit);
                } else if (currentAmountInCredit < amountNeedToDebit) {
                    creditors.remove(user.get(i));
                    debitors.put(user.get(i), amountNeedToDebit - currentAmountInCredit);
                }
            }
        }
    }

    public List<String> settleBalance() {
        List<String> result = new ArrayList<>();
        List<ExpenseUser> creditorsList = new ArrayList<>();
        List<ExpenseUser> debitorsList = new ArrayList<>();
        creditors.forEach((k, v) -> creditorsList.add(new ExpenseUser(k, v)));
        debitors.forEach((k, v) -> debitorsList.add(new ExpenseUser(k, v)));
        creditorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());
        debitorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());

        while (!creditorsList.isEmpty() && !debitorsList.isEmpty()) {
            ExpenseUser creditor = creditorsList.remove(0);
            ExpenseUser debtor = debitorsList.remove(0);
            double min = Math.min(creditor.amount, debtor.amount);
            result.add(debtor.id + " owes " + creditor.id + ": " + String.format("%.2f", min));
            double creditorRemaining = creditor.amount - min;
            double debtorRemaining = debtor.amount - min;
            if (creditorRemaining > 0) {
                creditorsList.add(new ExpenseUser(creditor.id, creditorRemaining));
                creditorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());
            }
            if (debtorRemaining > 0) {
                debitorsList.add(new ExpenseUser(debtor.id, debtorRemaining));
                debitorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());
            }
        }

        Collections.sort(result);
        return result;
    }
}

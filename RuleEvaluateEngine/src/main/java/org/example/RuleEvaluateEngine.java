package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum Rule {
    NoAirFly,
    NoEntertainment,
    RestaurantExpense,
    ExpenseLimit,
    TripExpense,
    TripMealExpense
}

class Violation {
    String expenseId;
    String tripId;
    String ruleId;

    public Violation(String expenseId, String tripId, String ruleId) {
        this.expenseId = expenseId;
        this.tripId = tripId;
        this.ruleId = ruleId;
    }
}

interface RuleEngine {
    public List<Violation> validate(RuleContext context);
}

class RuleContext {
    public RuleContext(List<Expense> expenses) {
        this.expenses = expenses;
    }

    List<Expense> expenses;

    public void setTripTotal(HashMap<String, BigDecimal> tripTotal) {
        this.tripTotal = tripTotal;
    }

    Map<String, BigDecimal> tripTotal;

    public void setTripMealTotal(HashMap<String, BigDecimal> tripMealTotal) {
        this.tripMealTotal = tripMealTotal;
    }

    Map<String, BigDecimal> tripMealTotal;
}

class NoAirFlyRule implements RuleEngine {

    @Override
    public List<Violation> validate(RuleContext context) {
        List<Violation> violations = new ArrayList<>();
        List<Expense> violationExpense = context.expenses.stream().filter(expense -> "airfare".equals(expense.expenseType)).toList();
        for (Expense expense: violationExpense) {
            violations.add(new Violation(expense.expenseId, expense.tripId, Rule.NoAirFly.name()));
        }
        return violations;
    }
}

class NoEntertainmentRule implements RuleEngine {

    @Override
    public List<Violation> validate(RuleContext context) {
        List<Violation> violations = new ArrayList<>();
        List<Expense> violationExpense = context.expenses.stream().filter(expense -> "entertainment".equals(expense.expenseType)).toList();
        for (Expense expense: violationExpense) {
            violations.add(new Violation(expense.expenseId, expense.tripId, Rule.NoEntertainment.name()));
        }
        return violations;
    }
}

class RestaurantExpenseRule implements RuleEngine {

    @Override
    public List<Violation> validate(RuleContext context) {
        List<Violation> violations = new ArrayList<>();
        List<Expense> violationExpense = context.expenses.stream().filter(expense -> "restaurant".equals(expense.vendorType) &&
                expense.amount > 75).toList();
        for (Expense expense: violationExpense) {
            violations.add(new Violation(expense.expenseId, expense.tripId, Rule.RestaurantExpense.name()));
        }
        return violations;
    }
}

class ExpenseLimitRule implements RuleEngine {

    @Override
    public List<Violation> validate(RuleContext context) {
        List<Violation> violations = new ArrayList<>();
        List<Expense> violationExpense = context.expenses.stream().filter(expense -> expense.amount > 250).toList();
        for (Expense expense: violationExpense) {
            violations.add(new Violation(expense.expenseId, expense.tripId, Rule.ExpenseLimit.name()));
        }
        return violations;
    }
}

class TripExpenseRule implements RuleEngine {

    @Override
    public List<Violation> validate(RuleContext context) {
        List<Violation> violations = new ArrayList<>();
        HashMap<String, BigDecimal> tripAmount = new HashMap<>();
        for (Expense expense: context.expenses) {
            BigDecimal existingAmount = tripAmount.getOrDefault(expense.tripId, BigDecimal.ZERO);
            tripAmount.put(expense.tripId, existingAmount.add(expense.amount));
        }

        context.setTripTotal(tripAmount);

        for (String trip: tripAmount.keySet()) {
            if (tripAmount.get(trip) > 2000) {
                violations.add(new Violation(null, trip, Rule.TripExpense.name()));
            }
        }
        return violations;
    }
}

class TripMealRule implements RuleEngine {

    @Override
    public List<Violation> validate(RuleContext context) {
        List<Violation> violations = new ArrayList<>();
        HashMap<String, BigDecimal> tripAmount = new HashMap<>();
        for (Expense expense: context.expenses) {
            if (!"restaurant".equals(expense.vendorType)) {
                continue;
            }
            BigDecimal existingAmount = tripAmount.getOrDefault(expense.tripId, BigDecimal.ZERO);
            tripAmount.put(expense.tripId, existingAmount.add(expense.amount));
        }

        context.setTripMealTotal(tripAmount);

        for (String trip: tripAmount.keySet()) {
            if (tripAmount.get(trip) > 200) {
                violations.add(new Violation(null, trip, Rule.TripMealExpense.name()));
            }
        }
        return violations;
    }
}

class RuleEngineFactory {
    public static RuleEngine getRule(Rule id) {
        switch (id) {
            case NoAirFly -> {
                return new NoAirFlyRule();
            }
            case NoEntertainment -> {
                return new NoEntertainmentRule();
            }
            case ExpenseLimit -> {
                return new ExpenseLimitRule();
            }
            case RestaurantExpense -> {
                return new RestaurantExpenseRule();
            }
            case TripExpense -> {
                return new TripExpenseRule();
            }
            case TripMealExpense -> {
                return new TripMealRule();
            }
            default -> throw new RuntimeException("No rule defined for id " + id);
        }
    }
}

class Expense {
    String expenseId;
    String tripId;
    BigDecimal amount;
    String expenseType;
    String vendorType;

    static class Builder {
        Expense expense = new Expense();

        public Builder expenseId(String id) {
            expense.expenseId = id;
            return this;
        }

        public Builder tripId(String id) {
            expense.tripId = id;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            expense.amount = amount;
            return this;
        }

        public Builder expenseType(String id) {
            expense.expenseType = id;
            return this;
        }

        public Builder vendorType(String id) {
            expense.vendorType = id;
            return this;
        }

        public Expense build() {
            return expense;
        }
    }
}

class ExpenseMapper {
    public static List<Expense> convertToExpense(List<Map<String, String>> expenses) {
        List<Expense> expenseList = new ArrayList<>();
        for (Map<String, String> expense: expenses) {
            Expense e = new Expense.Builder()
                    .expenseId(expense.get("expense_id"))
                    .tripId(expense.get("trip_id"))
                    .amount(BigDecimal.valueOf(Double.parseDouble(expense.getOrDefault("amount_usd", "0"))))
                    .expenseType(expense.get("expense_type"))
                    .vendorType(expense.get("vendor_type"))
                    .build();
            expenseList.add(e);
        }
        return expenseList;
    }
}


public class RuleEvaluateEngine {

    public List<Violation> evaluateRules(List<Rule> rules, List<Map<String, String>> expenses) {

        List<RuleEngine> ruleEngineList = new ArrayList<>();
        for (Rule rule: rules) {
            ruleEngineList.add(RuleEngineFactory.getRule(rule));
        }

        List<Expense> expenses1 = ExpenseMapper.convertToExpense(expenses);

        List<Violation> violations = new ArrayList<>();

        // Evaluating all rule
        RuleContext context = new RuleContext(expenses1);
        for (RuleEngine ruleEngine: ruleEngineList) {
            violations.addAll(ruleEngine.validate(context));
        }

        return violations;
    }
}

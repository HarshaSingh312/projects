package org.example;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/* ===================== ENUM ===================== */

enum Rule {
    NoAirFly,
    NoEntertainment,
    RestaurantExpense,
    ExpenseLimit,
    TripExpense,
    TripMealExpense
}

/* ===================== DOMAIN ===================== */

class Violation {
    private final String expenseId;
    private final String tripId;
    private final String ruleId;

    public Violation(String expenseId, String tripId, String ruleId) {
        this.expenseId = expenseId;
        this.tripId = tripId;
        this.ruleId = ruleId;
    }

    public String getExpenseId() { return expenseId; }
    public String getTripId() { return tripId; }
    public String getRuleId() { return ruleId; }
}

class Expense {
    private final String expenseId;
    private final String tripId;
    private final BigDecimal amount;
    private final String expenseType;
    private final String vendorType;

    private Expense(Builder builder) {
        this.expenseId = builder.expenseId;
        this.tripId = builder.tripId;
        this.amount = builder.amount;
        this.expenseType = builder.expenseType;
        this.vendorType = builder.vendorType;
    }

    public String getExpenseId() { return expenseId; }
    public String getTripId() { return tripId; }
    public BigDecimal getAmount() { return amount; }
    public String getExpenseType() { return expenseType; }
    public String getVendorType() { return vendorType; }

    public static class Builder {
        private String expenseId;
        private String tripId;
        private BigDecimal amount = BigDecimal.ZERO;
        private String expenseType;
        private String vendorType;

        public Builder expenseId(String id) { this.expenseId = id; return this; }
        public Builder tripId(String id) { this.tripId = id; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder expenseType(String type) { this.expenseType = type; return this; }
        public Builder vendorType(String type) { this.vendorType = type; return this; }

        public Expense build() { return new Expense(this); }
    }
}

/* ===================== CONTEXT ===================== */

class RuleContext {
    private final List<Expense> expenses;

    private Map<String, BigDecimal> tripTotals = new HashMap<>();
    private Map<String, BigDecimal> tripMealTotals = new HashMap<>();

    public RuleContext(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<Expense> getExpenses() { return expenses; }

    public Map<String, BigDecimal> getTripTotals() { return tripTotals; }
    public void setTripTotals(Map<String, BigDecimal> tripTotals) {
        this.tripTotals = tripTotals;
    }

    public Map<String, BigDecimal> getTripMealTotals() { return tripMealTotals; }
    public void setTripMealTotals(Map<String, BigDecimal> tripMealTotals) {
        this.tripMealTotals = tripMealTotals;
    }
}

/* ===================== RULE ENGINE ===================== */

interface RuleEngine {
    List<Violation> validate(RuleContext context);
}

/* ===================== BASE RULE ===================== */

abstract class BaseExpenseRule {

    protected List<Violation> buildViolations(List<Expense> expenses, Rule rule) {
        return expenses.stream()
                .map(e -> new Violation(e.getExpenseId(), e.getTripId(), rule.name()))
                .toList();
    }
}

/* ===================== RULES ===================== */

class NoAirFlyRule extends BaseExpenseRule implements RuleEngine{
    public List<Violation> validate(RuleContext context) {
        return buildViolations(
                context.getExpenses().stream()
                        .filter(e -> "airfare".equals(e.getExpenseType()))
                        .toList(),
                Rule.NoAirFly
        );
    }
}

class NoEntertainmentRule extends BaseExpenseRule {
    public List<Violation> validate(RuleContext context) {
        return buildViolations(
                context.getExpenses().stream()
                        .filter(e -> "entertainment".equals(e.getExpenseType()))
                        .toList(),
                Rule.NoEntertainment
        );
    }
}

class RestaurantExpenseRule extends BaseExpenseRule {

    private static final BigDecimal LIMIT = BigDecimal.valueOf(75);

    public List<Violation> validate(RuleContext context) {
        return buildViolations(
                context.getExpenses().stream()
                        .filter(e -> "restaurant".equals(e.getVendorType()) &&
                                e.getAmount().compareTo(LIMIT) > 0)
                        .toList(),
                Rule.RestaurantExpense
        );
    }
}

class ExpenseLimitRule extends BaseExpenseRule {

    private static final BigDecimal LIMIT = BigDecimal.valueOf(250);

    public List<Violation> validate(RuleContext context) {
        return buildViolations(
                context.getExpenses().stream()
                        .filter(e -> e.getAmount().compareTo(LIMIT) > 0)
                        .toList(),
                Rule.ExpenseLimit
        );
    }
}

class TripExpenseRule implements RuleEngine {

    private static final BigDecimal LIMIT = BigDecimal.valueOf(2000);

    public List<Violation> validate(RuleContext context) {

        Map<String, BigDecimal> totals = context.getExpenses().stream()
                .collect(Collectors.groupingBy(
                        Expense::getTripId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Expense::getAmount,
                                BigDecimal::add
                        )
                ));

        context.setTripTotals(totals);

        return totals.entrySet().stream()
                .filter(e -> e.getValue().compareTo(LIMIT) > 0)
                .map(e -> new Violation(null, e.getKey(), Rule.TripExpense.name()))
                .toList();
    }
}

class TripMealRule implements RuleEngine {

    private static final BigDecimal LIMIT = BigDecimal.valueOf(200);

    public List<Violation> validate(RuleContext context) {

        Map<String, BigDecimal> totals = context.getExpenses().stream()
                .filter(e -> "restaurant".equals(e.getVendorType()))
                .collect(Collectors.groupingBy(
                        Expense::getTripId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Expense::getAmount,
                                BigDecimal::add
                        )
                ));

        context.setTripMealTotals(totals);

        return totals.entrySet().stream()
                .filter(e -> e.getValue().compareTo(LIMIT) > 0)
                .map(e -> new Violation(null, e.getKey(), Rule.TripMealExpense.name()))
                .toList();
    }
}

/* ===================== REGISTRY ===================== */

class RuleRegistry {

    private static final Map<Rule, RuleEngine> registry = new HashMap<>();

    static {
        register(Rule.NoAirFly, new NoAirFlyRule());
        register(Rule.NoEntertainment, new NoEntertainmentRule());
        register(Rule.ExpenseLimit, new ExpenseLimitRule());
        register(Rule.RestaurantExpense, new RestaurantExpenseRule());
        register(Rule.TripExpense, new TripExpenseRule());
        register(Rule.TripMealExpense, new TripMealRule());
    }

    public static void register(Rule rule, RuleEngine engine) {
        registry.put(rule, engine);
    }

    public static RuleEngine get(Rule rule) {
        RuleEngine engine = registry.get(rule);
        if (engine == null) {
            throw new IllegalArgumentException("No rule registered for: " + rule);
        }
        return engine;
    }
}

/* ===================== MAPPER ===================== */

class ExpenseMapper {
    public static List<Expense> map(List<Map<String, String>> input) {
        return input.stream()
                .map(e -> new Expense.Builder()
                        .expenseId(e.get("expense_id"))
                        .tripId(e.get("trip_id"))
                        .amount(new BigDecimal(e.getOrDefault("amount_usd", "0"))) // SAFE
                        .expenseType(e.get("expense_type"))
                        .vendorType(e.get("vendor_type"))
                        .build())
                .toList();
    }
}

/* ===================== ENGINE ===================== */

public class RuleEvaluate {

    public List<Violation> evaluate(List<Rule> rules, List<Map<String, String>> rawExpenses) {

        List<Expense> expenses = ExpenseMapper.map(rawExpenses);
        RuleContext context = new RuleContext(expenses);

        List<Violation> result = new ArrayList<>();

        for (Rule rule : rules) {
            result.addAll(RuleRegistry.get(rule).validate(context));
        }

        return result;
    }
}
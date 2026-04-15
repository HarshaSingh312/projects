package org.example;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/* ===================== ENUMS ===================== */

enum ExpenseType {
    AIRFARE, ENTERTAINMENT, MEAL, OTHER
}

enum RuleType {
    NO_AIRFARE,
    NO_ENTERTAINMENT,
    EXPENSE_LIMIT,
    RESTAURANT_LIMIT,
    TRIP_LIMIT,
    TRIP_MEAL_LIMIT
}

/* ===================== DOMAIN ===================== */

class Expense {
    final String expenseId;
    final String tripId;
    final String vendorId;
    final ExpenseType type;
    final BigDecimal amount;

    public Expense(String expenseId, String tripId, String vendorId,
                   ExpenseType type, BigDecimal amount) {
        this.expenseId = expenseId;
        this.tripId = tripId;
        this.vendorId = vendorId;
        this.type = type;
        this.amount = amount;
    }
}

class Violation {
    final String expenseId;
    final String tripId;
    final RuleType rule;
    final String message;

    public Violation(String expenseId, String tripId, RuleType rule, String message) {
        this.expenseId = expenseId;
        this.tripId = tripId;
        this.rule = rule;
        this.message = message;
    }
}

/* ===================== CONTEXT ===================== */

class EvaluationContext {
    final List<Expense> expenses;
    final Map<String, List<Expense>> expensesByTrip;

    public EvaluationContext(List<Expense> expenses) {
        this.expenses = expenses;
        this.expensesByTrip = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.tripId));
    }
}

/* ===================== RULE ENGINE ===================== */

interface Rule {
    List<Violation> evaluate(EvaluationContext context);
}

/* ===================== GENERIC RULES ===================== */

class PredicateRule implements Rule {
    private final RuleType ruleType;
    private final Predicate<Expense> predicate;
    private final String message;

    public PredicateRule(RuleType ruleType, Predicate<Expense> predicate, String message) {
        this.ruleType = ruleType;
        this.predicate = predicate;
        this.message = message;
    }

    @Override
    public List<Violation> evaluate(EvaluationContext context) {
        return context.expenses.stream()
                .filter(predicate)
                .map(e -> new Violation(e.expenseId, e.tripId, ruleType, message))
                .toList();
    }
}

class TripAggregateRule implements Rule {
    private final RuleType ruleType;
    private final Predicate<Expense> filter;
    private final BigDecimal limit;
    private final String message;

    public TripAggregateRule(RuleType ruleType,
                             Predicate<Expense> filter,
                             BigDecimal limit,
                             String message) {
        this.ruleType = ruleType;
        this.filter = filter;
        this.limit = limit;
        this.message = message;
    }

    @Override
    public List<Violation> evaluate(EvaluationContext context) {
        List<Violation> violations = new ArrayList<>();

        for (Map.Entry<String, List<Expense>> entry : context.expensesByTrip.entrySet()) {
            BigDecimal total = entry.getValue().stream()
                    .filter(filter)
                    .map(e -> e.amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (total.compareTo(limit) > 0) {
                violations.add(new Violation(null, entry.getKey(), ruleType, message));
            }
        }
        return violations;
    }
}

/* ===================== RULE FACTORY ===================== */

class RuleFactory {

    public static List<Rule> defaultRules() {
        return List.of(

                new PredicateRule(
                        RuleType.NO_AIRFARE,
                        e -> e.type == ExpenseType.AIRFARE,
                        "Airfare not allowed"
                ),

                new PredicateRule(
                        RuleType.NO_ENTERTAINMENT,
                        e -> e.type == ExpenseType.ENTERTAINMENT,
                        "Entertainment not allowed"
                ),

                new PredicateRule(
                        RuleType.EXPENSE_LIMIT,
                        e -> e.amount.compareTo(BigDecimal.valueOf(250)) > 0,
                        "Expense exceeds limit"
                ),

                new PredicateRule(
                        RuleType.RESTAURANT_LIMIT,
                        e -> e.type == ExpenseType.MEAL &&
                                e.amount.compareTo(BigDecimal.valueOf(75)) > 0,
                        "Meal exceeds per-expense limit"
                ),

                new TripAggregateRule(
                        RuleType.TRIP_LIMIT,
                        e -> true,
                        BigDecimal.valueOf(2000),
                        "Trip total exceeds limit"
                ),

                new TripAggregateRule(
                        RuleType.TRIP_MEAL_LIMIT,
                        e -> e.type == ExpenseType.MEAL,
                        BigDecimal.valueOf(200),
                        "Trip meal total exceeds limit"
                )
        );
    }
}

/* ===================== ENGINE ===================== */

class RuleEngine {
    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Violation> evaluate(List<Expense> expenses) {
        EvaluationContext context = new EvaluationContext(expenses);

        return rules.stream()
                .flatMap(rule -> rule.evaluate(context).stream())
                .toList();
    }
}

/* ===================== MAPPER ===================== */

class Mapper {
    public static List<Expense> map(List<Map<String, String>> raw) {
        return raw.stream()
                .map(m -> new Expense(
                        m.get("expense_id"),
                        m.get("trip_id"),
                        m.get("vendor_id"),
                        ExpenseType.valueOf(m.get("expense_type").toUpperCase()),
                        new BigDecimal(m.get("amount"))
                ))
                .toList();
    }
}
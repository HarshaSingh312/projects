package org.example;

import java.io.File;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

class Payment {
    private String id;
    private BigDecimal amount;
    private String user;

    public Payment() {}

    public Payment(String id, BigDecimal amount, String user) {
        this.id = id;
        this.amount = amount;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUser() {
        return user;
    }

    public boolean equal(Payment p) {
        return this.id.equals(p.id) && this.getAmount().equals(p.getAmount()) && this.getUser().equals(p.getUser());
    }
}

public class PaymentProcessing {
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Payment> paymentIds = new HashMap<>();
    private final String path = "src/main/java/org/example/data/payments.json";

    public void process() throws Exception {
        List<Payment> payments = readPayments();

        for (Payment payment: payments) {
            if (paymentIds.containsKey(payment.getId())) {
                Payment p = paymentIds.get(payment.getId());
                if (!p.equal(payment)) {
                    throw new IllegalArgumentException("Payment with different amount/user: " + payment + ", " + p);
                }
            }
            paymentIds.put(payment.getId(), payment);
            System.out.println(payment.getUser() + ", " + payment.getAmount() + ", " + payment.getId());
        }
    }

    private List<Payment> readPayments() throws Exception {
//        var stream = getClass().getResourceAsStream(path);
//        if (stream == null) throw new IllegalArgumentException("File not found: " + path);
//        return Arrays.asList(mapper.readValue(stream, Payment[].class));
        return Arrays.asList(mapper.readValue(new File(path), Payment[].class));
    }
}

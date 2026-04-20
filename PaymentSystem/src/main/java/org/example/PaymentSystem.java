package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Payment {
    private String id;
    private BigDecimal amount;
    private String invoiceId;
    private String memo;

    public Payment(String id, BigDecimal amount, String invoice, String memo) {
        this.id = id;
        this.amount = amount;
        this.invoiceId = invoice;
        this.memo = memo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

class Invoice {
    String id;
    LocalDate dueDate;
    BigDecimal amount;

    public Invoice(String id, LocalDate dueDate, BigDecimal amount) {
        this.id = id;
        this.dueDate = dueDate;
        this.amount = amount;
    }
}

class PaymentMapper {
    public static Payment map(String payment) {
        String[] parts = payment.split(",");
        if (parts.length < 3) throw new IllegalArgumentException("Invalid payment " + payment);
        BigDecimal amount = new BigDecimal(parts[1].trim());

        String memo = parts[2].trim();
        String invoiceId = null;

        if (memo.contains(":")) {
            String[] memoParts = memo.split(":");
            if (memoParts.length >= 2) {
                invoiceId = memoParts[1].trim();
            }
        }

        return new Payment(parts[0], amount, invoiceId, memo);
    }
}

class InvoiceMapper {
    public static List<Invoice> map(List<String> invoices) {
        List<Invoice> convertedInvoices = new ArrayList<>();
        for (String invoice: invoices) {
            try {
                String[] parts = invoice.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("Invalid invoice");
                BigDecimal amount = new BigDecimal(parts[2].trim());
                LocalDate localDate = LocalDate.parse(parts[1].trim());
                convertedInvoices.add(new Invoice(parts[0], localDate, amount));
            } catch (Exception e) {
                System.out.println("Invalid invoice: " + invoice);
                //continue;
            }
        }
        return convertedInvoices;
    }
}

public class PaymentSystem {
    private String formatInvoice(String paymentName, BigDecimal paymentAmount, String inv, LocalDate date, BigDecimal diff) {
        return String.format("Payment: %s, paymentAmount: %s, inv: %s, due date: %s, diff: %s", paymentName, paymentAmount, inv, date, diff);
    }

    public String reconcile(String paymentString, List<String> invoiceStrings, int forgiveness) { // pass 0 for part 1 & 2
        Payment payment1 = PaymentMapper.map(paymentString);
        List<Invoice> invoices = InvoiceMapper.map(invoiceStrings);

        // Checking
        for (Invoice invoice: invoices) {
            if (invoice.id.equals(payment1.getInvoiceId())) {
                return formatInvoice(payment1.getId(), payment1.getAmount(), invoice.id, invoice.dueDate, BigDecimal.ZERO);
            }
        }

        // Checking amount
        Invoice selectedInvoice = null;
        for (Invoice invoice: invoices) {
            BigDecimal diff = payment1.getAmount().subtract(invoice.amount);
            if (diff.abs().compareTo(BigDecimal.valueOf(forgiveness)) <= 0 && (Objects.isNull(selectedInvoice) || selectedInvoice.dueDate.isAfter(invoice.dueDate))) {
                selectedInvoice = invoice;
            }
        }

        if (Objects.nonNull(selectedInvoice)) {
            BigDecimal diff = payment1.getAmount().subtract(selectedInvoice.amount);
            return formatInvoice(payment1.getId(), payment1.getAmount(), selectedInvoice.id, selectedInvoice.dueDate, diff.abs());
        }
        return "NO INVOICE";
    }
}

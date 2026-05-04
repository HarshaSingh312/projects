package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.example.mapper.AmountDeserializer;
import org.example.mapper.AmountDeserializer2;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private int id;
    private String userName;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionType getTxnType() {
        return txnType;
    }

    public void setTxnType(TransactionType txnType) {
        this.txnType = txnType;
    }

    private Long timestamp;
    private TransactionType txnType;

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionLocation getLocation() {
        return location;
    }

    @JsonDeserialize(using = AmountDeserializer2.class)
    private BigDecimal amount;
    private TransactionLocation location;

    public Transaction() {

    }

    public Transaction(int id, String userName, long timestamp, TransactionType txnType, BigDecimal amount,
                       TransactionLocation location) {
        this.id = id;
        this.userName = userName;
        this.timestamp = timestamp;
        this.txnType = txnType;
        this.amount = amount;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", timestamp=" + timestamp +
                ", txnType=" + txnType +
                ", amount=" + amount +
                ", location=" + location +
                '}';
    }
}

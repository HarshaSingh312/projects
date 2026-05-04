package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class TransactionResponse {
    private List<Transaction> data;

    public TransactionResponse() {}

    public TransactionResponse(List<Transaction> data) {
        this.data = data;
    }

    public List<Transaction> getData() {
        return data;
    }
}

public class TransactionService {

    TransactionServiceClient client = new TransactionServiceClient();
    ObjectMapper mapper = new ObjectMapper();

    public List<Transaction> getTransactions() {
        JSONObject firstPage = client.fetchPage(1);
        int totalPages = firstPage.getInt("total_pages");

        List<JSONArray> transactionsArray = new ArrayList<>();
        transactionsArray.add(firstPage.getJSONArray("data"));
        List<CompletableFuture<JSONArray>> futures = new ArrayList<>();

        for (int i=2; i<= totalPages; i++) {
            final int index = i;
            futures.add(CompletableFuture.supplyAsync(() -> client.fetchPage(index).getJSONArray("data")));
        }

        for (CompletableFuture<JSONArray> future: futures) {
            transactionsArray.add(future.join());
        }

        List<Transaction> transactions = new ArrayList<>();

        for (JSONArray objects : transactionsArray) {
            for (int j = 0; j < objects.length(); j++) {
                try {
                    JSONObject currentTransaction = objects.getJSONObject(j);
                    Transaction transaction = mapper.readValue(currentTransaction.toString(), Transaction.class);
                    transactions.add(transaction);

//                    System.out.println(transaction.getId() + " " +transaction.getAmount() + " " + transaction.getTxnType()
//                    + transaction.getLocation());
                } catch (Exception e) {
                    // ignore and log
                    System.out.println("Exception parsing transaction: " + objects.getJSONObject(j) + e);
                }
            }
        }

        writeToFile(transactions);
        readFromFile();
        return transactions;
    }

    private void writeToFile(List<Transaction> transactions) {
        TransactionResponse response = new TransactionResponse(transactions);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/TransactionsFile.json"), response);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void readFromFile() {
        try {
            TransactionResponse response =
                    mapper.readValue(new File("src/main/resources/TransactionsFile.json"), TransactionResponse.class);
            List<Transaction> transactions = response.getData();
            System.out.println(transactions);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}

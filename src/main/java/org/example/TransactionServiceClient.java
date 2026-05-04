package org.example;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class TransactionServiceClient {

    private final String BASE_URI = "https://jsonmock.hackerrank.com/api/transactions";
    private final HttpClient client = HttpClient.newHttpClient();

    public JSONObject fetchPage(int page) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URI + "?page=" + page))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(response.body());
        } catch (Exception e) {
            System.out.println("Exception while fetching page " + page);
        }
        return null;
    }
}

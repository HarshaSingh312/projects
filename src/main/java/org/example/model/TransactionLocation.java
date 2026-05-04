package org.example.model;

public class TransactionLocation {
    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public Long getZipCode() {
        return zipCode;
    }

    //    "id":7,"address":"770, Deepends, Stockton Street","city":"Ripley","zipCode":44139
    private int id;
    private String address;
    private String city;
    private Long zipCode;

    public TransactionLocation() {}

    public TransactionLocation(int id, String address, String city, Long zipCode) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return "TransactionLocation{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", zipCode=" + zipCode +
                '}';
    }
}

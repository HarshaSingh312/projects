package org.example.model;

import java.util.List;

public class Expense {

    private Integer id;
    private List<String> members;
    private List<Integer> paid ;

    public Expense(int id, List<String> members, List<Integer> paid) {
        this.id = id;
        this.members = members;
        this.paid = paid;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<Integer> getPaid() {
        return paid;
    }
}

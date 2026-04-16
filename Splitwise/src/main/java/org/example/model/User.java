package org.example.model;

public class User {

    private String id;

    public String getDisplayName() {
        return displayName;
    }

    private String displayName;

    public User(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
}

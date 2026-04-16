package org.example.services;

import org.example.model.User;

import java.util.HashMap;

public class UserService {

    HashMap<String, User> userDb = new HashMap<>();

    public void registerUser(String id, String name) {
        userDb.put(id, new User(id, name));
    }

    public boolean isUserRegistered(String id) {
        return userDb.containsKey(id);
    }

    public String getUserName(String id) {
        return userDb.get(id).getDisplayName();
    }
}

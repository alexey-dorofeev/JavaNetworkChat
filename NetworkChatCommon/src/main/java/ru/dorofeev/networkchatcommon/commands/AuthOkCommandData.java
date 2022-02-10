package ru.dorofeev.networkchatcommon.commands;

import ru.dorofeev.networkchatcommon.auth.User;

import java.io.Serializable;

public class AuthOkCommandData implements Serializable {

    private final User user;

    public AuthOkCommandData(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

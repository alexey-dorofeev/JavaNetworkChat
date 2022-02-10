package ru.dorofeev.networkchatcommon.commands;

import java.io.Serializable;

public class UpdateUserNameCommandData implements Serializable {

    private final String newUserName;

    public UpdateUserNameCommandData(String newUserName) {
        this.newUserName = newUserName;
    }

    public String getNewUserName() {
        return newUserName;
    }
}

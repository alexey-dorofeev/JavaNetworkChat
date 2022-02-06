package ru.dorofeev.networkchatcommon.commands;

import java.io.Serializable;

public class PrivateMessageCommandData implements Serializable {

    private final String sender;
    private final String recipient;
    private final String message;

    public PrivateMessageCommandData(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }
}

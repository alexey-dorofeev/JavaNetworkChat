package ru.dorofeev.networkchatclient.client;

import ru.dorofeev.networkchatcommon.Command;

public interface CommandListener {
    void processCommand(Command message);
}
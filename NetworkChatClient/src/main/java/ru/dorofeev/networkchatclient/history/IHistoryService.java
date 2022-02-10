package ru.dorofeev.networkchatclient.history;

import java.util.List;

public interface IHistoryService {
    void saveMessageToHistory(String message);
    List<String> loadFromHistory(int rowCount);

    void init(String login);
    void close();
}

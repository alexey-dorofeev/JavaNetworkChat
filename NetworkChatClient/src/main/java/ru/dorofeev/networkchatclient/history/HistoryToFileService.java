package ru.dorofeev.networkchatclient.history;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryToFileService implements IHistoryService {

    private String login;
    private BufferedWriter writer;
    private final String fileNameFormat = "history_%s.txt";
    private File file;

    @Override
    public void saveMessageToHistory(String message) {
        try {
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи сообщения в историю", e);
        }
    }

    @Override
    public List<String> loadFromHistory(int rowCount) {
        List<String> messages = new ArrayList<>();

        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(file)) {
            int counter = 0;
            while (counter < rowCount) {
                String message = reversedLinesFileReader.readLine();
                if (message == null) {
                    break;
                }
                messages.add(message);
                counter++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузки истории из файла", e);
        }
        Collections.reverse(messages);

        return messages;
    }

    @Override
    public void init(String login) {
        this.login = login;

        try {
            file = new File(String.format(this.fileNameFormat, this.login));
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации службы хранения истории", e);
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при завершении работы службы хранения истории", e);
            }
        }
    }
}

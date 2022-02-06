package ru.dorofeev.networkchatclient.controllers;

import ru.dorofeev.networkchatclient.ChatApplication;
import ru.dorofeev.networkchatclient.client.Client;
import ru.dorofeev.networkchatclient.client.CommandListener;
import ru.dorofeev.networkchatcommon.CommandType;
import ru.dorofeev.networkchatcommon.commands.AuthOkCommandData;
import ru.dorofeev.networkchatcommon.commands.ErrorCommandData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AuthController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    private Client client;
    private ChatApplication application;
    private CommandListener listener;

    public void initClientHandler(Client client) {
        this.client = client;
        listener = this.client.addCommandListener(command -> {
            if (command.getType() == CommandType.AUTH_OK) {
                AuthOkCommandData data = (AuthOkCommandData) command.getData();
                Platform.runLater(
                        () -> {
                            application.switchToChatStage(data.getUserName());
                            client.removeCommandListener(listener);
                        }
                );
            } else if (command.getType() == CommandType.ERROR) {
                ErrorCommandData data = (ErrorCommandData) command.getData();
                Platform.runLater(
                        () -> application.showError(data.getErrorMessage())
                );
            }
        });
    }

    public void setApplication(ChatApplication application) {
        this.application = application;
    }

    @FXML
    public void onActionSendAuthMessage() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            application.showError("Логин и(или) пароль пустое");
            return;
        }

        try {
            client.sendAuthMessage(login, password);
        } catch (IOException e) {
            application.showError("Ошибка отравки данных");
        }
    }
}

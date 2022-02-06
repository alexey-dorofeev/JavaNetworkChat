package ru.dorofeev.networkchatclient.controllers;

import javafx.collections.FXCollections;
import javafx.scene.control.TextInputDialog;
import ru.dorofeev.networkchatclient.ChatApplication;
import ru.dorofeev.networkchatclient.client.Client;
import ru.dorofeev.networkchatcommon.CommandType;
import ru.dorofeev.networkchatcommon.commands.ErrorCommandData;
import ru.dorofeev.networkchatcommon.commands.PrivateMessageCommandData;
import ru.dorofeev.networkchatcommon.commands.PublicMessageCommandData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.dorofeev.networkchatcommon.commands.UpdateUserListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

public class ChatController {

    @FXML
    private TextArea textAreaChat;
    @FXML
    private TextField textFieldMessage;
    @FXML
    public ListView<String> listViewChatUsers;

    private Client client;
    private ChatApplication application;

    public void initClientHandler(Client client) {
        this.client = client;
        this.client.addCommandListener(command -> {
            if (command.getType() == CommandType.PRIVATE_MESSAGE) {
                PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                Platform.runLater(
                        () -> appendMessage(data.getSender(), data.getMessage())
                );
            } else if (command.getType() == CommandType.PUBLIC_MESSAGE) {
                PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                Platform.runLater(
                        () -> appendMessage(data.getSender() + " (Всем)", data.getMessage())
                );
            } else if (command.getType() == CommandType.UPDATE_USER_LIST) {
                UpdateUserListCommandData data = (UpdateUserListCommandData) command.getData();
                Platform.runLater(
                        () -> listViewChatUsers.setItems(FXCollections.observableList(data.getUsers()))
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
    public void onActionSendMessage() {

        String message = textFieldMessage.getText();
        textFieldMessage.clear();
        textFieldMessage.setFocusTraversable(true);

        if (!message.isEmpty()) {
            String recipient = null;
            if (!listViewChatUsers.getSelectionModel().isEmpty()) {
                recipient = listViewChatUsers.getSelectionModel().getSelectedItem();
            }

            try {
                if (recipient != null) {
                    appendMessage("Я", message);
                    client.sendPrivateMessage(application.getUserName(), recipient, message);
                } else {
                    appendMessage("Я (Всем)", message);
                    client.sendPublicMessage(application.getUserName(), message);
                }
            } catch (IOException e) {
                application.showError("Ошибка отравки данных");
            }
        }
    }

    @FXML
    public void onActionChangeUserName() {
        TextInputDialog editDialog = new TextInputDialog();
        editDialog.setTitle("Изменить ник");
        editDialog.setHeaderText("Введите новый ник");
        editDialog.setContentText("Новый ник:");

        Optional<String> result = editDialog.showAndWait();
        if (result.isPresent()) {
            try {
                String newUserName = result.get();
                client.changeUserName(newUserName);
                application.setUserName(newUserName);
            } catch (IOException e) {
                application.showError("Ошибка отравки данных");
            }

        }
    }

    private void appendMessage(String sender, String message) {
        textAreaChat.appendText(DateFormat.getDateTimeInstance().format(new Date()) + " " + sender + ": " + message);
        textAreaChat.appendText(System.lineSeparator());
    }
}
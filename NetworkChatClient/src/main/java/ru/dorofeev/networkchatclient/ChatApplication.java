package ru.dorofeev.networkchatclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.dorofeev.networkchatclient.client.Client;
import ru.dorofeev.networkchatclient.controllers.AuthController;
import ru.dorofeev.networkchatclient.controllers.ChatController;
import ru.dorofeev.networkchatclient.history.HistoryToFileService;
import ru.dorofeev.networkchatclient.history.IHistoryService;
import ru.dorofeev.networkchatcommon.auth.User;

import java.io.IOException;

public class ChatApplication extends Application {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8181;

    Client client;
    private IHistoryService history;

    private String userName;

    private Stage chatStage;
    private Stage authStage;

    private ChatController chatController;

    @Override
    public void start(Stage stage) throws IOException {

        try {
            client = new Client(SERVER_HOST, SERVER_PORT);
            client.connect();
        } catch (Exception e) {
            showError(String.format("Ошибка соединения с сервером: %s:%s", SERVER_HOST, SERVER_PORT));
            client.close();
            System.exit(1);
        }

        history = new HistoryToFileService();

        chatStage = stage;
        chatStage.setTitle("Онлайн-чат");
        chatController = initStage(chatStage, "chat-view.fxml");
        chatController.setApplication(this);

        authStage = new Stage();
        authStage.setTitle("Вход в онлайн-чат");
        authStage.initOwner(chatStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        AuthController authController = initStage(authStage, "auth-view.fxml");
        authController.setApplication(this);
        authController.initClientHandler(client);

        authStage.showAndWait();
    }

    private <T> T initStage(Stage stage, String resourceName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(resourceName));
        Parent authLoad = fxmlLoader.load();
        stage.setScene(new Scene(authLoad));
        stage.setOnCloseRequest(windowEvent -> exitApplication());
        return fxmlLoader.getController();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(chatStage);
        alert.setTitle("Ошибка");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(chatStage);
        alert.setTitle("Онлайн-чат");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public void switchToChatStage(User user) {
        chatController.initClientHandler(client);
        chatController.initHistory(history, user.getLogin());

        setUserName(user.getUserName());
        authStage.close();
        chatStage.show();
    }

    public void exitApplication() {
        if (client != null) {
            client.close();
        }
        if (history != null) {
            history.close();
        }
        Platform.exit();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        chatStage.setTitle("Онлайн-чат: " + this.userName);
    }

    public Stage getChatStage() {
        return chatStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
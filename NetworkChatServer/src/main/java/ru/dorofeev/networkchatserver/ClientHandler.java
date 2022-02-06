package ru.dorofeev.networkchatserver;

import ru.dorofeev.networkchatcommon.commands.UpdateUserNameCommandData;
import ru.dorofeev.networkchatserver.auth.User;
import ru.dorofeev.networkchatcommon.Command;
import ru.dorofeev.networkchatcommon.CommandType;
import ru.dorofeev.networkchatcommon.commands.AuthCommandData;
import ru.dorofeev.networkchatcommon.commands.PrivateMessageCommandData;
import ru.dorofeev.networkchatcommon.commands.PublicMessageCommandData;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class ClientHandler {

    public static final int TIMEOUT_TO_AUTH = 120;
    private final Server server;
    private final Socket clientSocket;

    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;
    private User user;

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        inStream = new ObjectInputStream(clientSocket.getInputStream());
        outStream = new ObjectOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(() -> {
                    try {
                        waitAuth();
                    } catch (IOException e) {
                        System.err.println("Клиент разорвал соединение");
                    }
                });

                try {
                    future.get(TIMEOUT_TO_AUTH, TimeUnit.SECONDS);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    future.cancel(true);
                    System.err.println("Клиент не авторизовался за отведенный таймаут");
                }
                executor.shutdownNow();

                if (user != null) {
                    waitCommands();
                }
            } catch (IOException e) {
                System.err.println("Клиент разорвал соединение");
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии соединения");
                }
            }
        }
        ).start();
    }

    private void waitAuth() throws IOException {
        System.out.println("Ожидаем аутентификации клиента...");
        while (true) {

            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.AUTH) {

                AuthCommandData authCommandData = (AuthCommandData) command.getData();

                System.out.printf("Попытка аутентификации: %s%n", command);

                String login = authCommandData.getLogin();
                String password = authCommandData.getPassword();

                user = server.getAuthService().auth(login, password);
                if (user == null) {
                    sendCommand(Command.errorCommand("Неверный(е) логин и(или) пароль"));
                } else if (server.isAlreadyAuth(login)) {
                    sendCommand(Command.errorCommand("Такой пользователь уже аутентифицирован"));
                } else {
                    sendCommand(Command.authOkCommand(user.getUserName()));
                    server.subscribe(this);
                    System.out.printf("Пользователь %s аутентифицирован%n", user.getUserName());
                    return;
                }
            }
        }
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Неизвестная команда");
            e.printStackTrace();
        }
        return command;
    }

    public void sendCommand(Command command) throws IOException {
        outStream.writeObject(command);
    }

    private void waitCommands() throws IOException {
        System.out.printf("Ожидаем сообщений от клиента %s...%n", user.getUserName());
        while (true) {

            Command command = readCommand();
            if (command == null) {
                continue;
            }

            System.out.printf("Сообщение от клиента %s: %s%n", user.getUserName(), command);

            switch (command.getType()) {
                case END -> {
                    System.out.printf("Соединение разорвано по команде от клиента %s%n", user.getUserName());
                    return;
                }
                case PRIVATE_MESSAGE -> {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    server.sendPrivateMessage(this, data.getRecipient(), data.getMessage());
                }
                case PUBLIC_MESSAGE -> {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    server.sendPublicMessage(this, data.getMessage());
                }
                case UPDATE_USERNAME -> {
                    UpdateUserNameCommandData data = (UpdateUserNameCommandData) command.getData();
                    server.getAuthService().setNewUserName(user, data.getNewUserName());
                    server.notifyClientUserListUpdated();
                }
            }
        }
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }

    public User getUser() {
        return user;
    }
}

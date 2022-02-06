package ru.dorofeev.networkchatserver;

import ru.dorofeev.networkchatserver.auth.IAuthService;
import ru.dorofeev.networkchatcommon.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private final int port;
    private final IAuthService authService;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();

    public Server(int port, IAuthService authService) {
        this.port = port;
        this.authService = authService;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер чата запущен");

            while (true) {
                processClientConnection(serverSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.releaseResources();
        }
    }

    private void processClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Ожидаем подключения...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Клиент подключился");

        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public synchronized void sendPublicMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendCommand(Command.publicMessageCommand(sender.getUser().getUserName(), message));
            }
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String message) throws IOException {
        for (ClientHandler client : clientHandlers) {
            if (client != sender && client.getUser().getUserName().equals(recipient)) {
                client.sendCommand(Command.privateMessageCommand(sender.getUser().getUserName(), recipient, message));
            }
        }
    }

    public synchronized void subscribe(ClientHandler client) throws IOException {
        this.clientHandlers.add(client);
        notifyClientUserListUpdated();
    }

    public synchronized void unsubscribe(ClientHandler client) throws IOException {
        this.clientHandlers.remove(client);
        notifyClientUserListUpdated();
    }

    public synchronized void notifyClientUserListUpdated() throws IOException {
        List<String> userListOnline = new ArrayList<>();

        for (ClientHandler client : clientHandlers) {
            userListOnline.add(client.getUser().getUserName());
        }

        for (ClientHandler client : clientHandlers) {
            client.sendCommand(Command.updateUserListCommand(userListOnline));
        }
    }

    public boolean isAlreadyAuth(String login) {
        for (ClientHandler client : clientHandlers) {
            if (client.getUser().getUserName().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public IAuthService getAuthService() {
        return authService;
    }
}

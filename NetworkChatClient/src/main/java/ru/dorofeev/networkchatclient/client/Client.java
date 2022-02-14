package ru.dorofeev.networkchatclient.client;

import ru.dorofeev.networkchatcommon.Command;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client {

    private final String ip;
    private final int port;

    private Socket socket;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;

    private final List<CommandListener> commandListeners = new CopyOnWriteArrayList<>();

    private Thread readMessagesProcess;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(ip, port);
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new ObjectInputStream(socket.getInputStream());

            readMessagesProcess = startReadMessagesProcess();

            System.out.println("Клиент запущен и подключился");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка установки соединения!", e);
        }
    }

    public Thread startReadMessagesProcess() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {

                    Command command = readCommand();
                    if (command == null) {
                        continue;
                    }

                    for (CommandListener listener : commandListeners) {
                        listener.processCommand(command);
                    }

                } catch (IOException e) {
                    System.err.println("Не удалось прочитать сообщение от сервера!");
                    close();
                    break;
                }
            }
        });
        thread.start();
        return thread;
    }

    public void sendAuthMessage(String login, String password) throws IOException {
        sendCommand(Command.authCommand(login, password));
    }

    public void sendPublicMessage(String sender, String message) throws IOException {
        sendCommand(Command.publicMessageCommand(sender, message));
    }

    public void sendPrivateMessage(String sender, String recipient, String message) throws IOException {
        sendCommand(Command.privateMessageCommand(sender, recipient, message));
    }

    public void changeUserName(String newUserName) throws IOException {
        sendCommand(Command.updateUserNameCommand(newUserName));
    }

    private void sendCommand(Command command) throws IOException {
        try {
            outStream.writeObject(command);
        } catch (IOException e) {
            System.err.println("Ошибка отравки данных!");
            throw e;
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

    public void close() {
        try {
            if(readMessagesProcess != null) {
                readMessagesProcess.interrupt();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CommandListener addCommandListener(CommandListener listener) {
        commandListeners.add(listener);
        return listener;
    }

    public void removeCommandListener(CommandListener listener) {
        commandListeners.remove(listener);
    }
}


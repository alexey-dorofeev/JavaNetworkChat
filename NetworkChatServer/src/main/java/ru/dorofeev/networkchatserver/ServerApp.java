package ru.dorofeev.networkchatserver;

import ru.dorofeev.networkchatserver.auth.AuthService;

public class ServerApp {
    public static void main(String[] args) {

        Server server = new Server(8181, new AuthService());
        server.start();
   }
}

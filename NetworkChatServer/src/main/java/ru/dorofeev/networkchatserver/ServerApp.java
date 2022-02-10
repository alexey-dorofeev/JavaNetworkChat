package ru.dorofeev.networkchatserver;

import ru.dorofeev.networkchatserver.auth.DbAuthService;

public class ServerApp {
    public static void main(String[] args) {

        Server server = new Server(8181, new DbAuthService("jdbc:sqlite:data.db"));
        server.start();
   }
}

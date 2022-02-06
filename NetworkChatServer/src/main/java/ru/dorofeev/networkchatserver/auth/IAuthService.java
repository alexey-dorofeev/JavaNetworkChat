package ru.dorofeev.networkchatserver.auth;

public interface IAuthService {

    User auth(String login, String password);
}

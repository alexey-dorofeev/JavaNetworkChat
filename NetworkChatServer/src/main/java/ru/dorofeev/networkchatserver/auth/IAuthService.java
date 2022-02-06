package ru.dorofeev.networkchatserver.auth;

public interface IAuthService {

    User auth(String login, String password);

    void setNewUserName(User user, String newUserName);

    void releaseResources();
}

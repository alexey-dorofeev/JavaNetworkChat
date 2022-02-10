package ru.dorofeev.networkchatserver.auth;

import ru.dorofeev.networkchatcommon.auth.User;

public interface IAuthService {

    User auth(String login, String password);

    void setNewUserName(User user, String newUserName);

    void releaseResources();
}

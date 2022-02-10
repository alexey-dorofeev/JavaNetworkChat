package ru.dorofeev.networkchatserver.auth;

import java.util.Set;

public class SimpleSetAuthService implements IAuthService {

    private static final Set<User> USERS = Set.of(
            new User(1L, "nick1", "user1", "123456"),
            new User(2L, "nick2", "user2", "123456"),
            new User(3L, "nick3", "user3", "123456")
    );

    @Override
    public User auth(String login, String password) {
        User authUser = new User(login, password);
        for (User user : USERS) {
            if (authUser.equals(user)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void setNewUserName(User user, String newUserName) {
        user.setUserName(newUserName);
    }

    @Override
    public void releaseResources() {

    }
}

package ru.dorofeev.networkchatserver.auth;

import java.util.Set;

public class AuthService implements IAuthService {

    private static final Set<User> USERS = Set.of(
            new User("user1", "123456"),
            new User("user2", "123456"),
            new User("user3", "123456")
    );

    public User auth(String login, String password) {
        User authUser = new User(login, password);
        for (User user : USERS) {
            if (authUser.equals(user)) {
                return user;
            }
        }
        return null;
    }
}

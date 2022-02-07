package ru.dorofeev.networkchatcommon.auth;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {

    private final Long id;
    private final String login;
    private final transient String password;
    private String userName;

    public User(Long id, String userName, String login, String password ) {
        this.id = id;
        this.userName = userName;
        this.login = login;
        this.password = password;

    }
    public User(Long id, String userName, String login) {
        this(id, userName,  login, null);
    }

    public User(String login, String password) {
        this(null, null,  login, password);
    }

    public String getUserName() {
        return userName;
    }

    public String getLogin() {
        return login;
    }

    public Long getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}

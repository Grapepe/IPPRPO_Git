package org.example;

import java.io.Serializable; // Добавлен импорт

public abstract class User implements Serializable { // Добавлен Serializable
    private static final long serialVersionUID = 1L;

    protected String login;
    protected String password;
    protected String email;

    public User() {}
    public User(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }

    public boolean authenticate(String inputLogin, String inputPassword) {
        return this.login.equals(inputLogin) && this.password.equals(inputPassword);
    }
}
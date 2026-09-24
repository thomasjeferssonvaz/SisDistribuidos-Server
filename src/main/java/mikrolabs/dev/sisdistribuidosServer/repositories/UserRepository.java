package mikrolabs.dev.sisdistribuidosServer.repositories;

import mikrolabs.dev.sisdistribuidosServer.DTOs.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserRepository {
    List<User> users = new ArrayList<>();

    public UserRepository() {
        User defaultUser = User.userAdmin("Admin", "admin", "admin", true);
        this.users.add(defaultUser);
    }


    public Optional<User> getUserByCredencials(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return users.stream()
                .filter(u -> Objects.equals(u.username(), username) && Objects.equals(u.password(), password))
                .findFirst();
    }

    public Optional<User> getUserByObject(User user) {
        if (user.username() == null) return Optional.empty();
        return users.stream()
                .filter(u -> Objects.equals(u.username(), user.username()))
                .findFirst();
    }

    public Optional<User> registerUser(String name, String username, String password) {
        User createdUser = User.user(name, username, password);
        if (getUserByObject(createdUser).isEmpty()) {
            users.add(createdUser);
            return Optional.of(createdUser);
        } else {
            return Optional.empty();
        }


    }

}

package mikrolabs.dev.sisdistribuidosServer.repositories;

import mikrolabs.dev.sisdistribuidosServer.DTOs.User;

import java.util.*;

public class UserRepository {
    private final List<User> users = Collections.synchronizedList(new ArrayList<>());

    public Optional<User> getUserByCredentials(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        synchronized(users) {
            return users.stream()
                    .filter(u -> Objects.equals(u.username(), username) && Objects.equals(u.password(), password))
                    .findFirst();
        }
    }

    public Optional<User> getUserByUsername(String username) {
        if (username == null) return Optional.empty();
        synchronized(users) {
            return users.stream()
                    .filter(u -> Objects.equals(u.username(), username))
                    .findFirst();
        }
    }

    public Optional<User> getUserByObject(User user) {
        if (user.username() == null) return Optional.empty();
        synchronized(users) {
            return users.stream()
                    .filter(u -> Objects.equals(u.username(), user.username()))
                    .findFirst();
        }
    }

    public Optional<User> registerUser(String name, String username, String password) {
        synchronized(users) {
            boolean alreadyExists = users.stream().anyMatch(u -> Objects.equals(u.username(), username));
            if (alreadyExists) return Optional.empty();

            User createdUser = User.user(name, username, password);
            users.add(createdUser);
            return Optional.of(createdUser);
        }

    }

    public Optional<User> updateUserName(String username, String newName) {
        synchronized(users) {
            Optional<User> userBeingUpdated = getUserByUsername(username);
            if (userBeingUpdated.isEmpty()) return Optional.empty();

            User currentUser = userBeingUpdated.get();
            User updatedUser = currentUser.withName(newName);

            int index = users.indexOf(currentUser);
            users.set(index, updatedUser);

            return getUserByObject(updatedUser);
        }
    }

    public Optional<User> updateUserPassword(String username, String newPassword) {
        synchronized(users) {
            Optional<User> userBeingUpdated = getUserByUsername(username);
            if (userBeingUpdated.isEmpty()) {
                return Optional.empty();
            }

            User currentUser = userBeingUpdated.get();
            User updatedUser = currentUser.withPassword(newPassword);

            int index = users.indexOf(currentUser);
            users.set(index, updatedUser);

            return Optional.of(updatedUser);
        }
    }

}

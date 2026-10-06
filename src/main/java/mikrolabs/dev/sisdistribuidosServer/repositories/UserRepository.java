package mikrolabs.dev.sisdistribuidosServer.repositories;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import mikrolabs.dev.sisdistribuidosServer.DTOs.User;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class UserRepository {
    private static final Object DATABASE_LOCK = new Object();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path databasePath;

    public UserRepository() { this(Path.of("data", "users.json")); }

    public UserRepository(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath().normalize();
        synchronized (DATABASE_LOCK) {
            if (!Files.exists(this.databasePath)) writeUsers(List.of());
            readUsers();
        }
    }

    private List<User> readUsers() {
        try (Reader reader = Files.newBufferedReader(databasePath, StandardCharsets.UTF_8)) {
            User[] stored = gson.fromJson(reader, User[].class);
            if (stored == null) throw new IllegalStateException("Arquivo de usuários inválido");
            Set<String> usernames = new HashSet<>();
            for (User user : stored) {
                if (user == null || user.name() == null || user.username() == null
                        || user.password() == null || !usernames.add(user.username())) {
                    throw new IllegalStateException("Arquivo de usuários inválido");
                }
            }
            return new ArrayList<>(Arrays.asList(stored));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler os usuários", e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Arquivo de usuários inválido", e);
        }
    }

    private void writeUsers(List<User> users) {
        Path temporary = null;
        try {
            Files.createDirectories(databasePath.getParent());
            temporary = Files.createTempFile(databasePath.getParent(), "users-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                gson.toJson(users, writer);
            }
            try {
                Files.move(temporary, databasePath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, databasePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar os usuários", e);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public Optional<User> getUserByCredentials(String username, String password) {
        synchronized (DATABASE_LOCK) {
            return readUsers().stream().filter(u -> Objects.equals(u.username(), username)
                    && Objects.equals(u.password(), password)).findFirst();
        }
    }

    public Optional<User> getUserByUsername(String username) {
        synchronized (DATABASE_LOCK) {
            return readUsers().stream().filter(u -> Objects.equals(u.username(), username)).findFirst();
        }
    }

    public Optional<User> getUserByObject(User user) { return getUserByUsername(user.username()); }

    public Optional<User> registerUser(String name, String username, String password) {
        synchronized (DATABASE_LOCK) {
            List<User> users = readUsers();
            if (users.stream().anyMatch(u -> Objects.equals(u.username(), username))) return Optional.empty();
            User created = User.user(name, username, password);
            users.add(created);
            writeUsers(users);
            return Optional.of(created);
        }
    }

    public Optional<User> updateUserName(String username, String newName) {
        synchronized (DATABASE_LOCK) {
            List<User> users = readUsers();
            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).username(), username)) {
                    User updated = users.get(i).withName(newName);
                    users.set(i, updated);
                    writeUsers(users);
                    return Optional.of(updated);
                }
            }
            return Optional.empty();
        }
    }

    public Optional<User> updateUserPassword(String username, String oldPassword, String newPassword) {
        synchronized (DATABASE_LOCK) {
            List<User> users = readUsers();
            for (int i = 0; i < users.size(); i++) {
                User current = users.get(i);
                if (Objects.equals(current.username(), username)) {
                    if (!Objects.equals(current.password(), oldPassword)) return Optional.empty();
                    User updated = current.withPassword(newPassword);
                    users.set(i, updated);
                    writeUsers(users);
                    return Optional.of(updated);
                }
            }
            return Optional.empty();
        }
    }

    public Optional<User> deleteUser(String username) {
        synchronized (DATABASE_LOCK) {
            List<User> users = readUsers();
            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).username(), username)) {
                    User deleted = users.remove(i);
                    writeUsers(users);
                    return Optional.of(deleted);
                }
            }
            return Optional.empty();
        }
    }
}

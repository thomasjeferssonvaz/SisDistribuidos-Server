package mikrolabs.dev.sisdistribuidosServer.DTOs;

public record User(
        String name,
        String username,
        String password
) {
    public static User user(String name, String username, String password){
        return new User(name, username, password);
    }

    public User withName(String newName) {
        return new User(newName, this.username, this.password);
    }

    public User withPassword(String newPassword) {
        return new User(this.name, this.username, newPassword);
    }
}

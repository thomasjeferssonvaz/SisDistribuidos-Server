package mikrolabs.dev.sisdistribuidosServer.DTOs;

public record User(
        String name,
        String username,
        String password,
        String token,
        boolean admin
) {
    public static User user(String name, String username, String password){
        return new User(name, username, password, "", false);
    }

    public static User userAdmin(String name, String username, String password, boolean admin){
        return new User(name, username, password, "", admin);
    }

    public User withName(String newName) {
        return new User(newName, this.username, this.password, this.token, this.admin);
    }

    public User withPassword(String newPassword) {
        return new User(this.name, this.username, newPassword, this.token, this.admin);
    }
}

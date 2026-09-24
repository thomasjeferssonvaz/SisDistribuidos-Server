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
}

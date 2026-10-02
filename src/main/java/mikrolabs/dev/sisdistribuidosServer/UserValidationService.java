package mikrolabs.dev.sisdistribuidosServer;

import java.util.regex.Pattern;

public class UserValidationService {
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^(?=.*\\p{L})[\\p{L} ]{1,60}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-z0-9._]{3,20}$");

    private static final Pattern PASSWORD_CHARACTERS_PATTERN =
            Pattern.compile("^[A-Za-z0-9#.*&%$@!()_+=-]{8,20}$");

    public boolean isNameValid(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public boolean isUsernameValid(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public boolean hasOnlyAllowedPasswordCharacters(String password) {
        return password != null
                && PASSWORD_CHARACTERS_PATTERN.matcher(password).matches();
    }
}

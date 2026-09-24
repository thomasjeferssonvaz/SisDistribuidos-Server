package mikrolabs.dev.sisdistribuidosServer.DTOs;

import java.util.Optional;
import java.util.UUID;

public record Session(
        UUID token,
        Optional<User> user
) {

}

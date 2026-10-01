package mikrolabs.dev.sisdistribuidosServer.DTOs;

import java.util.UUID;

public record Session(
        UUID token,
        String username
) {

}

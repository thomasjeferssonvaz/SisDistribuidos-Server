package mikrolabs.dev.sisdistribuidosServer.exceptions;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;

public class UserAlreadyExists extends BaseException {
    public UserAlreadyExists(String username) {
        super("User with the following name already exists: " + username);
    }

    @Override
    public Response toResponse() {
        return Response.error(409, getMessage());
    }
}

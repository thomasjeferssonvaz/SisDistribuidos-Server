package mikrolabs.dev.sisdistribuidosServer.exceptions;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;

public class UserAlreadyExists extends BaseException {
    public UserAlreadyExists(String username) {
        super("O username já está em uso.");
    }

    @Override
    public Response toResponse() {
        return Response.error(409, getMessage());
    }
}

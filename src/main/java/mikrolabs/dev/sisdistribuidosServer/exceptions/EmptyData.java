package mikrolabs.dev.sisdistribuidosServer.exceptions;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;

public class EmptyData extends BaseException {
    public EmptyData() {
        super("Campo data Vazio");
    }

    @Override
    public Response toResponse() {
        return Response.error(400, getMessage());
    }
}

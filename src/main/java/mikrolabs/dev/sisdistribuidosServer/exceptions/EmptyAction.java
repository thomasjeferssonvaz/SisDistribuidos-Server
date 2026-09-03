package mikrolabs.dev.sisdistribuidosServer.exceptions;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;

public class EmptyAction extends BaseException {
    public EmptyAction() {
        super("O campo 'action' é obrigatório");
    }

    @Override
    public Response toResponse() {
        return Response.error(400, "O campo 'action' é obrigatório");
    }
}

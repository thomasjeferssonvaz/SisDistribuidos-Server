package mikrolabs.dev.sisdistribuidosServer.exceptions;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Request;
import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;

public class ActionNotFound extends BaseException {
    Request request;

    public ActionNotFound(String message, Request request) {
        super("Ação desconhecida: " + request.action());
        this.request = request;
    }

    @Override
    public Response toResponse() {
        return Response.error(404, "Ação desconhecida: " + this.request.action());
    }
}

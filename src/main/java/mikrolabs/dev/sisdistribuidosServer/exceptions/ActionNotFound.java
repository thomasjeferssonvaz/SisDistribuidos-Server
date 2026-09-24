package mikrolabs.dev.sisdistribuidosServer.exceptions;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Request;
import mikrolabs.dev.sisdistribuidosServer.DTOs.Response;

public class ActionNotFound extends BaseException {
    Request request;

    public ActionNotFound(Request request) {
        super("Ação desconhecida: " + request.method());
        this.request = request;
    }


    @Override
    public Response toResponse() {
        return Response.error(404, getMessage());
    }
}

package ca.mestevens.java.dropwizard.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/test")
public class SimpleResource {

    @GET
    public Response getResponse() {
        return Response.ok().build();
    }

}

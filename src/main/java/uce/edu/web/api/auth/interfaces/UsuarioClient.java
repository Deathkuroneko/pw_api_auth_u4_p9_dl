package uce.edu.web.api.auth.interfaces;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import uce.edu.web.api.auth.entity.UsuarioDTO;


@RegisterRestClient(baseUri = "http://localhost:7890/matricula/api/v1.0")
@Path("/usuarios")
public interface UsuarioClient {

    @GET
    @Path("/{username}")
    UsuarioDTO buscarPorNombre(@PathParam("username") String username);
}
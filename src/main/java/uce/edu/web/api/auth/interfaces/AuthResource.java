package uce.edu.web.api.auth.interfaces;

import java.time.Instant;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uce.edu.web.api.auth.entity.Usuario;

@Path("/auth")
public class AuthResource {

    @ConfigProperty(name = "auth.issuer")
    String issuer;

    @ConfigProperty(name = "auth.token.ttl")
    long ttl;

    @POST
    @Path("/login") // Cambiamos la ruta a /login
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse login(Usuario credenciales) { // Recibe el objeto Usuario desde Vue

        // 1. Buscar el usuario en la base de datos por el nombre
        Usuario userFound = Usuario.find("username", credenciales.username).firstResult();

        // 2. Validar credenciales (Comparación simple para el ejercicio)
        if (userFound == null || !userFound.password.equals(credenciales.password)) {
            throw new WebApplicationException(
                "Usuario o contraseña incorrectos", 
                Response.Status.UNAUTHORIZED
            );
        }

        // 3. Si todo está bien, generamos el Token con los datos de la DB
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttl);

        String jwt = Jwt.issuer(issuer)
                .subject(userFound.username) // Usamos el nombre de la DB
                .groups(Set.of(userFound.role)) // Usamos el rol de la DB
                .issuedAt(now)
                .expiresAt(exp)
                .sign();

        return new TokenResponse(jwt, exp.getEpochSecond(), userFound.role);
    }


    public static class TokenResponse {
        public String accessToken;
        public long expiresAt;
        public String role;

        public TokenResponse() {}

        public TokenResponse(String accessToken, long expiresAt, String role) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
            this.role = role;
        }
    }
}

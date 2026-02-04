package uce.edu.web.api.auth.interfaces;

import java.time.Instant;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import uce.edu.web.api.auth.entity.UsuarioDTO;

@Path("/auth")
public class AuthResource {

    @ConfigProperty(name = "auth.issuer")
    String issuer;

    @ConfigProperty(name = "auth.token.ttl")
    long ttl;

    @Inject
    @RestClient
    UsuarioClient usuarioClient;    

    @POST
    @Path("/login") // Cambiamos la ruta a /login
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse login(UsuarioDTO credenciales) { // Recibe el objeto Usuario desde Vue

        System.out.println("Intentando login para: " + credenciales.username);
            
            UsuarioDTO userFound;
            try {
                userFound = usuarioClient.buscarPorNombre(credenciales.username);
                System.out.println("Usuario encontrado en Matrícula: " + userFound.username);
            } catch (Exception e) {
                System.out.println("Error llamando a Matrícula: " + e.getMessage());
                throw new WebApplicationException("Error de conexión", 401);
            }

            if (!userFound.password.equals(credenciales.password)) {
                System.out.println("Contraseña no coincide. DB: " + userFound.password + " Form: " + credenciales.password);
                throw new WebApplicationException("Password incorrecto", 401);
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

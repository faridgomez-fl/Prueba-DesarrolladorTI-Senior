package com.faridfarfan.pruebadesarrolladortisenior.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Valida el header x-api-key. */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "x-api-key";

    private final byte[] apiKeyEsperada;

    public ApiKeyAuthenticationFilter(String apiKeyEsperada) {
        this.apiKeyEsperada = apiKeyEsperada.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER);
        if (apiKey != null && esValida(apiKey)) {
            var autenticacion = new PreAuthenticatedAuthenticationToken(
                    "cliente-api", null, AuthorityUtils.createAuthorityList("ROLE_API"));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        }
        chain.doFilter(request, response);
    }

    /** Comparación en tiempo constante. */
    private boolean esValida(String apiKey) {
        return MessageDigest.isEqual(apiKeyEsperada, apiKey.getBytes(StandardCharsets.UTF_8));
    }
}

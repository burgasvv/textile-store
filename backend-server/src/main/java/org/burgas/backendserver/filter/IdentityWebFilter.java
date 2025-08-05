package org.burgas.backendserver.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.burgas.backendserver.entity.Authority;
import org.burgas.backendserver.entity.Identity;
import org.burgas.backendserver.exception.IdentityNotAuthenticatedException;
import org.burgas.backendserver.exception.IdentityNotAuthorizedException;
import org.burgas.backendserver.message.IdentityMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@WebFilter(
        urlPatterns = {
                "/identities/by-id","/identities/update","/identities/delete","/identities/change-password",
                "/identities/upload-image","/identities/change-image","/identities/delete-image",

                "/buckets/by-id", "/buckets/by-identity", "/buckets/add-product",
                "/buckets/plus-product-amount", "/buckets/minus-product-amount",
                "/buckets/remove-product", "/buckets/clean-bucket", "/buckets/pay-bill",

                "/bills/by-identity"
        }
)
public class IdentityWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (
                request.getRequestURI().equals("/identities/by-id") ||

                request.getRequestURI().equals("/buckets/add-product") ||
                request.getRequestURI().equals("/buckets/by-id") || request.getRequestURI().equals("/buckets/by-identity") ||
                request.getRequestURI().equals("/buckets/plus-product-amount") || request.getRequestURI().equals("/buckets/minus-product-amount") ||
                request.getRequestURI().equals("/buckets/remove-product") || request.getRequestURI().equals("/buckets/clean-bucket") ||
                request.getRequestURI().equals("/buckets/pay-bill") ||

                request.getRequestURI().equals("/bills/by-identity")
        ) {

            Authentication authentication = (Authentication) request.getUserPrincipal();

            if (authentication.isAuthenticated()) {

                Identity identity = (Identity) authentication.getPrincipal();
                String identityIdParam = request.getParameter("identityId");
                UUID identityId = identityIdParam == null || identityIdParam.isBlank() ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : UUID.fromString(identityIdParam);

                if (identity.getId().equals(identityId) || identity.getAuthority().equals(Authority.ADMIN)) {

                    filterChain.doFilter(request, response);

                } else {
                    throw new IdentityNotAuthorizedException(IdentityMessages.IDENTITY_NOT_AUTHORIZED.getMessage());
                }

            } else {
                throw new IdentityNotAuthenticatedException(IdentityMessages.IDENTITY_NOT_AUTHENTICATED.getMessage());
            }

        } else if (
                request.getRequestURI().equals("/identities/update") ||
                request.getRequestURI().equals("/identities/delete") ||
                request.getRequestURI().equals("/identities/change-password") ||
                request.getRequestURI().equals("/identities/upload-image") ||
                request.getRequestURI().equals("/identities/change-image") ||
                request.getRequestURI().equals("/identities/delete-image")
        ) {

            Authentication authentication = (Authentication) request.getUserPrincipal();

            if (authentication.isAuthenticated()) {

                Identity identity = (Identity) authentication.getPrincipal();
                String identityIdParam = request.getParameter("identityId");
                UUID identityId = identityIdParam == null || identityIdParam.isBlank() ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : UUID.fromString(identityIdParam);

                if (identity.getId().equals(identityId)) {

                    filterChain.doFilter(request, response);

                } else {
                    throw new IdentityNotAuthorizedException(IdentityMessages.IDENTITY_NOT_AUTHORIZED.getMessage());
                }

            } else {
                throw new IdentityNotAuthenticatedException(IdentityMessages.IDENTITY_NOT_AUTHENTICATED.getMessage());
            }
        }
    }
}

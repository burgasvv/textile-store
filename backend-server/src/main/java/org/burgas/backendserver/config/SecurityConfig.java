package org.burgas.backendserver.config;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.burgas.backendserver.entity.Authority.ADMIN;
import static org.burgas.backendserver.entity.Authority.USER;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler()))
                .cors(cors -> cors.configurationSource(new UrlBasedCorsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .authenticationManager(this.authenticationManager())
                .authorizeHttpRequests(
                        httpRequests -> httpRequests

                                .requestMatchers(
                                        "/security/csrf-token",

                                        "/identities/create",

                                        "/images/by-id",

                                        "/categories", "/categories/by-id",

                                        "/products", "/products/by-id",

                                        "/buckets/by-session", "/buckets/add-product-in-session", "/buckets/remove-product-in-session",
                                        "/buckets/plus-product-amount-in-session", "/buckets/minus-product-amount-in-session",
                                        "/buckets/by-cookie", "/buckets/add-product-by-cookie", "/buckets/remove-product-by-cookie",
                                        "/buckets/plus-product-amount-by-cookie", "/buckets/minus-product-amount-by-cookie"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/identities/by-id", "/identities/update", "/identities/delete", "/identities/change-password",
                                        "/identities/upload-image", "/identities/change-image", "/identities/delete-image",

                                        "/buckets/by-identity", "/buckets/add-product", "/buckets/plus-product-amount", "/buckets/minus-product-amount",
                                        "/buckets/remove-product", "/buckets/clean-bucket", "/buckets/pay-bill",

                                        "/bills/by-identity", "/bills/by-id"
                                )
                                .hasAnyAuthority(ADMIN.getAuthority(), USER.getAuthority())

                                .requestMatchers(
                                        "/identities", "/identities/enable-disable",

                                        "/categories/create", "/categories/create-multiple", "/categories/update", "/categories/delete",

                                        "/products/create", "/products/update", "/products/delete",

                                        "/buckets/by-id"
                                )
                                .hasAnyAuthority(ADMIN.getAuthority())
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(this.customUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }
}

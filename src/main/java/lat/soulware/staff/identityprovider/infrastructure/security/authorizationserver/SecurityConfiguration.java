package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Three ways in, checked in order: the OpenID Connect endpoints that log people in and issue service
 * tokens, the directory API services call with one of those tokens, and the login form itself.
 */
@Configuration
public class SecurityConfiguration {

    /**
     * The OpenID Connect provider endpoints. A browser that reaches the authorization endpoint without
     * a session is sent to the login form, and a logged-in person is let into an application only when
     * {@link EligibilityValidator} finds a role for them there. Userinfo answers with the claims the ID
     * token carries.
     */
    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurity(HttpSecurity http, EligibilityValidator eligibility)
        throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServer = new OAuth2AuthorizationServerConfigurer();
        MediaTypeRequestMatcher browserRequests = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);

        return http.securityMatcher(authorizationServer.getEndpointsMatcher())
            .with(authorizationServer, server -> server
                .authorizationEndpoint(endpoint -> endpoint.authenticationProviders(
                    providers -> checkEligibility(providers, eligibility)
                ))
                .oidc(oidc -> oidc.userInfoEndpoint(
                    userInfo -> userInfo.userInfoMapper(SecurityConfiguration::userInfoFromIdToken)
                ))
            )
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"),
                browserRequests
            ))
            .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
            .build();
    }

    /**
     * The directory API, for services holding a token staff issued to them for it: addressed to the
     * directory API, marked {@code token_use: service}, and carrying the {@code people.read} scope. A
     * person's token is refused even when addressed here. Stateless, so there is no session to forge a
     * request against and no CSRF token either. A request without a usable token is answered by
     * the kernel's {@code UnauthenticatedEntryPoint}, the same way sgt and poi answer it.
     */
    @Bean
    @Order(2)
    SecurityFilterChain directoryApiSecurity(
        HttpSecurity http,
        JWKSource<SecurityContext> jwkSource,
        AuthorizationServerSettings settings,
        AuthenticationEntryPoint entryPoint,
        @Value("${staff.directory.audience}") String audience
    ) throws Exception {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSource(jwkSource).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(List.of(
            new JwtIssuerValidator(settings.getIssuer()),
            new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, audiences -> audiences.contains(audience)),
            new JwtClaimValidator<String>("token_use", "service"::equals)
        )));

        return http.securityMatcher("/api/**")
            .csrf(a -> a.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests.anyRequest().hasAuthority("SCOPE_people.read"))
            .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.decoder(decoder))
                .authenticationEntryPoint(entryPoint)
            )
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
            .build();
    }

    /**
     * The login form the authorization endpoint sends browsers to. The OpenAPI document and the
     * Swagger UI over it fall to this chain too, and are served without a login when they are on.
     */
    @Bean
    @Order(3)
    SecurityFilterChain loginSecurity(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .build();
    }

    /** Checks the secrets confidential clients authenticate with against their BCrypt hashes. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Lets the frontends call the token, userinfo and key endpoints from the browser. */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${staff.oidc.web.origins}") List<String> webOrigins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(webOrigins);
        cors.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/oauth2/**", cors);
        source.registerCorsConfiguration("/userinfo", cors);
        source.registerCorsConfiguration("/.well-known/**", cors);
        return source;
    }

    /** Adds the eligibility check after Spring's own checks of the authorization request. */
    private static void checkEligibility(List<AuthenticationProvider> providers, EligibilityValidator eligibility) {
        Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> validator =
            new OAuth2AuthorizationCodeRequestAuthenticationValidator().andThen(eligibility);

        providers.stream()
            .filter(OAuth2AuthorizationCodeRequestAuthenticationProvider.class::isInstance)
            .map(OAuth2AuthorizationCodeRequestAuthenticationProvider.class::cast)
            .forEach(provider -> provider.setAuthenticationValidator(validator));
    }

    private static OidcUserInfo userInfoFromIdToken(OidcUserInfoAuthenticationContext context) {
        OidcIdToken idToken = context.getAuthorization().getToken(OidcIdToken.class).getToken();
        Map<String, Object> claims = idToken.getClaims();

        return new OidcUserInfo(claims);
    }
}

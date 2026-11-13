package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import lat.soulware.staff.shared.infrastructure.i18n.LocaleConfiguration;

/**
 * Honours the OpenID Connect {@code ui_locales} parameter. An application sends it to the
 * authorization endpoint with the language its user picked, space separated in order of
 * preference; the first one staff supports is kept in the session, so the login page the browser
 * is sent to next renders in it. An unsupported or missing value changes nothing, and the page
 * falls back to the browser's {@code Accept-Language}.
 *
 * <p>Runs ahead of Spring Security, which would otherwise redirect an anonymous request to the
 * login page before the parameter is read.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class UiLocalesFilter extends OncePerRequestFilter {

    static final String UI_LOCALES_PARAMETER = "ui_locales";

    private final LocaleResolver localeResolver;
    private final String authorizationEndpoint;

    public UiLocalesFilter(LocaleResolver localeResolver, AuthorizationServerSettings settings) {
        this.localeResolver = localeResolver;
        this.authorizationEndpoint = settings.getAuthorizationEndpoint();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !this.authorizationEndpoint.equals(request.getServletPath())
            || request.getParameter(UI_LOCALES_PARAMETER) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        Arrays.stream(request.getParameter(UI_LOCALES_PARAMETER).trim().split("\\s+"))
            .map(LocaleConfiguration::supportedLocale)
            .flatMap(optional -> optional.stream())
            .findFirst()
            .ifPresent(locale -> this.localeResolver.setLocale(request, response, locale));

        chain.doFilter(request, response);
    }
}

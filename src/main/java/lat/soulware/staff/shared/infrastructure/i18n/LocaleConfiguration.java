package lat.soulware.staff.shared.infrastructure.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * The languages staff answers in, and how a request's language is picked. English is the default;
 * Spanish is the one translation. A language stored in the session wins, which is how a login page
 * honours the {@code ui_locales} an application asked for. Otherwise the request's
 * {@code Accept-Language} decides, which is how an API caller gets its error messages. Anything
 * else falls back to English.
 */
@Configuration
public class LocaleConfiguration {

    /** The languages the message bundles cover, the default first. */
    public static final List<Locale> SUPPORTED_LOCALES = List.of(Locale.ENGLISH, Locale.of("es"));

    /** Named {@code localeResolver} so the dispatcher servlet picks it up instead of Spring's default. */
    @Bean
    LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocaleFunction(LocaleConfiguration::fromAcceptLanguage);
        return resolver;
    }

    /** The supported locale a language tag names, matched by language, such as {@code es} for {@code es-PE}. */
    public static Optional<Locale> supportedLocale(String languageTag) {
        String language = Locale.forLanguageTag(languageTag).getLanguage();

        return SUPPORTED_LOCALES.stream()
            .filter(locale -> locale.getLanguage().equals(language))
            .findFirst();
    }

    /**
     * The first supported language in {@code Accept-Language}, in the caller's order of preference.
     * Without the header the servlet container would report its own default locale, so the header's
     * absence is checked first.
     */
    private static Locale fromAcceptLanguage(HttpServletRequest request) {
        if (request.getHeader(HttpHeaders.ACCEPT_LANGUAGE) == null) {
            return SUPPORTED_LOCALES.getFirst();
        }

        return Collections.list(request.getLocales()).stream()
            .map(locale -> locale.toLanguageTag())
            .map(LocaleConfiguration::supportedLocale)
            .flatMap(optional -> optional.stream())
            .findFirst()
            .orElse(SUPPORTED_LOCALES.getFirst());
    }
}

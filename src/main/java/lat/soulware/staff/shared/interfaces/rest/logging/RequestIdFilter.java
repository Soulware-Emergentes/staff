package lat.soulware.staff.shared.interfaces.rest.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Assigns every request an identifier and publishes it three ways: in the SLF4J {@link MDC} under
 * {@link #MDC_KEY} for the duration of the request, on the response under {@link #HEADER}, and as a
 * request attribute that survives a forward, an include, and the container's error dispatch.
 *
 * <p>The identifier is 32 lowercase hex characters, the shape W3C Trace Context gives a trace id.
 * It is generated here on every request and never read from the incoming headers.
 *
 * <p>Registered at highest precedence, so the identifier is set before Spring Security's chain runs
 * and every log line written while serving the request carries it. The MDC is thread-local, so work
 * handed to another thread leaves the identifier behind.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    /** The MDC key, read by the {@code %X{requestId}} in the logging pattern. */
    public static final String MDC_KEY = "requestId";

    /** The response header carrying the identifier back to the caller. */
    public static final String HEADER = "X-Request-Id";

    /** Where the identifier is parked so a re-dispatch of the same request reuses it. */
    private static final String ATTRIBUTE = RequestIdFilter.class.getName() + ".id";

    /**
     * The identifier assigned to the request being served on this thread.
     *
     * @return the identifier, or {@code null} on a thread this filter did not run on
     */
    public static String current() {
        return MDC.get(MDC_KEY);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        String id = idFor(request);

        request.setAttribute(ATTRIBUTE, id);
        response.setHeader(HEADER, id);

        try (MDC.MDCCloseable _ = MDC.putCloseable(MDC_KEY, id)) {
            chain.doFilter(request, response);
        }
    }

    /** Runs on the async dispatch too, so the identifier is present on the thread that resumes. */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /** Runs on the error dispatch too, so whatever answers {@code /error} logs under it. */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    /** Reuses what an earlier dispatch of this request assigned, and mints one otherwise. */
    private static String idFor(HttpServletRequest request) {
        if (request.getAttribute(ATTRIBUTE) instanceof String assigned) {
            return assigned;
        }

        return UUID.randomUUID().toString().replace("-", "");
    }
}

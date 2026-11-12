package lat.soulware.staff.shared.interfaces.rest.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * Covers what the rest of the application relies on: an identifier readable from anywhere inside
 * the request, gone by the time the thread is handed back, and the same value on the response as in
 * the log.
 */
class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void publishesTheSameIdentifierToTheMdcAndTheResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] seenInside = new String[1];

        this.filter.doFilter(
            new MockHttpServletRequest(),
            response,
            capturing(seenInside)
        );

        assertNotNull(seenInside[0], "the chain ran without an identifier in the MDC");
        assertEquals(seenInside[0], response.getHeader(RequestIdFilter.HEADER));
    }

    @Test
    void mintsThirtyTwoLowercaseHexCharacters() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        this.filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertTrue(
            response.getHeader(RequestIdFilter.HEADER).matches("[0-9a-f]{32}"),
            "identifier is not a 32 character lowercase hex string"
        );
    }

    @Test
    void clearsTheMdcSoAPooledThreadCarriesNothingOver() throws Exception {
        this.filter.doFilter(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            new MockFilterChain()
        );

        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }

    @Test
    void assignsADistinctIdentifierPerRequest() throws Exception {
        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();

        this.filter.doFilter(new MockHttpServletRequest(), first, new MockFilterChain());
        this.filter.doFilter(new MockHttpServletRequest(), second, new MockFilterChain());

        assertNotEquals(
            first.getHeader(RequestIdFilter.HEADER),
            second.getHeader(RequestIdFilter.HEADER)
        );
    }

    @Test
    void reusesTheIdentifierWhenTheSameRequestIsDispatchedAgain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();

        this.filter.doFilter(request, first, new MockFilterChain());
        request.removeAttribute(RequestIdFilter.class.getName() + ".FILTERED");
        this.filter.doFilter(request, second, new MockFilterChain());

        assertEquals(
            first.getHeader(RequestIdFilter.HEADER),
            second.getHeader(RequestIdFilter.HEADER)
        );
    }

    @Test
    void ignoresAnIdentifierTheCallerSupplied() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestIdFilter.HEADER, "forged\nENTRY written by the caller");

        this.filter.doFilter(request, response, new MockFilterChain());

        assertNotEquals(
            "forged\nENTRY written by the caller",
            response.getHeader(RequestIdFilter.HEADER)
        );
    }

    /** A chain that records what {@link RequestIdFilter#current()} reports while it runs. */
    private static FilterChain capturing(String[] target) {
        return new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                target[0] = RequestIdFilter.current();
            }
        };
    }
}

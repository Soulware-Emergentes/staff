package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the login form at {@code /login} from the {@code login} template. Spring Security handles
 * the form's {@code POST}; this only renders the page the authorization endpoint sends browsers to,
 * naming the application that sent them there through {@link RequestingApplicationInterceptor}. A
 * browser that has already logged in is sent on instead, by {@link SignedInLoginInterceptor}.
 *
 * <p>Also serves, from the {@code continuing} template, the page a continued login passes through on
 * its way back to that application, which names it the same way and goes on to it through
 * {@link ContinuationInterceptor}.
 */
@Configuration
public class LoginPageConfiguration implements WebMvcConfigurer {

    private static final String LOGIN_PAGE = ContinuedLogin.LOGIN_PAGE;

    private final SignedInLoginInterceptor signedIn;
    private final RequestingApplicationInterceptor requestingApplication;
    private final ContinuationInterceptor continuation;

    public LoginPageConfiguration(
        SignedInLoginInterceptor signedIn,
        RequestingApplicationInterceptor requestingApplication,
        ContinuationInterceptor continuation
    ) {
        this.signedIn = signedIn;
        this.requestingApplication = requestingApplication;
        this.continuation = continuation;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(LOGIN_PAGE).setViewName("login");
        registry.addViewController(ContinuedLogin.CONTINUING_PAGE).setViewName("continuing");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.signedIn).addPathPatterns(LOGIN_PAGE);
        registry.addInterceptor(this.continuation).addPathPatterns(ContinuedLogin.CONTINUING_PAGE);
        registry.addInterceptor(this.requestingApplication).addPathPatterns(LOGIN_PAGE, ContinuedLogin.CONTINUING_PAGE);
    }
}

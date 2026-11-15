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
 *
 * <p>And, from the {@code signing-out} template, the page where a person confirms a sign-out an
 * application asked for without an ID token, filled by {@link SigningOutInterceptor}. Its form is
 * sent to Spring Security's {@code /logout}, as the hub's is.
 */
@Configuration
public class LoginPageConfiguration implements WebMvcConfigurer {

    private static final String LOGIN_PAGE = ContinuedLogin.LOGIN_PAGE;

    private final SignedInLoginInterceptor signedIn;
    private final RequestingApplicationInterceptor requestingApplication;
    private final ContinuationInterceptor continuation;
    private final SigningOutInterceptor signingOut;

    public LoginPageConfiguration(
        SignedInLoginInterceptor signedIn,
        RequestingApplicationInterceptor requestingApplication,
        ContinuationInterceptor continuation,
        SigningOutInterceptor signingOut
    ) {
        this.signedIn = signedIn;
        this.requestingApplication = requestingApplication;
        this.continuation = continuation;
        this.signingOut = signingOut;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(LOGIN_PAGE).setViewName("login");
        registry.addViewController(ContinuedLogin.CONTINUING_PAGE).setViewName("continuing");
        registry.addViewController(SigningOutInterceptor.SIGNING_OUT_PAGE).setViewName("signing-out");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.signedIn).addPathPatterns(LOGIN_PAGE);
        registry.addInterceptor(this.continuation).addPathPatterns(ContinuedLogin.CONTINUING_PAGE);
        registry.addInterceptor(this.requestingApplication).addPathPatterns(LOGIN_PAGE, ContinuedLogin.CONTINUING_PAGE);
        registry.addInterceptor(this.signingOut).addPathPatterns(SigningOutInterceptor.SIGNING_OUT_PAGE);
    }
}

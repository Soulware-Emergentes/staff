package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the hub at {@code /} from the {@code hub} template: where a login that no application asked
 * for lands, greeting the person and listing the applications they can open through
 * {@link HubInterceptor}. A card opens its application through the notice at {@code /opening},
 * filled by {@link OpeningInterceptor} and rendered from the {@code continuing} template a continued
 * login shows.
 */
@Configuration
public class HubPageConfiguration implements WebMvcConfigurer {

    private static final String HUB_PAGE = "/";
    private static final String OPENING_PAGE = "/opening";

    private final HubInterceptor hub;
    private final OpeningInterceptor opening;

    public HubPageConfiguration(HubInterceptor hub, OpeningInterceptor opening) {
        this.hub = hub;
        this.opening = opening;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(HUB_PAGE).setViewName("hub");
        registry.addViewController(OPENING_PAGE).setViewName("continuing");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.hub).addPathPatterns(HUB_PAGE);
        registry.addInterceptor(this.opening).addPathPatterns(OPENING_PAGE);
    }
}

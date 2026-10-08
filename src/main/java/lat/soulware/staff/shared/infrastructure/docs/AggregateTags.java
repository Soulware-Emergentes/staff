package lat.soulware.staff.shared.infrastructure.docs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;
import lat.soulware.staff.shared.domain.model.aggregates.PackagedAs;

/**
 * Tags every operation in the OpenAPI document with the value its aggregate's root declares in
 * {@link PackagedAs}, so the Swagger UI groups endpoints the way the code slices them. A controller
 * sits in {@code <module>.interfaces.rest.<directory>}, and the directory is one a root of the same
 * module claims; the claims are read once, from the roots' class metadata, when the document is
 * first built. A controller in a directory no root claims keeps the tag springdoc gives it.
 */
@Component
public class AggregateTags implements OperationCustomizer {

    private static final String SHARED = ".shared.";
    private static final String DOMAIN = ".domain.";
    private static final String INTERFACES = ".interfaces.rest.";

    /** The value each root declares, keyed by its module's package and that value. */
    private final Map<String, String> claims;

    public AggregateTags() {
        String ownPackage = AggregateTags.class.getPackageName();
        String basePackage = ownPackage.substring(0, ownPackage.indexOf(SHARED));
        this.claims = AggregateTags.claimsUnder(basePackage);
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        String controllerPackage = handlerMethod.getBeanType().getPackageName();
        int boundary = controllerPackage.indexOf(INTERFACES);
        if (boundary < 0) {
            return operation;
        }
        String module = controllerPackage.substring(0, boundary);
        String directory = controllerPackage.substring(boundary + INTERFACES.length());
        String claimed = this.claims.get(AggregateTags.claimKey(module, directory));

        return claimed == null
            ? operation
            : operation.tags(List.of(claimed));
    }

    private static Map<String, String> claimsUnder(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {

            /** Accepts abstract roots too, which the default turns away as no candidate for a bean. */
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return true;
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(PackagedAs.class));

        Map<String, String> claims = new HashMap<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
            AnnotationMetadata root = ((AnnotatedBeanDefinition) candidate).getMetadata();
            String value = root.getAnnotations().get(PackagedAs.class).getString("value");
            String module = root.getClassName().substring(0, root.getClassName().indexOf(DOMAIN));
            claims.put(AggregateTags.claimKey(module, value), value);
        }
        return Map.copyOf(claims);
    }

    private static String claimKey(String module, String directory) {
        return module + "/" + directory;
    }
}

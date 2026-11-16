package lat.soulware.staff.i18n;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lat.soulware.staff.shared.domain.model.exceptions.DomainException;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Checks that the message bundles and the domain exception hierarchy agree. Every concrete
 * {@link DomainException} reports a key derived from its own type, so the set of keys the
 * application can emit is a function of the classes on the classpath and can be compared against
 * what the bundles actually define.
 *
 * <p>This catches the two ways the pair drifts: an exception added without a translation, and a
 * class renamed so its derived key no longer matches the entry written for its old name.
 */
class MessageBundleTest {

    private static final String ROOT_PACKAGE = "lat.soulware.staff";
    private static final String DEFAULT_BUNDLE = "messages.properties";
    private static final String BUNDLE_PATTERN = "classpath*:messages*.properties";

    /** Imported once and shared: the exception hierarchy is the same for every check here. */
    private static final JavaClasses APPLICATION_CLASSES = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(ROOT_PACKAGE);

    @Test
    void everyDomainExceptionHasATranslation() throws IOException {
        Set<String> defined = keysOf(defaultBundle());

        for (Class<?> type : concreteDomainExceptions()) {
            String key = DomainException.messageKeyFor(type.asSubclass(DomainException.class));

            assertTrue(
                defined.contains(key),
                "%s reports '%s', which %s does not define".formatted(
                    type.getName(),
                    key,
                    DEFAULT_BUNDLE
                )
            );
        }
    }

    @Test
    void everyLocaleDefinesTheSameKeys() throws IOException {
        Resource defaultBundle = defaultBundle();
        Set<String> expected = keysOf(defaultBundle);

        for (Resource bundle : allBundles()) {
            if (bundle.getFilename().equals(DEFAULT_BUNDLE)) {
                continue;
            }

            Set<String> missing = new TreeSet<>(expected);
            missing.removeAll(keysOf(bundle));

            Set<String> extra = new TreeSet<>(keysOf(bundle));
            extra.removeAll(expected);

            assertTrue(
                missing.isEmpty() && extra.isEmpty(),
                "%s differs from %s: missing %s, unexpected %s".formatted(
                    bundle.getFilename(),
                    DEFAULT_BUNDLE,
                    missing,
                    extra
                )
            );
        }
    }

    /** The exception types that can actually be thrown, so abstract kinds report no key. */
    @SuppressWarnings("null")
    private static List<Class<?>> concreteDomainExceptions() {
        return APPLICATION_CLASSES.stream()
            .filter(type -> type.isAssignableTo(DomainException.class))
            .filter(type -> !type.getModifiers().contains(JavaModifier.ABSTRACT))
            .<Class<?>>map(JavaClass::reflect)
            .toList();
    }

    /** The bundle without a locale suffix, the set every other bundle is measured against. */
    private static Resource defaultBundle() throws IOException {
        return allBundles().stream()
            .filter(bundle -> DEFAULT_BUNDLE.equals(bundle.getFilename()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(DEFAULT_BUNDLE + " is not on the classpath"));
    }

    /** Every bundle on the classpath, locale suffixes included. */
    private static List<Resource> allBundles() throws IOException {
        return List.of(new PathMatchingResourcePatternResolver().getResources(BUNDLE_PATTERN));
    }

    /** The keys a bundle defines, read as properties so escaping and encoding match runtime. */
    private static Set<String> keysOf(Resource bundle) throws IOException {
        Properties properties = new Properties();

        try (InputStream contents = bundle.getInputStream()) {
            properties.load(contents);
        }

        return properties.stringPropertyNames();
    }
}

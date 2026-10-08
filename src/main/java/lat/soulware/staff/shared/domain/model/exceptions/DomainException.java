package lat.soulware.staff.shared.domain.model.exceptions;

import java.util.Locale;

/**
 * Root of the domain exception hierarchy. Carries an i18n message key and its arguments.
 *
 * <p>The key is derived from the type, as {@code error.<module>.<rule>}, where the module is the
 * package segment preceding {@code .domain.} and the rule is the type's own name in kebab case with
 * the {@code Exception} suffix dropped. Every concrete subclass must resolve against the message
 * bundle.
 *
 * <p>Sealed over the four kinds of failure the domain recognises. Throw the most specific type that
 * fits.
 */
public abstract sealed class DomainException extends RuntimeException permits
    UnauthenticatedException,
    ForbiddenException,
    EntityNotFoundException,
    BusinessRuleViolationException {

    /** Prefix on every derived key. */
    private static final String KEY_PREFIX = "error.";

    /** Dropped from the type name when deriving the rule name. */
    private static final String TYPE_SUFFIX = "Exception";

    /** Separates the module path from the layer path in a domain exception's package. */
    private static final String DOMAIN_SEGMENT = ".domain.";

    /** Values interpolated into the resolved message. */
    private final transient Object[] messageArgs;

    /**
     * @param messageArgs values interpolated into the resolved message, in placeholder order
     */
    protected DomainException(Object... messageArgs) {
        this.messageArgs = messageArgs;
    }

    /**
     * Derives the message key a given exception type reports.
     *
     * @param type the exception type
     * @return the i18n key that type reports
     */
    public static String messageKeyFor(Class<? extends DomainException> type) {
        return KEY_PREFIX + moduleOf(type) + "." + kebabCase(ruleNameOf(type));
    }

    /**
     * The i18n key naming this failure. Resolving it against a message bundle and a locale belongs
     * to whoever reports the failure onwards.
     *
     * @return the message key
     */
    public String getMessageKey() {
        return messageKeyFor(this.getClass());
    }

    /**
     * The key doubles as the JVM exception message.
     *
     * @return the message key
     */
    @Override
    public String getMessage() {
        return this.getMessageKey();
    }

    /**
     * Values to interpolate into the resolved message, in the order its placeholders expect.
     *
     * @return the arguments, in placeholder order
     */
    public Object[] getMessageArgs() {
        return this.messageArgs;
    }

    /** The module a domain exception belongs to, taken from the segment preceding {@code .domain.}. */
    private static String moduleOf(Class<?> type) {
        String packageName = type.getPackageName();
        int layerBoundary = packageName.indexOf(DOMAIN_SEGMENT);
        String modulePath = layerBoundary < 0
            ? packageName
            : packageName.substring(0, layerBoundary);

        return modulePath.substring(modulePath.lastIndexOf('.') + 1);
    }

    /** The type's simple name with the shared {@code Exception} suffix removed. */
    private static String ruleNameOf(Class<?> type) {
        String simpleName = type.getSimpleName();
        boolean carriesSuffix = simpleName.endsWith(TYPE_SUFFIX)
            && simpleName.length() > TYPE_SUFFIX.length();

        return carriesSuffix
            ? simpleName.substring(0, simpleName.length() - TYPE_SUFFIX.length())
            : simpleName;
    }

    /** Splits on the boundary between a lower-case or digit character and an upper-case one. */
    private static String kebabCase(String name) {
        return name.replaceAll("(?<=[a-z0-9])(?=[A-Z])", "-").toLowerCase(Locale.ROOT);
    }
}

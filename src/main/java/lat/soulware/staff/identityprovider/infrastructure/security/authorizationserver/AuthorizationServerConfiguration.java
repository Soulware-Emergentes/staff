package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

/**
 * Who the issuer is, and the key tokens are signed with. The registered applications are data, read
 * by {@link ProjectedRegisteredClientRepository}.
 *
 * <p>Authorizations live in memory, so a restart ends every session. The signing key is read from
 * files, so tokens issued before a restart still validate after it.
 */
@Configuration
public class AuthorizationServerConfiguration {

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(@Value("${staff.oidc.issuer}") String issuer) {
        return AuthorizationServerSettings.builder()
            .issuer(issuer)
            .build();
    }

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(
        @Value("${staff.oidc.signing.private-key-path}") Path privateKeyPath,
        @Value("${staff.oidc.signing.public-key-path}") Path publicKeyPath
    ) throws IOException, JOSEException {
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;
        try (
            InputStream privatePem = Files.newInputStream(privateKeyPath);
            InputStream publicPem = Files.newInputStream(publicKeyPath)
        ) {
            privateKey = RsaKeyConverters.pkcs8().convert(privatePem);
            publicKey = RsaKeyConverters.x509().convert(publicPem);
        }

        RSAKey key = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyIDFromThumbprint()
            .build();

        return new ImmutableJWKSet<>(new JWKSet(key));
    }
}

package lat.soulware.staff.identityprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import lat.soulware.staff.support.PostgresIntegrationTest;

/**
 * Drives staff the way a frontend does: discovery, a login with PKCE for one API, acting as one role
 * at a time, and the refusals when the person holds no role there.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IdentityProviderIT extends PostgresIntegrationTest {

    private static final String CLIENT_ID = "sgt-web-dev";
    private static final String POI_REDIRECT_URI = "http://localhost:5174/callback";
    private static final String API = "sgt-api-dev";
    private static final String FIELD_AGENT = "mcortes";
    private static final String FIELD_AGENT_ID = "13ae9db8-212f-466b-89ae-23e418ded4ac";
    private static final String DEVELOPER = "devuser";
    private static final String CODE_VERIFIER = "a-code-verifier-long-enough-for-pkce-0123456789abcdef";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtDecoder jwtDecoder;

    private final JsonMapper json = new JsonMapper();

    @Test
    void discoveryNamesTheIssuerAndItsKeys() throws Exception {
        this.mockMvc.perform(get("/.well-known/openid-configuration"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.issuer").value(ISSUER))
            .andExpect(jsonPath("$.jwks_uri").value(ISSUER + "/oauth2/jwks"));
    }

    @Test
    void aPersonHoldingOneRoleGetsATokenForTheApiActingAsIt() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        JsonNode tokens = this.tokensFor(this.authorize(session, "openid profile " + API));

        Jwt accessToken = this.jwtDecoder.decode(tokens.get("access_token").asString());
        assertThat(accessToken.getSubject()).isEqualTo(FIELD_AGENT_ID);
        assertThat(accessToken.getAudience()).containsExactly(API);
        assertThat(accessToken.getIssuer().toString()).isEqualTo(ISSUER);
        assertThat(accessToken.getClaimAsString("azp")).isEqualTo(CLIENT_ID);
        assertThat(accessToken.getClaimAsString("token_use")).isEqualTo("user");
        assertThat(accessToken.getClaimAsStringList("roles")).containsExactly("field-agent");
        assertThat(accessToken.getClaimAsStringList("eligible_roles")).containsExactly("field-agent");

        Jwt idToken = this.jwtDecoder.decode(tokens.get("id_token").asString());
        assertThat(idToken.getSubject()).isEqualTo(FIELD_AGENT_ID);
        assertThat(idToken.getClaimAsString("preferred_username")).isEqualTo(FIELD_AGENT);
        assertThat(idToken.getClaimAsString("job_title")).isEqualTo("Field agent");
        assertThat(idToken.getClaimAsString("department")).isEqualTo("Field operations");
        assertThat(idToken.getClaims()).doesNotContainKey("roles");
    }

    @Test
    void aPersonHoldingSeveralRolesPicksOneAndSwitchesWithoutLoggingInAgain() throws Exception {
        MockHttpSession session = this.logIn(DEVELOPER);

        Jwt undecided = this.accessToken(this.authorize(session, "openid " + API));
        assertThat(undecided.getClaimAsStringList("roles")).isEmpty();
        assertThat(undecided.getClaimAsStringList("eligible_roles"))
            .containsExactlyInAnyOrder("admin", "field-agent", "general-management-assistant");

        Jwt reviewing = this.accessToken(this.authorize(session, "openid " + API + " act-as:general-management-assistant"));
        assertThat(reviewing.getClaimAsStringList("roles")).containsExactly("general-management-assistant");

        Jwt submitting = this.accessToken(this.authorize(session, "openid " + API + " act-as:field-agent"));
        assertThat(submitting.getClaimAsStringList("roles")).containsExactly("field-agent");
    }

    @Test
    void eachFrontendLogsPeopleIntoItsOwnApi() throws Exception {
        MockHttpSession session = this.logIn(DEVELOPER);

        String redirect = this.authorize(session, "poi-web-dev", POI_REDIRECT_URI, "openid poi-api-dev act-as:planner");
        Jwt planning = this.accessToken("poi-web-dev", POI_REDIRECT_URI, redirect);

        assertThat(planning.getAudience()).containsExactly("poi-api-dev");
        assertThat(planning.getClaimAsStringList("roles")).containsExactly("planner");
        assertThat(planning.getClaimAsStringList("eligible_roles")).containsExactlyInAnyOrder("admin", "analyst", "planner");
        assertThat(errorOf(this.authorize(session, "openid poi-api-dev"))).isEqualTo("invalid_scope");
    }

    @Test
    void aRoleThePersonDoesNotHoldIsRefused() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        String redirect = this.authorize(session, "openid " + API + " act-as:general-management-assistant");

        assertThat(errorOf(redirect)).isEqualTo("access_denied");
    }

    @Test
    void aPersonWithNoRoleOnTheApiIsRefused() throws Exception {
        this.jdbc.update("DELETE FROM group_members WHERE person_id = (SELECT id FROM people WHERE username = ?)", "oserrano");
        MockHttpSession session = this.logIn("oserrano");

        String redirect = this.authorize(session, "openid " + API);

        assertThat(errorOf(redirect)).isEqualTo("access_denied");
    }

    @Test
    void aLoginNamingNoApiIsRefused() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        String redirect = this.authorize(session, "openid profile");

        assertThat(errorOf(redirect)).isEqualTo("invalid_scope");
    }

    @Test
    void aServiceGetsATokenForOneApiCarryingOnlyWhatItWasGrantedThere() throws Exception {
        String body = this.mockMvc.perform(post("/oauth2/token")
                .with(httpBasic(SERVICE_CLIENT_ID, SERVICE_CLIENT_SECRET))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials")
                .param("scope", "poi-api-dev tasks.read tasks.write")
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Jwt token = this.jwtDecoder.decode(this.json.readTree(body).get("access_token").asString());
        assertThat(token.getSubject()).isEqualTo(SERVICE_CLIENT_ID);
        assertThat(token.getAudience()).containsExactly("poi-api-dev");
        assertThat(token.getClaimAsString("token_use")).isEqualTo("service");
        assertThat(token.getClaimAsString("scope")).isEqualTo("tasks.read tasks.write");
        assertThat(token.getClaims()).doesNotContainKey("roles");
    }

    @Test
    void theLoginFormChecksThePasswordAgainstTheDirectory() throws Exception {
        this.mockMvc.perform(formLogin().user(FIELD_AGENT).password(DEV_PASSWORD))
            .andExpect(redirectedUrl("/"));
        this.mockMvc.perform(formLogin().user(FIELD_AGENT).password("wrong"))
            .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void aPersonWithoutAPasswordCannotLogIn() throws Exception {
        this.jdbc.update("UPDATE people SET password_hash = NULL WHERE username = ?", "lcaicedo");

        this.mockMvc.perform(formLogin().user("lcaicedo").password(DEV_PASSWORD))
            .andExpect(redirectedUrl("/login?error"));
    }

    private MockHttpSession logIn(String username) throws Exception {
        return (MockHttpSession) this.mockMvc.perform(formLogin().user(username).password(DEV_PASSWORD))
            .andReturn()
            .getRequest()
            .getSession();
    }

    private String authorize(MockHttpSession session, String scope) throws Exception {
        return this.authorize(session, CLIENT_ID, REDIRECT_URI, scope);
    }

    /** @return where staff sends the browser back to: a code, or an error */
    private String authorize(MockHttpSession session, String clientId, String redirectUri, String scope)
        throws Exception {
        return this.mockMvc.perform(get("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", scope)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", "state")
                .queryParam("code_challenge", codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse()
            .getRedirectedUrl();
    }

    private Jwt accessToken(String redirect) throws Exception {
        return this.accessToken(CLIENT_ID, REDIRECT_URI, redirect);
    }

    private Jwt accessToken(String clientId, String redirectUri, String redirect) throws Exception {
        return this.jwtDecoder.decode(this.tokensFor(clientId, redirectUri, redirect).get("access_token").asString());
    }

    private JsonNode tokensFor(String redirect) throws Exception {
        return this.tokensFor(CLIENT_ID, REDIRECT_URI, redirect);
    }

    private JsonNode tokensFor(String clientId, String redirectUri, String redirect) throws Exception {
        String code = parametersOf(redirect).getFirst("code");
        assertThat(code).as("code in %s", redirect).isNotNull();

        String body = this.mockMvc.perform(post("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "authorization_code")
                .param("code", code)
                .param("redirect_uri", redirectUri)
                .param("client_id", clientId)
                .param("code_verifier", CODE_VERIFIER)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return this.json.readTree(body);
    }

    private static String errorOf(String redirect) {
        return parametersOf(redirect).getFirst("error");
    }

    private static MultiValueMap<String, String> parametersOf(String redirect) {
        return UriComponentsBuilder.fromUriString(redirect).build().getQueryParams();
    }

    private static String codeChallenge() throws Exception {
        byte[] verifier = CODE_VERIFIER.getBytes(StandardCharsets.US_ASCII);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}

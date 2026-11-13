package lat.soulware.staff.identityprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
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
import org.springframework.web.util.HtmlUtils;
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
    private static final String POI_REDIRECT_URI = "http://localhost:4201/callback";
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
    void aBrowserWithoutASessionLogsInAndContinuesTheLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(get("/oauth2/authorize")
                .accept(MediaType.TEXT_HTML)
                .queryParam("response_type", "code")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("scope", "openid " + API)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "state")
                .queryParam("code_challenge", codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .session(session)
            )
            .andExpect(redirectedUrl("/login?continue"));
        this.mockMvc.perform(get("/login?continue").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Sign in to continue to SGT (Development).")));

        this.mockMvc.perform(post("/login?continue")
                .param("username", FIELD_AGENT)
                .param("password", DEV_PASSWORD)
                .with(csrf())
                .session(session)
            )
            .andExpect(redirectedUrl("/continuing?continue"));
        String notice = this.mockMvc.perform(get("/continuing?continue").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Redirecting to SGT (Development)")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String continueTo = HtmlUtils.htmlUnescape(notice.replaceFirst("(?s).*content=\"2;url=([^\"]+)\".*", "$1"));
        assertThat(continueTo).startsWith("http://localhost/oauth2/authorize?");
        String redirect = this.mockMvc.perform(get(URI.create(continueTo)).session(session))
            .andReturn()
            .getResponse()
            .getRedirectedUrl();
        assertThat(redirect).startsWith(REDIRECT_URI + "?code=");
    }

    @Test
    void theContinuingPageWithNothingToContinueOpensTheHub() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        this.mockMvc.perform(get("/continuing?continue").session(session))
            .andExpect(redirectedUrl("/"));
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
    void aLoginOpenedByHandIgnoresTheOneAnApplicationLeftAbandoned() throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(get("/oauth2/authorize")
                .accept(MediaType.TEXT_HTML)
                .queryParam("response_type", "code")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("scope", "openid " + API)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "state")
                .queryParam("code_challenge", codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .session(session)
            )
            .andExpect(redirectedUrl("/login?continue"));

        this.mockMvc.perform(get("/login").session(session))
            .andExpect(content().string(allOf(
                containsString("Use your institutional credentials."),
                not(containsString("SGT"))
            )));
        this.mockMvc.perform(post("/login")
                .param("username", FIELD_AGENT)
                .param("password", DEV_PASSWORD)
                .with(csrf())
                .session(session)
            )
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void theLoginFormSendsABrowserAlreadyLoggedInToTheHub() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        this.mockMvc.perform(get("/login").session(session))
            .andExpect(redirectedUrl("/"));
        this.mockMvc.perform(get("/login?continue").session(session))
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void theLoginFormSendsABrowserAlreadyLoggedInOnToTheApplicationItContinues() throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(get("/oauth2/authorize")
                .accept(MediaType.TEXT_HTML)
                .queryParam("response_type", "code")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("scope", "openid " + API)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "state")
                .queryParam("code_challenge", codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .session(session)
            )
            .andExpect(redirectedUrl("/login?continue"));
        this.mockMvc.perform(post("/login?continue")
                .param("username", FIELD_AGENT)
                .param("password", DEV_PASSWORD)
                .with(csrf())
                .session(session)
            )
            .andExpect(redirectedUrl("/continuing?continue"));

        this.mockMvc.perform(get("/login?continue").session(session))
            .andExpect(redirectedUrl("/continuing?continue"));
    }

    @Test
    void aSilentLoginWithoutASessionComesBackAskingForOne() throws Exception {
        String redirect = this.silentlyAuthorize(new MockHttpSession());

        assertThat(redirect).startsWith(REDIRECT_URI + "?");
        assertThat(errorOf(redirect)).isEqualTo("login_required");
    }

    @Test
    void aSilentLoginWithASessionComesBackWithACode() throws Exception {
        String redirect = this.silentlyAuthorize(this.logIn(FIELD_AGENT));

        assertThat(parametersOf(redirect).getFirst("code")).isNotNull();
    }

    @Test
    void aFailedContinuedLoginKeepsContinuing() throws Exception {
        this.mockMvc.perform(post("/login?continue")
                .param("username", FIELD_AGENT)
                .param("password", "wrong")
                .with(csrf())
            )
            .andExpect(redirectedUrl("/login?error&continue"));
    }

    @Test
    void theLoginFormLeavesProductionUnnamed() throws Exception {
        this.jdbc.update("UPDATE client_registrations SET environment = 'prod' WHERE client_id = ?", "poi-web-dev");
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(get("/oauth2/authorize")
                .accept(MediaType.TEXT_HTML)
                .queryParam("response_type", "code")
                .queryParam("client_id", "poi-web-dev")
                .queryParam("scope", "openid poi-api-dev")
                .queryParam("redirect_uri", POI_REDIRECT_URI)
                .queryParam("state", "state")
                .queryParam("code_challenge", codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .session(session)
            )
            .andExpect(redirectedUrl("/login?continue"));
        this.mockMvc.perform(get("/login?continue").session(session))
            .andExpect(content().string(containsString("Sign in to continue to POI.")));
    }

    @Test
    void theLoginFormOpenedDirectlyNamesNoApplication() throws Exception {
        this.mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Use your institutional credentials.")));
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

    @Test
    void aLoginNoApplicationAskedForLandsOnTheHubListingWhatThePersonCanOpen() throws Exception {
        MockHttpSession session = this.logIn(DEVELOPER);

        this.mockMvc.perform(get("/").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                containsString("Hello, Dev"),
                containsString("Dev User"),
                containsString("href=\"/opening?client=sgt-web-dev\""),
                containsString("href=\"/opening?client=poi-web-dev\""),
                containsString("Development")
            )));
    }

    @Test
    void theHubLeavesOutApplicationsThePersonHoldsNoRoleOn() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        this.mockMvc.perform(get("/").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                containsString("href=\"/opening?client=sgt-web-dev\""),
                not(containsString("poi-web-dev"))
            )));
    }

    @Test
    void aHubCardOpensItsApplicationThroughTheRedirectNotice() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        this.mockMvc.perform(get("/opening").queryParam("client", CLIENT_ID).session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                containsString("Redirecting to SGT (Development)"),
                containsString("content=\"2;url=http://localhost:4200/\"")
            )));
    }

    @Test
    void aHubCardForAnApplicationThePersonCannotOpenGoesBackToTheHub() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        this.mockMvc.perform(get("/opening").queryParam("client", "poi-web-dev").session(session))
            .andExpect(redirectedUrl("/"));
        this.mockMvc.perform(get("/opening").queryParam("client", "sgt-service-dev").session(session))
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void theHubTellsAPersonWithNoRoleAnywhereSo() throws Exception {
        this.jdbc.update("""
            INSERT INTO people (id, username, display_name, job_title, department, password_hash)
            SELECT '9a1b2c3d-4e5f-4a6b-8c7d-0e1f2a3b4c01', 'nogroups', 'No Groups', 'Visitor', 'Nowhere', password_hash
            FROM people WHERE username = ?""", FIELD_AGENT);
        MockHttpSession session = this.logIn("nogroups");

        this.mockMvc.perform(get("/").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No applications yet")));
    }

    @Test
    void theHubAsksABrowserWithoutASessionToLogIn() throws Exception {
        this.mockMvc.perform(get("/").accept(MediaType.TEXT_HTML))
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void signingOutOfTheHubEndsTheSession() throws Exception {
        MockHttpSession session = this.logIn(FIELD_AGENT);

        this.mockMvc.perform(post("/logout").with(csrf()).session(session))
            .andExpect(redirectedUrl("/login?logout"));
        this.mockMvc.perform(get("/").accept(MediaType.TEXT_HTML).session(session))
            .andExpect(redirectedUrl("/login"));
    }

    /** @return where staff sends the browser back to for an authorization request asking it to show nothing */
    private String silentlyAuthorize(MockHttpSession session) throws Exception {
        return this.mockMvc.perform(get("/oauth2/authorize")
                .accept(MediaType.TEXT_HTML)
                .queryParam("response_type", "code")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("scope", "openid " + API)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "state")
                .queryParam("code_challenge", codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .queryParam("prompt", "none")
                .session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse()
            .getRedirectedUrl();
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

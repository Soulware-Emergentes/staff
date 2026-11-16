package lat.soulware.staff.directory;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

import lat.soulware.staff.support.PostgresIntegrationTest;

/**
 * The directory API as other services read it, with a token they get for themselves through the
 * client credentials grant: pages of people, and GUID lookups whatever the job.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PersonApiIT extends PostgresIntegrationTest {

    private static final String FIELD_AGENT_ID = "13ae9db8-212f-466b-89ae-23e418ded4ac";
    private static final String DEVELOPER_ID = "5d1e0c3a-7b52-4a8e-9f1d-2c6b8e4a9d01";
    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = new JsonMapper();

    @Test
    void theApiDocsAreServedWithoutAToken() throws Exception {
        this.mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v3/people'].get.tags[0]").value("person"))
            .andExpect(jsonPath("$.components.securitySchemes['bearer-token'].scheme").value("bearer"))
            .andExpect(jsonPath("$.security[0]['bearer-token']").isArray());
        this.mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());
    }

    @Test
    void aPageNarrowsByJobAndName() throws Exception {
        this.mockMvc.perform(get("/api/v3/people")
                .header("Authorization", this.serviceToken())
                .queryParam("jobTitle", "field agent")
                .queryParam("username", "MCORT")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].id").value(FIELD_AGENT_ID))
            .andExpect(jsonPath("$.content[0].department").value("Field operations"));
    }

    @Test
    void aPageSortsByAColumnEitherWay() throws Exception {
        List<String> ascending = this.usernamesSorted("asc");
        List<String> descending = this.usernamesSorted("desc");

        assertTrue(ascending.size() > 1);
        assertEquals(ascending.reversed(), descending);
    }

    @Test
    void whatNoPageCanBeIsRefused() throws Exception {
        for (String[] parameter : List.of(
            new String[] {"sort", "id"},
            new String[] {"direction", "up"},
            new String[] {"size", "0"},
            new String[] {"size", "101"},
            new String[] {"page", "-1"}
        )) {
            this.mockMvc.perform(get("/api/v3/people")
                    .header("Authorization", this.serviceToken())
                    .queryParam(parameter[0], parameter[1])
                )
                .andExpect(status().isUnprocessableContent());
        }
    }

    @Test
    void aLookupResolvesAnyoneAndReportsWhatItMisses() throws Exception {
        String lookup = """
            {"ids": ["%s", "%s", "%s"]}""".formatted(FIELD_AGENT_ID, DEVELOPER_ID, UNKNOWN_ID);

        this.mockMvc.perform(post("/api/v3/people/lookup")
                .header("Authorization", this.serviceToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(lookup)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.found.length()").value(2))
            .andExpect(jsonPath("$.notFound[0]").value(UNKNOWN_ID));
    }

    @Test
    void aRequestWithoutAServiceTokenIsRefused() throws Exception {
        this.mockMvc.perform(get("/api/v3/people"))
            .andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/api/v3/people").header("Authorization", "Bearer not-a-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void aRequestWithoutATokenIsAnsweredWithTheUsualEnvelope() throws Exception {
        this.mockMvc.perform(get("/api/v3/people"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void aTokenForAnotherApiIsRefused() throws Exception {
        String poiToken = this.serviceToken("poi-api-dev tasks.read");

        this.mockMvc.perform(get("/api/v3/people").header("Authorization", poiToken))
            .andExpect(status().isUnauthorized());
    }

    /** Usernames are unique, so the id that breaks ties never reorders this list. */
    private List<String> usernamesSorted(String direction) throws Exception {
        String page = this.mockMvc.perform(get("/api/v3/people")
                .header("Authorization", this.serviceToken())
                .queryParam("sort", "username")
                .queryParam("direction", direction)
                .queryParam("size", "100")
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<String> usernames = new ArrayList<>();
        this.json.readTree(page).get("content").forEach(person -> usernames.add(person.get("username").asString()));
        return usernames;
    }

    private String serviceToken() throws Exception {
        return this.serviceToken("staff-directory-api-dev people.read");
    }

    /** Gets a token the way a service does: its client id and secret, and the API and scopes it wants. */
    private String serviceToken(String scope) throws Exception {
        String body = this.mockMvc.perform(post("/oauth2/token")
                .with(httpBasic(SERVICE_CLIENT_ID, SERVICE_CLIENT_SECRET))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials")
                .param("scope", scope)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return "Bearer " + this.json.readTree(body).get("access_token").asString();
    }
}

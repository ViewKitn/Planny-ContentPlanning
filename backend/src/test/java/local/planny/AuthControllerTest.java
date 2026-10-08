package local.planny;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {
    private final CloudAccess access = new CloudAccess(true, "synthetic-test-password");
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthController(access))
        .addFilters(new LocalRequestFilter(access, "https://planny.example")).build();
    @Test void signsInSetsPrivateCookieAndRestoresSession() throws Exception {
        mvc.perform(get("/api/auth/session")).andExpect(status().isOk()).andExpect(jsonPath("$.required").value(true)).andExpect(jsonPath("$.authenticated").value(false));
        var result = mvc.perform(post("/api/auth/login").header("Origin", "https://planny.example").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"planny\",\"password\":\"synthetic-test-password\"}"))
            .andExpect(status().isOk()).andExpect(header().doesNotExist("WWW-Authenticate")).andReturn();
        String cookie = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(cookie);
        assertTrue(cookie.contains("HttpOnly"));assertTrue(cookie.contains("Secure"));assertTrue(cookie.contains("SameSite=Strict"));assertTrue(cookie.contains("Path=/"));
        assertFalse(cookie.contains("synthetic-test-password"));
        String token = cookie.substring(cookie.indexOf('=')+1,cookie.indexOf(';'));
        mvc.perform(get("/api/auth/session").cookie(new Cookie(CloudAccess.COOKIE,token))).andExpect(jsonPath("$.authenticated").value(true));
    }
    @Test void wrongCredentialsAndForeignOriginAreRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"other\",\"password\":\"synthetic-test-password\"}"))
            .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("Set-Cookie")).andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"planny\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").header("Origin", "https://foreign.example").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }
    @Test void logoutClearsSessionCookie() throws Exception {
        var result = mvc.perform(post("/api/auth/logout")).andExpect(status().isOk()).andReturn();
        assertTrue(result.getResponse().getHeader("Set-Cookie").contains("Max-Age=0"));
    }
}

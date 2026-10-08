package local.planny;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class CloudAccessTest {
    private final String password = "synthetic-test-password";
    private final LocalRequestFilter cloud = new LocalRequestFilter(true, password, "https://planny.example");

    private int request(LocalRequestFilter filter, String authorization, String origin, boolean expectedPass) throws Exception {
        var request = new MockHttpServletRequest("PUT", "/api/state");
        if (authorization != null) request.addHeader("Authorization", authorization);
        if (origin != null) request.addHeader("Origin", origin);
        var response = new MockHttpServletResponse();
        var passed = new AtomicBoolean();
        filter.doFilter(request, response, (req, res) -> passed.set(true));
        assertEquals(expectedPass, passed.get());
        return response.getStatus();
    }
    private String auth(String value) { return "Basic " + Base64.getEncoder().encodeToString(("planny:" + value).getBytes(StandardCharsets.UTF_8)); }
    @Test void rejectsAnonymousAndMalformedCredentials() throws Exception {
        assertEquals(401, request(cloud, null, null, false));
        assertEquals(401, request(cloud, "Basic not-base64!", null, false));
        assertEquals(401, request(cloud, auth("wrong"), null, false));
    }
    @Test void acceptsPasswordAndConfiguredOrigin() throws Exception {
        assertEquals(200, request(cloud, auth(password), "https://planny.example", true));
    }
    @Test void rejectsOtherOriginsEvenWithPassword() throws Exception {
        assertEquals(403, request(cloud, auth(password), "https://untrusted.example", false));
    }
    @Test void requiresStrongCloudPassword() {
        assertThrows(IllegalArgumentException.class, () -> new LocalRequestFilter(true, "", ""));
        assertThrows(IllegalArgumentException.class, () -> new LocalRequestFilter(true, "short", ""));
    }
    @Test void localModeRetainsAccountFreeLoopbackAccess() throws Exception {
        var local = new LocalRequestFilter(false, "", "");
        assertEquals(200, request(local, null, "http://127.0.0.1:18080", true));
        assertEquals(403, request(local, null, "https://untrusted.example", false));
    }
}

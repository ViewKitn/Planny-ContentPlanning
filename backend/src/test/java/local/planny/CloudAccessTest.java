package local.planny;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class CloudAccessTest {
    private final String password = "synthetic-test-password";
    private final CloudAccess access = new CloudAccess(true, password);
    private final LocalRequestFilter cloud = new LocalRequestFilter(access, "https://planny.example");

    private int request(LocalRequestFilter filter, String authorization, String origin, boolean expectedPass) throws Exception {
        var request = new MockHttpServletRequest("PUT", "/api/state");
        if (authorization != null) request.setCookies(new Cookie(CloudAccess.COOKIE, authorization));
        if (origin != null) request.addHeader("Origin", origin);
        var response = new MockHttpServletResponse();
        var passed = new AtomicBoolean();
        filter.doFilter(request, response, (req, res) -> passed.set(true));
        assertNull(response.getHeader("WWW-Authenticate"));
        assertEquals(expectedPass, passed.get());
        return response.getStatus();
    }
    private String auth(String value) { return new CloudAccess(true, value).token(java.time.Instant.now().getEpochSecond()); }
    @Test void rejectsAnonymousAndMalformedCredentials() throws Exception {
        assertEquals(401, request(cloud, null, null, false));
        assertEquals(401, request(cloud, "Basic not-base64!", null, false));
        assertEquals(401, request(cloud, auth("wrong-test-password"), null, false));
    }
    @Test void acceptsPasswordAndConfiguredOrigin() throws Exception {
        assertEquals(200, request(cloud, auth(password), "https://planny.example", true));
    }
    @Test void rejectsOtherOriginsEvenWithPassword() throws Exception {
        assertEquals(403, request(cloud, auth(password), "https://untrusted.example", false));
    }
    @Test void requiresStrongCloudPassword() {
        assertThrows(IllegalArgumentException.class, () -> new CloudAccess(true, ""));
        assertThrows(IllegalArgumentException.class, () -> new CloudAccess(true, "short"));
    }
    @Test void localModeRetainsAccountFreeLoopbackAccess() throws Exception {
        var local = new LocalRequestFilter(new CloudAccess(false, ""), "");
        assertEquals(200, request(local, null, "http://127.0.0.1:18080", true));
        assertEquals(403, request(local, null, "https://untrusted.example", false));
    }
    @Test void loginPageAndAssetsArePublicButApiIsProtected() throws Exception {
        for (String path : new String[]{"/", "/assets/index.js", "/api/auth/session"}) {
            var req = new MockHttpServletRequest("GET", path);
            var res = new MockHttpServletResponse();
            var passed = new AtomicBoolean();
            cloud.doFilter(req, res, (r, s) -> passed.set(true));
            assertTrue(passed.get(), path);
            assertNull(res.getHeader("WWW-Authenticate"));
        }
        var req = new MockHttpServletRequest("GET", "/api/backup");
        req.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(("planny:" + password).getBytes(StandardCharsets.UTF_8)));
        var res = new MockHttpServletResponse();
        cloud.doFilter(req, res, (r, s) -> fail("Basic auth must not grant access"));
        assertEquals(401, res.getStatus());
    }
    @Test void sessionsExpireRejectTamperingAndWorkAcrossInstances() {
        long now = java.time.Instant.now().getEpochSecond();
        String token = access.token(now);
        assertTrue(new CloudAccess(true, password).validToken(token, now));
        assertFalse(access.validToken(token + "x", now));
        assertFalse(access.validToken(token, now + CloudAccess.LIFETIME_SECONDS));
        assertFalse(new CloudAccess(true, "changed-test-password").validToken(token, now));
    }
}

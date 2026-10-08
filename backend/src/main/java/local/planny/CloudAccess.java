package local.planny;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/** Signed cookies work across container instances without storing passwords in the browser. */
@Component
public class CloudAccess {
    static final String COOKIE = "planny_session";
    static final long LIFETIME_SECONDS = 12 * 60 * 60;
    private final boolean required;
    private final byte[] credentials;
    private final byte[] key;

    public CloudAccess(@Value("${planny.cloud-enabled:false}") boolean required,
                       @Value("${planny.access-password:}") String password) {
        if (required && password.length() < 16) throw new IllegalArgumentException("Cloud access password must contain at least 16 characters");
        this.required = required;
        credentials = ("planny:" + password).getBytes(StandardCharsets.UTF_8);
        key = ("Planny session v1:" + password).getBytes(StandardCharsets.UTF_8);
    }
    public boolean required() { return required; }
    public boolean validCredentials(String username, String password) {
        if (!"planny".equals(username) || password == null) return false;
        return MessageDigest.isEqual(credentials, (username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
    public boolean authenticated(HttpServletRequest request) {
        if (!required) return true;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && validToken(cookie.getValue(), Instant.now().getEpochSecond())) return true;
        }
        return false;
    }
    String token(long now) {
        String payload = (now + LIFETIME_SECONDS) + ":" + UUID.randomUUID();
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + signature(encoded);
    }
    boolean validToken(String token, long now) {
        try {
            if (token == null || token.length() > 256) return false;
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2 || !MessageDigest.isEqual(signature(parts[0]).getBytes(StandardCharsets.US_ASCII), parts[1].getBytes(StandardCharsets.US_ASCII))) return false;
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            long expires = Long.parseLong(payload.substring(0, payload.indexOf(':')));
            return expires > now && expires <= now + LIFETIME_SECONDS;
        } catch (IllegalArgumentException ex) { return false; }
    }
    private String signature(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));
        } catch (java.security.GeneralSecurityException ex) { throw new IllegalStateException("Session signing unavailable", ex); }
    }
    public String sessionCookie() { return cookie(token(Instant.now().getEpochSecond()), LIFETIME_SECONDS); }
    public String clearCookie() { return cookie("", 0); }
    private String cookie(String value, long maxAge) {
        return ResponseCookie.from(COOKIE, value).httpOnly(true).secure(required).sameSite("Strict").path("/").maxAge(maxAge).build().toString();
    }
}

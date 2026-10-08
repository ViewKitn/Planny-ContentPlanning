package local.planny;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

@Component
public class LocalRequestFilter extends OncePerRequestFilter {
    private final boolean cloud;
    private final byte[] credentials;
    private final Set<String> allowedOrigins;

    public LocalRequestFilter(@Value("${planny.cloud-enabled:false}") boolean cloud,
                              @Value("${planny.access-password:}") String password,
                              @Value("${planny.allowed-origins:}") String origins) {
        if (cloud && password.length() < 16) throw new IllegalArgumentException("Cloud access password must contain at least 16 characters");
        this.cloud = cloud;
        this.credentials = ("planny:" + password).getBytes(StandardCharsets.UTF_8);
        this.allowedOrigins = new HashSet<>(List.of(origins.split(",")));
        for (String key : List.of("VERCEL_URL", "VERCEL_PROJECT_PRODUCTION_URL")) {
            String host = System.getenv(key);
            if (host != null && !host.isBlank()) allowedOrigins.add("https://" + host);
        }
    }

    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Cache-Control", "no-store");
        if (cloud && !authenticated(req.getHeader("Authorization"))) {
            res.setHeader("WWW-Authenticate", "Basic realm=\"Planny\", charset=\"UTF-8\"");
            res.setStatus(401); return;
        }
        String origin = req.getHeader("Origin");
        if (origin != null) {
            try {
                URI uri = URI.create(origin);
                boolean accepted = cloud ? allowedOrigins.contains(origin) :
                    "http".equals(uri.getScheme()) && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost()));
                if (!accepted) {
                    res.sendError(403); return;
                }
            } catch (IllegalArgumentException ex) { res.sendError(403); return; }
        }
        chain.doFilter(req, res);
    }

    private boolean authenticated(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) return false;
        try {
            return MessageDigest.isEqual(credentials, Base64.getDecoder().decode(authorization.substring(6)));
        } catch (IllegalArgumentException ex) { return false; }
    }
}

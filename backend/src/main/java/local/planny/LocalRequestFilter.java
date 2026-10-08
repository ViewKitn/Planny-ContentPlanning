package local.planny;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

@Component
public class LocalRequestFilter extends OncePerRequestFilter {
    private final boolean cloud;
    private final CloudAccess access;
    private final Set<String> allowedOrigins;

    public LocalRequestFilter(CloudAccess access,
                              @Value("${planny.allowed-origins:}") String origins) {
        this.cloud = access.required();
        this.access = access;
        this.allowedOrigins = new HashSet<>(List.of(origins.split(",")));
        for (String key : List.of("VERCEL_URL", "VERCEL_PROJECT_PRODUCTION_URL")) {
            String host = System.getenv(key);
            if (host != null && !host.isBlank()) allowedOrigins.add("https://" + host);
        }
    }

    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Cache-Control", "no-store");
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
        String path = req.getServletPath();
        if (path.isEmpty()) path = req.getRequestURI();
        boolean api = path.equals("/api") || path.startsWith("/api/");
        boolean authRoute = path.equals("/api/auth/session") || path.equals("/api/auth/login") || path.equals("/api/auth/logout");
        if (cloud && api && !authRoute && !access.authenticated(req)) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"กรุณาเข้าสู่ระบบเพื่อเปิดพื้นที่ของคุณ\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}

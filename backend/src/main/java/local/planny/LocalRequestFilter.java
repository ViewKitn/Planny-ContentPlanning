package local.planny;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.net.URI;

@Component
public class LocalRequestFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String origin = req.getHeader("Origin");
        if (origin != null) {
            try {
                URI uri = URI.create(origin);
                if (!("http".equals(uri.getScheme()) && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost())))) {
                    res.sendError(403); return;
                }
            } catch (IllegalArgumentException ex) { res.sendError(403); return; }
        }
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, res);
    }
}

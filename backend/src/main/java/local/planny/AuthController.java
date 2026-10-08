package local.planny;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CloudAccess access;
    public AuthController(CloudAccess access) { this.access = access; }
    public record LoginRequest(String username, String password) {}
    @GetMapping("/session") public Map<String, Boolean> session(HttpServletRequest request) {
        return Map.of("required", access.required(), "authenticated", access.authenticated(request));
    }
    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody LoginRequest input) {
        if (access.required() && !access.validCredentials(input.username(), input.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง กรุณาลองใหม่"));
        }
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, access.sessionCookie()).body(Map.of("authenticated", true));
    }
    @PostMapping("/logout") public ResponseEntity<?> logout() {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, access.clearCookie()).body(Map.of("authenticated", false));
    }
}

package local.planny;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkspaceController {
    private final WorkspaceStore store;
    public WorkspaceController(WorkspaceStore store) { this.store = store; }
    @GetMapping("/state") public Workspace.Snapshot state() { return store.read(); }
    @PutMapping("/state") public Workspace.Snapshot save(@RequestBody Workspace.Snapshot snapshot) { return store.save(snapshot); }
    @GetMapping("/backup") public Workspace.Backup backup() { return new Workspace.Backup(1, store.read().data()); }
    public record ImportRequest(long revision, Workspace.Backup backup) {}
    @PostMapping("/import") public Workspace.Snapshot restore(@RequestBody ImportRequest request) {
        if (request.backup() == null || request.backup().schemaVersion() != 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "เวอร์ชันไฟล์สำรองไม่รองรับ");
        return store.save(new Workspace.Snapshot(request.revision(), request.backup().data()));
    }
    @GetMapping("/health") public Map<String, String> health() { store.read(); return Map.of("status", "ok"); }
}

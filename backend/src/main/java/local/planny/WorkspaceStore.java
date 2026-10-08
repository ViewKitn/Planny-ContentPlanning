package local.planny;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class WorkspaceStore {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final WorkspaceValidator validator;
    public WorkspaceStore(JdbcTemplate jdbc, ObjectMapper mapper, WorkspaceValidator validator) {
        this.jdbc = jdbc; this.mapper = mapper; this.validator = validator;
    }
    public Workspace.Snapshot read() {
        return jdbc.queryForObject("SELECT revision, document::text FROM workspace WHERE id=1", (rs, row) -> {
            try { return new Workspace.Snapshot(rs.getLong(1), mapper.readValue(rs.getString(2), Workspace.class)); }
            catch (Exception ex) { throw new IllegalStateException("Cannot read workspace", ex); }
        });
    }
    @Transactional
    public Workspace.Snapshot save(Workspace.Snapshot snapshot) {
        validator.validate(snapshot.data());
        try {
            String json = mapper.writeValueAsString(snapshot.data());
            if (json.length() > 8_000_000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ข้อมูลเกินขนาด 8 MB");
            int changed = jdbc.update("UPDATE workspace SET document=CAST(? AS jsonb), revision=revision+1, updated_at=now() WHERE id=1 AND revision=?", json, snapshot.revision());
            if (changed == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "ข้อมูลเปลี่ยนจากหน้าต่างอื่น กรุณาโหลดข้อมูลล่าสุดก่อนบันทึก");
            return new Workspace.Snapshot(snapshot.revision() + 1, snapshot.data());
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) { throw new IllegalStateException(ex); }
    }
}

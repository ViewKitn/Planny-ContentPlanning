package local.planny;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named="PLANNY_TEST_DB_URL",matches=".*planny_test.*")
class WorkspaceIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired WorkspaceStore store;
    @Autowired ObjectMapper mapper;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("PLANNY_TEST_DB_URL"));
    }
    @Test void savesToPostgresAndRejectsStaleRevision() throws Exception {
        var snapshot=store.read();
        var fixture=new WorkspaceValidatorTest().workspace("Published",null,"2026-10-09T07:00:00Z", "https://example.com/post");
        var write=new Workspace.Snapshot(snapshot.revision(),fixture);
        mvc.perform(put("/api/state").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(write))).andExpect(status().isOk());
        assertEquals("ไอเดีย",store.read().data().contents().getFirst().title());
        mvc.perform(put("/api/state").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(write))).andExpect(status().isConflict());
    }
    @Test void invalidImportLeavesExistingDataUnchanged() throws Exception {
        var initial=store.read();
        var bad=new WorkspaceValidatorTest().workspace("Planned",null,null, "");
        var body=new WorkspaceController.ImportRequest(initial.revision(),new Workspace.Backup(1,bad));
        mvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
        assertEquals(initial,store.read());
    }
    @Test void backupCanRoundTripAllMetadata() throws Exception {
        var initial=store.read();
        var fixture=new WorkspaceValidatorTest().workspace("Published","2026-10-08T00:00:00Z","2026-10-09T07:00:00Z", "https://example.com/post");
        var saved=store.save(new Workspace.Snapshot(initial.revision(),fixture));
        var backup=mapper.readValue(mvc.perform(get("/api/backup")).andReturn().getResponse().getContentAsString(),Workspace.Backup.class);
        mvc.perform(post("/api/import").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(new WorkspaceController.ImportRequest(saved.revision(),backup)))).andExpect(status().isOk());
        assertEquals(fixture,store.read().data());
    }
    @Test void externalOriginCannotModifyWorkspace() throws Exception {
        mvc.perform(put("/api/state").header("Origin","https://example.com").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }
}

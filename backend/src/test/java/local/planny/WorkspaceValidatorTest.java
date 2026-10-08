package local.planny;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WorkspaceValidatorTest {
    private final WorkspaceValidator validator = new WorkspaceValidator();
    private final ObjectMapper mapper = new ObjectMapper();
    Workspace workspace(String status, String planned, String actual, String post) throws Exception {
        var doc = mapper.readTree("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\"}]}");
        var destinations = List.of(new Workspace.Destination("facebook", "caption", List.of(), post));
        var c = new Workspace.Content("content-1", "ไอเดีย", status, doc, doc, List.of(), List.of(), destinations, planned, actual, false, null, "2026-10-09T00:00:00Z", "2026-10-09T00:00:00Z");
        return new Workspace(List.of(c), List.of(), List.of(new Workspace.Platform("facebook", "Facebook", true, "")), "Asia/Bangkok");
    }
    @Test void plannedRequiresSchedule() throws Exception {
        var w = workspace("Planned", null, null, "");
        assertThrows(ResponseStatusException.class, () -> validator.validate(w));
    }
    @Test void directPublishedDoesNotRequireOldPlanOrLink() throws Exception {
        validator.validate(workspace("Published", null, "2026-10-09T07:00:00Z", ""));
    }
    @Test void unpublishedCannotRetainPublicationMetadata() throws Exception {
        var w = workspace("Draft", null, "2026-10-09T07:00:00Z", "https://example.com/post");
        assertThrows(ResponseStatusException.class, () -> validator.validate(w));
    }
    @Test void invalidLinkCannotBeImported() throws Exception {
        var w = workspace("Published", null, "2026-10-09T07:00:00Z", "javascript:alert(1)");
        assertThrows(ResponseStatusException.class, () -> validator.validate(w));
    }
    @Test void pastedLinkWhitespaceDoesNotRejectValidHttpLink() throws Exception {
        validator.validate(workspace("Published",null,"2026-10-09T07:00:00Z", "  https://example.com/post  "));
    }
    @Test void platformProfileRejectsExecutableUrlsAndAcceptsLegacyBackups() throws Exception {
        var good=workspace("Idea",null,null, "");
        var invalid=new Workspace(good.contents(),good.tags(),List.of(new Workspace.Platform("facebook","Facebook",true,"javascript:alert(1)")),good.timezone());
        assertThrows(ResponseStatusException.class,()->validator.validate(invalid));
        validator.validate(new Workspace(good.contents(),good.tags(),List.of(new Workspace.Platform("facebook","Facebook",true,null)),good.timezone()));
    }
    @Test void invalidTimezoneAndDuplicateIdsRejected() throws Exception {
        var good=workspace("Idea", null, null, "");
        assertThrows(ResponseStatusException.class, () -> validator.validate(new Workspace(good.contents(), good.tags(), good.platforms(), "Invalid/Zone")));
        assertThrows(ResponseStatusException.class, () -> validator.validate(new Workspace(List.of(good.contents().getFirst(),good.contents().getFirst()), good.tags(), good.platforms(), "UTC")));
    }
    @Test void archivedAndTrashedContentRetainsDestinations() throws Exception {
        var good=workspace("Published", "2026-10-08T00:00:00Z", "2026-10-09T00:00:00Z", "https://example.com/post");
        var c=good.contents().getFirst();
        var trashed=new Workspace.Content(c.id(),c.title(),c.status(),c.brief(),c.script(),c.referenceLinks(),c.tagIds(),c.destinations(),c.plannedAt(),c.publishedAt(),true,"2026-10-10T00:00:00Z",c.createdAt(),c.updatedAt());
        validator.validate(new Workspace(List.of(trashed),good.tags(),good.platforms(),good.timezone()));
    }
}

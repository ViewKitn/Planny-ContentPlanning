package local.planny;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Component
public class WorkspaceValidator {
    private static final Set<String> STATUSES = Set.of("Idea", "Draft", "Ready", "Planned", "Published");
    private static final Set<String> NODES = Set.of("doc", "paragraph", "heading", "text", "bulletList", "orderedList", "listItem", "hardBreak");

    public void validate(Workspace w) {
        require(w != null, "ไม่มีข้อมูล workspace");
        try { ZoneId.of(w.timezone()); } catch (Exception ex) { fail("เขตเวลาไม่ถูกต้อง"); }
        var tags = ids(w.tags(), Workspace.Tag::id, 2000);
        var platforms = ids(w.platforms(), Workspace.Platform::id, 100);
        ids(w.contents(), Workspace.Content::id, 10000);
        w.tags().forEach(t -> text(t.name(), 80, true));
        w.platforms().forEach(p -> { text(p.name(), 80, true); link(p.pageUrl()); });
        uniqueNames(w.tags(), Workspace.Tag::name);
        uniqueNames(w.platforms(), Workspace.Platform::name);
        for (var c : w.contents()) {
            text(c.title(), 240, true);
            require(STATUSES.contains(c.status()), "สถานะไม่ถูกต้อง");
            rich(c.brief(), 0); rich(c.script(), 0);
            links(c.referenceLinks());
            require(c.tagIds() != null && new HashSet<>(c.tagIds()).size() == c.tagIds().size() && tags.containsAll(c.tagIds()), "Tag ไม่ถูกต้อง");
            require(c.destinations() != null && c.destinations().size() <= 100, "แพลตฟอร์มไม่ถูกต้อง");
            var selected = new HashSet<String>();
            for (var d : c.destinations()) {
                require(d != null && platforms.contains(d.platformId()) && selected.add(d.platformId()), "แพลตฟอร์มไม่ถูกต้องหรือซ้ำ");
                text(d.caption(), 100000, false); links(d.assetLinks()); link(d.postLink());
            }
            date(c.createdAt(), true); date(c.updatedAt(), true); date(c.deletedAt(), false);
            date(c.plannedAt(), false); date(c.publishedAt(), false);
            if ("Planned".equals(c.status())) require(!selected.isEmpty() && present(c.plannedAt()), "Planned ต้องมีแพลตฟอร์มและกำหนดเผยแพร่");
            if ("Published".equals(c.status())) require(!selected.isEmpty() && present(c.publishedAt()), "Published ต้องมีแพลตฟอร์มและเวลาเผยแพร่จริง");
            else {
                require(!present(c.publishedAt()), "งานที่ยังไม่ Published ต้องไม่มีเวลาเผยแพร่จริง");
                require(c.destinations().stream().noneMatch(d -> present(d.postLink())), "งานที่ยังไม่ Published ต้องไม่มีลิงก์โพสต์");
            }
        }
    }

    private <T> Set<String> ids(List<T> items, Function<T, String> id, int max) {
        require(items != null && items.size() <= max, "จำนวนรายการไม่ถูกต้อง");
        var result = new HashSet<String>();
        for (T item : items) {
            require(item != null, "รายการว่างไม่ถูกต้อง");
            String value = id.apply(item);
            require(value != null && value.matches("[a-zA-Z0-9_-]{1,80}") && result.add(value), "ID ไม่ถูกต้องหรือซ้ำ");
        }
        return result;
    }
    private <T> void uniqueNames(List<T> items, Function<T, String> name) {
        var names = new HashSet<String>();
        for (T item : items) require(names.add(name.apply(item).strip().toLowerCase(Locale.ROOT)), "ชื่อซ้ำ");
    }
    private void rich(JsonNode node, int depth) {
        require(node != null && node.isObject() && depth <= 20 && node.toString().length() <= 200000, "รูปแบบเนื้อหาไม่ถูกต้อง");
        require(NODES.contains(node.path("type").asText()), "ชนิดข้อความไม่รองรับ");
        if (depth == 0) require("doc".equals(node.path("type").asText()), "เนื้อหาต้องเป็นเอกสาร");
        if (node.has("text")) text(node.get("text").asText(), 100000, false);
        if (node.has("content")) {
            require(node.get("content").isArray(), "รูปแบบเนื้อหาไม่ถูกต้อง");
            for (var child : node.get("content")) rich(child, depth + 1);
        }
        if (node.has("marks")) {
            require(node.get("marks").isArray(), "รูปแบบข้อความไม่ถูกต้อง");
            for (var mark : node.get("marks")) require("bold".equals(mark.path("type").asText()), "รูปแบบข้อความไม่รองรับ");
        }
    }
    private void links(List<String> links) {
        require(links != null && links.size() <= 100, "รายการลิงก์ไม่ถูกต้อง");
        links.forEach(this::link);
    }
    private void link(String value) {
        if (!present(value)) return;
        text(value, 2048, false);
        try {
            var uri = URI.create(value.strip());
            require(Set.of("http", "https").contains(uri.getScheme()) && uri.getHost() != null, "ลิงก์ต้องเป็น http หรือ https");
        } catch (IllegalArgumentException ex) { fail("ลิงก์ไม่ถูกต้อง"); }
    }
    private void date(String value, boolean required) {
        if (!present(value)) { require(!required, "ไม่มีวันที่"); return; }
        try { OffsetDateTime.parse(value); } catch (Exception ex) { fail("วันเวลาไม่ถูกต้อง"); }
    }
    private void text(String value, int max, boolean required) {
        require(value != null && value.length() <= max && (!required || !value.isBlank()), "ข้อความว่างหรือยาวเกินกำหนด");
    }
    private boolean present(String value) { return value != null && !value.isBlank(); }
    private void require(boolean test, String message) { if (!test) fail(message); }
    private void fail(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}

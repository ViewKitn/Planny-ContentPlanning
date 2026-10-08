package local.planny;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record Workspace(List<Content> contents, List<Tag> tags, List<Platform> platforms, String timezone) {
    public record Tag(String id, String name) {}
    public record Platform(String id, String name, boolean active, String pageUrl) {}
    public record Destination(String platformId, String caption, List<String> assetLinks, String postLink) {}
    public record Content(String id, String title, String status, JsonNode brief, JsonNode script,
                          List<String> referenceLinks, List<String> tagIds, List<Destination> destinations,
                          String plannedAt, String publishedAt, boolean archived, String deletedAt,
                          String createdAt, String updatedAt) {}
    public record Snapshot(long revision, Workspace data) {}
    public record Backup(int schemaVersion, Workspace data) {}
}

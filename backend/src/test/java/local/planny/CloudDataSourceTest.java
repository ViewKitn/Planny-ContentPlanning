package local.planny;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CloudDataSourceTest {
    @Test void parsesMarketplaceUrlWithoutPuttingCredentialsInJdbcUrl() {
        try (var database = new HikariDataSource()) {
            CloudDataSource.configure(database, "postgresql://creator:p%40ss%3Aword+plus@db.example:5432/planny?sslmode=require", "", "");
            assertEquals("jdbc:postgresql://db.example:5432/planny?sslmode=require", database.getJdbcUrl());
            assertEquals("creator", database.getUsername());
            assertEquals("p@ss:word+plus", database.getPassword());
        }
    }
    @Test void retainsExplicitJdbcConfiguration() {
        try (var database = new HikariDataSource()) {
            CloudDataSource.configure(database, "jdbc:postgresql://db.example/planny?sslmode=require", "creator", "synthetic-password");
            assertEquals("creator", database.getUsername());
            assertEquals("synthetic-password", database.getPassword());
        }
    }
    @Test void rejectsInvalidUrlsWithoutLeakingSecrets() {
        try (var database = new HikariDataSource()) {
            var error = assertThrows(IllegalArgumentException.class, () -> CloudDataSource.configure(database, "https://creator:secret@example.com/db", "", ""));
            assertFalse(error.getMessage().contains("secret"));
            assertNull(error.getCause());
            assertThrows(IllegalArgumentException.class, () -> CloudDataSource.configure(database, "", "", ""));
        }
    }
}

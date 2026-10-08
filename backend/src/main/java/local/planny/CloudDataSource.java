package local.planny;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("cloud")
public class CloudDataSource {
    @Bean
    public HikariDataSource cloudDatabase(@Value("${planny.database-url}") String url,
                                         @Value("${planny.database-user}") String user,
                                         @Value("${planny.database-password}") String password) {
        var database = new HikariDataSource();
        configure(database, url, user, password);
        database.setMaximumPoolSize(3);
        database.setMinimumIdle(0);
        return database;
    }

    static void configure(HikariDataSource database, String url, String user, String password) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Cloud database URL is required");
        if (url.startsWith("jdbc:postgresql://")) {
            database.setJdbcUrl(url);
            database.setUsername(user);
            database.setPassword(password);
            return;
        }
        try {
            URI uri = URI.create(url);
            if (!("postgres".equals(uri.getScheme()) || "postgresql".equals(uri.getScheme())) || uri.getHost() == null || uri.getRawUserInfo() == null) throw new IllegalArgumentException();
            String[] credentials = uri.getRawUserInfo().split(":", 2);
            if (credentials.length != 2) throw new IllegalArgumentException();
            String host = uri.getHost().contains(":") && !uri.getHost().startsWith("[") ? "[" + uri.getHost() + "]" : uri.getHost();
            database.setJdbcUrl("jdbc:postgresql://" + host + (uri.getPort() == -1 ? "" : ":" + uri.getPort()) + uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery()));
            database.setUsername(decode(credentials[0]));
            database.setPassword(decode(credentials[1]));
        } catch (IllegalArgumentException ex) {
            // Do not include the input URL or nested exception: they can contain credentials.
            throw new IllegalArgumentException("Cloud database URL must be a PostgreSQL connection string");
        }
    }
    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}

package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String jdbcUrl = toJdbcUrl(rawUrl);

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }

    private String toJdbcUrl(String url) {
        if (url == null) return url;
        // Already a JDBC URL — return as-is
        if (url.startsWith("jdbc:")) return url;
        // Convert plain mysql://user:pass@host:port/db to jdbc:mysql://host:port/db?params
        try {
            // Strip the scheme and re-parse as a generic URI
            String normalized = url.replaceFirst("^mysql://", "http://");
            URI uri = URI.create(normalized);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String db = uri.getPath() != null ? uri.getPath().replaceFirst("^/", "") : "";
            return "jdbc:mysql://" + host + ":" + port + "/" + db
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        } catch (Exception e) {
            // Fallback: prepend jdbc: and hope for the best
            return "jdbc:" + url;
        }
    }
}

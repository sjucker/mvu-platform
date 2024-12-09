package ch.mvurdorf.platform.config;

import ch.mvurdorf.platform.service.CloudStorageService;
import ch.mvurdorf.platform.service.StorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@Profile("!local")
@EnableConfigurationProperties({CloudStorageConfig.CloudStorageProperties.class})
public class CloudStorageConfig {

    @Bean
    public Bucket bucket(CloudStorageProperties properties) throws IOException {
        var credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(properties.credentials().getBytes(UTF_8)));
        var storage = StorageOptions.newBuilder()
                                    .setCredentials(credentials)
                                    .build()
                                    .getService();

        return storage.get(properties.bucket());
    }

    @Bean
    public StorageService cloudStorageService(Bucket bucket) {
        return new CloudStorageService(bucket);
    }

    @ConfigurationProperties(prefix = "cloud-storage")
    public record CloudStorageProperties(String credentials,
                                         String bucket) {

    }

}

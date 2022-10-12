package lt.aigen.geles;

import lt.aigen.geles.uploadingfiles.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class GelesApplication {
    public static void main(String[] args) {
        SpringApplication.run(GelesApplication.class, args);
    }
}

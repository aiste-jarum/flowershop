package lt.aigen.geles;

import lt.aigen.geles.uploadingfiles.FileSystemStorageService;
import lt.aigen.geles.uploadingfiles.StorageService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@Profile("development")
public class DevConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedOrigins("http://localhost:3000")
                .allowCredentials(true);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper;
    }

    @Bean
    public StorageService storageService(ApplicationContext applicationContext,
                                         Environment environment, FileSystemStorageService fileSystemStorageService) {
        String serviceName = environment.getProperty("storage.service.name");

        if (Arrays.asList(applicationContext.getBeanDefinitionNames()).contains(serviceName)) {
            return applicationContext.getBean(serviceName, StorageService.class);
        } else {
            return fileSystemStorageService;
        }
    }
}
package lt.aigen.geles.controller;

import lt.aigen.geles.annotations.Authorized;
import lt.aigen.geles.models.Configuration;
import lt.aigen.geles.models.dto.ConfigurationDTO;
import lt.aigen.geles.repositories.ConfigurationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/config")
public class ConfigurationController {
    private final ConfigurationRepository configRepository;
    private final ModelMapper modelMapper;

    public ConfigurationController(ConfigurationRepository configRepository, ModelMapper modelMapper) {
        this.configRepository = configRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/") // /config/?q=key
    public List<ConfigurationDTO> getConfiguration(@RequestParam Optional<String> q) {
        return configRepository.findConfigsByKey(q.orElse(""))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Authorized(admin = true)
    @PutMapping("/")
    public ResponseEntity<ConfigurationDTO> changeConfiguration(@RequestBody @Validated ConfigurationDTO configDTO) {

        var configOpt = configRepository.findConfigByKey(configDTO.getKey());
        if (configOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        var config = configOpt.get();
        config.setValue(configDTO.getValue());
        configRepository.save(config);
        return ResponseEntity.ok(convertToDTO(config));
    }

    private ConfigurationDTO convertToDTO(Configuration config) {
        return modelMapper.map(config, ConfigurationDTO.class);
    }

    private Configuration convertFromDTO(ConfigurationDTO configDTO) {
        return modelMapper.map(configDTO, Configuration.class);
    }
}

package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ConfigurationDTO {
    @NotNull
    private String key;
    @NotNull
    private String value;
}


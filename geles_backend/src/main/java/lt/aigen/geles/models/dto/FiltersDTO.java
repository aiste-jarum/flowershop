package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class FiltersDTO {
    private String name;
    private String value;
}

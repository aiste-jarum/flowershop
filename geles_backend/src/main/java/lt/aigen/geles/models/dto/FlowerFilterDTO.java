package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Setter
public class FlowerFilterDTO {
    @NotNull
    private String sort;
    @NotNull
    private String sortType;
    private List<FiltersDTO> filters;
}

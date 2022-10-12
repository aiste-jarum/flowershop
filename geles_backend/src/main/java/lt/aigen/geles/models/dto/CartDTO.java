package lt.aigen.geles.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private List<FlowerInCartDTO> flowersInCart;
}

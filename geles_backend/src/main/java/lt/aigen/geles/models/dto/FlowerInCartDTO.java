package lt.aigen.geles.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.Flower;

@Getter
@Setter
public class FlowerInCartDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Integer amount;

    private Long flowerId;

    private Long cartId;
}

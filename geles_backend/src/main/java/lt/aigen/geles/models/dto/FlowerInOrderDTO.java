package lt.aigen.geles.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.Flower;
import lt.aigen.geles.models.Order;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class FlowerInOrderDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull
    private Integer quantity;
    @NotNull
    private Long flowerId;

}

package lt.aigen.geles.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class FlowerFavoriteAmountDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Double price;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    private Integer daysToExpire;

    private String photo;

    private Integer amount;

    public FlowerFavoriteAmountDTO() {

    }

    public FlowerFavoriteAmountDTO(Object id, Object name, Object price, Object description, Object daysToExpire,
                                   Object photo, Object amount) {
        this.id = (Long)id;
        this.name = (String)name;
        this.price = (Double)price;
        this.description = (String)description;
        this.daysToExpire = (Integer)daysToExpire;
        this.photo = (String)photo;
        this.amount = (Integer) amount;
    }
}

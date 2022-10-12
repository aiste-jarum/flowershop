package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class OrderAddDTO {
    @NotBlank
    private String address;

    @NotBlank
    private String contactPhone;

    @NotNull
    private Long cartId;
}

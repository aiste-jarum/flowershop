package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.Cart;
import org.hibernate.annotations.Type;

import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserAddDTO {

    @NotBlank
    @Size(min = 4, max=32)
    private String username;

    @NotBlank
    @Size(min = 8, max=64)
    private String password;

    // Profile picture
    @Type(type = "text")
    private String photo;

}

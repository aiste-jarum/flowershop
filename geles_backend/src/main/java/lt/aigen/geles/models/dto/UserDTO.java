package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDTO {
    private Long id;
    private String username;

    private String photo;

    private boolean isAdmin;

    private Integer cartId;
}

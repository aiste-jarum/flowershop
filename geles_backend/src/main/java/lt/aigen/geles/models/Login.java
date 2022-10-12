package lt.aigen.geles.models;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class Login {
    @NotBlank
    @Size(min = 4)
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Login() {

    }
}

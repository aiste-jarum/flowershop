package lt.aigen.geles.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.validation.constraints.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Getter @Setter
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "UNIQUE_USERNAME", columnNames = {"username"})
        }
)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(min = 4, max=32)
    private String username;

    @NotBlank
    @Size(min = 8, max=64)
    private String password;

    // Profile picture
    @Type(type = "text")
    private String photo;

    @ManyToMany(mappedBy = "userFavorites")
    private Set<Flower> favoriteFlowers = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartTemplate> cartTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @NotNull
    @Column(columnDefinition = "boolean default false")
    private Boolean isAdmin;

    public User(Long id, String username, String password, String photo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.photo = photo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    public User() {

    }
}

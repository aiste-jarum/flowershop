package lt.aigen.geles.models;

import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.dto.FlowerDTO;
import lt.aigen.geles.models.dto.FlowerFavoriteAmountDTO;
import org.hibernate.annotations.Type;

import javax.validation.constraints.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter @Setter
@NamedNativeQuery(name = "Flower.findAllFavoriteFlowerIds",
        query = "select f.id from flower f left join user_flower uf on f.id = uf.flower_id left join users u on u.id = uf.user_id where u.username = :username", resultSetMapping = "mapIdColumnToList")
@NamedNativeQuery(name = "Flower.findAllFavoriteFlowers",
        query = "select f.* from flower f left join user_flower uf on f.id = uf.flower_id left join users u on u.id = uf.user_id where u.username = :username", resultSetMapping = "mapToFlower")
@NamedNativeQuery(name = "Flower.findAllUsersFavoriteFlowers",
        query = "select f.*, count(f.id) as amount from flower f join user_flower uf on f.id = uf.flower_id group by f.id order by amount desc", resultSetMapping = "mapToFlowerWithAmount")
@SqlResultSetMapping(name="mapIdColumnToList",
        columns = { @ColumnResult(name="id", type=Long.class) }
)
@SqlResultSetMapping(name="mapToFlower",
        classes = @ConstructorResult(
                targetClass=FlowerDTO.class,
                columns= {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "name"),
                        @ColumnResult(name = "price"),
                        @ColumnResult(name = "description"),
                        @ColumnResult(name = "days_to_expire"),
                        @ColumnResult(name = "photo"),
                        @ColumnResult(name = "version"),
                })
)
@SqlResultSetMapping(name="mapToFlowerWithAmount",
        classes = @ConstructorResult(
                targetClass= FlowerFavoriteAmountDTO.class,
                columns= {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "name"),
                        @ColumnResult(name = "price"),
                        @ColumnResult(name = "description"),
                        @ColumnResult(name = "days_to_expire"),
                        @ColumnResult(name = "photo"),
                        @ColumnResult(name = "amount", type = Integer.class),
                })
)
public class Flower implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Double price;

    @NotNull
    @Type(type="text")
    private String description;

    @Type(type="text")
    private String photo;

    @NotNull
    @Min(1)
    private Integer daysToExpire;

    @Version
    @Column(columnDefinition = "integer default 0")
    private Integer version;

    @ManyToMany
    @JoinTable(
        name = "user_flower",
        joinColumns = @JoinColumn(name = "flower_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "flower_id"})
    )
    private Set<User> userFavorites = new HashSet<>();

    public Flower(Long id, String name, Double price, String description, String photo, Integer daysToExpire,
                  Integer version) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.photo = photo;
        this.daysToExpire = daysToExpire;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flower flower = (Flower) o;
        return Objects.equals(id, flower.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Flower() {

    }
}

package lt.aigen.geles.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "Orders")
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String address;

    @NotNull
    private String contactPhone;

    @NotNull
    private Date createdDate;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlowerInOrder> orderProducts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(address, order.address) && Objects.equals(contactPhone, order.contactPhone) && Objects.equals(createdDate, order.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, contactPhone, createdDate);
    }

    @NotNull
    OrderStatus orderStatus;

    @PrePersist
    protected void onCreate() {
        createdDate = new Date();
    }

    @Transient
    public Double getTotalOrderPrice() {
        return getOrderProducts().stream()
                .map(FlowerInOrder::getTotalPrice)
                .reduce(0.0, Double::sum);
    }

    public enum OrderStatus {
        @JsonProperty("UNPAID") UNPAID,
        @JsonProperty("PAID") PAID,
        @JsonProperty("CONFIRMED") CONFIRMED,
        @JsonProperty("DELIVERED") DELIVERED,
        @JsonProperty("CANCELED") CANCELED
    }

    @Version
    @Column(columnDefinition = "integer default 0")
    private Integer version;
}

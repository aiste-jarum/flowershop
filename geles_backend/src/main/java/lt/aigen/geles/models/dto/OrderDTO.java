package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.Order;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderDTO {
    private Long id;

    private Date createdDate;

    private List<FlowerInOrderDTO> orderFlowers;

    private Double totalOrderPrice;

    private String address;

    private String contactPhone;

    private Order.OrderStatus orderStatus;

    private Long userId;
    private Integer version;

}

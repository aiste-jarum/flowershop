package lt.aigen.geles.models.dto;

import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.Order;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderEditDTO {
    private List<FlowerInOrderDTO> orderFlowers;
    private String address;
    private String contactPhone;
    private Integer version;
}

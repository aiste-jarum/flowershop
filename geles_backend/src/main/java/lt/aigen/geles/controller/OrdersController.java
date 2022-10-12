package lt.aigen.geles.controller;

import lt.aigen.geles.annotations.Authorized;
import lt.aigen.geles.components.CurrentUser;
import lt.aigen.geles.models.FlowerInOrder;
import lt.aigen.geles.models.Order;
import lt.aigen.geles.models.User;
import lt.aigen.geles.models.dto.*;
import lt.aigen.geles.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final FlowerInCartRepository flowerInCartRepository;
    private final FlowerInOrderRepository flowerInOrderRepository;
    private final FlowerRepository flowerRepository;
    private final CurrentUser currentUser;
    private final ModelMapper modelMapper;

    public OrdersController(CartRepository cartRepository, OrderRepository orderRepository, FlowerInCartRepository flowerInCartRepository, FlowerInOrderRepository flowerInOrderRepository, FlowerRepository flowerRepository, CurrentUser currentUser, ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.flowerInCartRepository = flowerInCartRepository;
        this.flowerInOrderRepository = flowerInOrderRepository;
        this.flowerRepository = flowerRepository;
        this.currentUser = currentUser;
        this.modelMapper = modelMapper;
        modelMapper.typeMap(Order.class, OrderDTO.class).addMapping((order -> 0.0), OrderDTO::setTotalOrderPrice); //HACK
    }

    @Authorized
    @Transactional
    @PostMapping("/")
    public ResponseEntity<OrderDTO> create(@RequestBody OrderAddDTO orderDTO) {
        var user = currentUser.get();

        Order order = new Order();
        order.setAddress(orderDTO.getAddress());
        order.setContactPhone(orderDTO.getContactPhone());
        order.setOrderProducts(null);
        order.setOrderStatus(Order.OrderStatus.UNPAID);
        order.setUser(user);
        orderRepository.save(order);

        var cart = cartRepository.findById(orderDTO.getCartId());
        if (cart.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<FlowerInOrder> flowersInOrder = new ArrayList<>();
        for (var flowerInCart : cart.get().getFlowersInCart()) {
            var flowerInOrder = new FlowerInOrder();
            flowerInOrder.setFlower(flowerInCart.getFlower());
            flowerInOrder.setOrder(order);
            flowerInOrder.setQuantity(flowerInCart.getAmount());
            flowersInOrder.add(flowerInOrder);
        }

        flowerInOrderRepository.saveAll(flowersInOrder);
        order.setOrderProducts(flowersInOrder); //so it adds it to DTO

        // Empty cart after successful order
        flowerInCartRepository.deleteAll(cart.get().getFlowersInCart());

        return new ResponseEntity<>(convertToDTO(order), HttpStatus.CREATED);
    }

    @Authorized
    @GetMapping("/")
    public ResponseEntity<List<OrderDTO>> getOrders() {
        var user = currentUser.get();
        if (user.getIsAdmin()) { // ;)
            return new ResponseEntity<>(orderRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList()), HttpStatus.OK);
        }
        var orders = orderRepository.findAllByUser(user).stream().map(this::convertToDTO).collect(Collectors.toList());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @Authorized
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        var user = currentUser.get();
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        var order = orderOpt.get();
        if (!user.getIsAdmin() && !doesOrderBelongToUser(order, user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(convertToDTO(order), HttpStatus.OK);
    }

    @Authorized
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOrder(@PathVariable Long id, @RequestBody VersionDTO version) {
        var user = currentUser.get();
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();
        if (!user.getIsAdmin() && !doesOrderBelongToUser(order, user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //check manually because DTO does not map to order
        if (!orderVersionValid(order, version.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        orderRepository.delete(order);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Authorized
    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDTO> payForOrder(@PathVariable Long id, @RequestBody VersionDTO version) {
        var user = currentUser.get();
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();
        if (!user.getIsAdmin() && !doesOrderBelongToUser(order, user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (order.getOrderStatus() != Order.OrderStatus.UNPAID) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        order.setOrderStatus(Order.OrderStatus.PAID);
        //check manually because DTO does not map to order
        if (!orderVersionValid(order, version.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        orderRepository.save(order);
        return new ResponseEntity<>(convertToDTO(order), HttpStatus.OK);
    }

    @Authorized
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id, @RequestBody VersionDTO version) {
        var user = currentUser.get();
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();
        if (!user.getIsAdmin() && !doesOrderBelongToUser(order, user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (order.getOrderStatus().equals(Order.OrderStatus.DELIVERED) ||
                order.getOrderStatus().equals(Order.OrderStatus.CANCELED)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        order.setOrderStatus(Order.OrderStatus.CANCELED);

        //check manually because DTO does not map to order
        if (!orderVersionValid(order, version.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        orderRepository.save(order);
        return new ResponseEntity<>(convertToDTO(order), HttpStatus.OK);
    }

    @Authorized
    @PutMapping("/{id}/edit")
    @Transactional
    public ResponseEntity<OrderDTO> editOrder(@PathVariable Long id, @RequestBody OrderEditDTO orderEditDTO) {
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var user = currentUser.get();
        var order = orderOpt.get();

        if (order.getOrderStatus().equals(Order.OrderStatus.CANCELED) ||
                order.getOrderStatus().equals(Order.OrderStatus.DELIVERED)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        //admin can edit confirmed and paid-for orders
        if (!user.getIsAdmin() && !order.getOrderStatus().equals(Order.OrderStatus.UNPAID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        //check manually because DTO does not map to order exactly
        if (!orderVersionValid(order, orderEditDTO.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }


        var flowerInOrderDTOs = orderEditDTO.getOrderFlowers();
        if (flowerInOrderDTOs.isEmpty()) {
            orderRepository.delete(order);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        var newFlowersInOrder = new ArrayList<FlowerInOrder>();
        for (var flowerInOrder : flowerInOrderDTOs) {
            var f = convertFromDTO(flowerInOrder);
            //TODO: Check if flower exists?
            var flower = flowerRepository.findById(flowerInOrder.getFlowerId());
            f.setFlower(flower.get());
            f.setOrder(order);
            newFlowersInOrder.add(f);
        }

        order.getOrderProducts().clear();
        order.getOrderProducts().addAll(newFlowersInOrder);
        order.setAddress(orderEditDTO.getAddress());
        order.setContactPhone(orderEditDTO.getContactPhone());

        if (!user.getIsAdmin())
            order.setOrderStatus(Order.OrderStatus.UNPAID);

        try {
            flowerInOrderRepository.saveAll(newFlowersInOrder);
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException e) {
            //is this needed as order is checked above?
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(convertToDTO(order), HttpStatus.OK);

    }

    @Authorized(admin = true)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id, @RequestBody VersionDTO version) {
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();
        if (!orderVersionValid(order, version.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!order.getOrderStatus().equals(Order.OrderStatus.UNPAID) &&
                !order.getOrderStatus().equals(Order.OrderStatus.PAID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


        order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
        return new ResponseEntity<>(convertToDTO(order), HttpStatus.OK);
    }

    @Authorized(admin = true)
    @PostMapping("/{id}/confirmDelivery")
    public ResponseEntity<?> confirmOrderDelivered(@PathVariable Long id, @RequestBody VersionDTO version) {
        var orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        var order = orderOpt.get();
        if (!orderVersionValid(order, version.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!order.getOrderStatus().equals(Order.OrderStatus.CONFIRMED)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        order.setOrderStatus(Order.OrderStatus.DELIVERED);
        orderRepository.save(order);
        return new ResponseEntity<>(convertToDTO(order), HttpStatus.OK);
    }

    private OrderDTO convertToDTO(Order order) {
        var r = modelMapper.map(order, OrderDTO.class);
        r.setTotalOrderPrice(orderRepository.getOrderPrice(order));
        return r;
    }

    private Order convertFromDTO(OrderDTO orderDTO) {
        return modelMapper.map(orderDTO, Order.class);
    }

    public FlowerInOrderDTO convertToDTO(FlowerInOrder flowerInOrder) {
        return modelMapper.map(flowerInOrder, FlowerInOrderDTO.class);
    }

    private FlowerInOrder convertFromDTO(FlowerInOrderDTO flowerInOrderDTO) {
        return modelMapper.map(flowerInOrderDTO, FlowerInOrder.class);
    }

    private boolean doesOrderBelongToUser(Order order, User user)
    {
        return order != null && user != null && order.getUser().getId().equals(user.getId());
    }

    private boolean orderVersionValid(Order order, Integer version)
    {
        return order != null && version != null && version.equals(order.getVersion());
    }

}

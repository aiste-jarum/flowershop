package lt.aigen.geles.controller;

import lt.aigen.geles.annotations.Authorized;
import lt.aigen.geles.components.CurrentUser;
import lt.aigen.geles.models.Cart;
import lt.aigen.geles.models.FlowerInCart;
import lt.aigen.geles.models.dto.CartDTO;
import lt.aigen.geles.models.dto.FlowerInCartDTO;
import lt.aigen.geles.repositories.CartRepository;
import lt.aigen.geles.repositories.FlowerInCartRepository;
import lt.aigen.geles.repositories.FlowerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/carts")
public class CartController {
    CartRepository cartRepository;
    CurrentUser currentUser;
    FlowerRepository flowerRepository;
    FlowerInCartRepository flowerInCartRepository;
    ModelMapper modelMapper;

    public CartController(CartRepository cartRepository, CurrentUser currentUser, FlowerRepository flowerRepository,
                          FlowerInCartRepository flowerInCartRepository, ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.currentUser = currentUser;
        this.flowerInCartRepository = flowerInCartRepository;
        this.flowerRepository = flowerRepository;
        this.modelMapper = modelMapper;
    }

    @Authorized
    @GetMapping("/") // /carts/10
    public ResponseEntity<CartDTO> getCart() {
        var id = currentUser.get().getCart().getId();
        var cart = cartRepository.findById(id);
        if (cart.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(convertToDTO(cart.get()));
        }
    }

    @Authorized
    @GetMapping("/{id}") // /carts/10
    public ResponseEntity<CartDTO> getCart(@PathVariable Long id) {
        var cart = cartRepository.findById(id);
        if (cart.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(convertToDTO(cart.get()));
        }
    }

    @Authorized
    @PutMapping("/{id}")
    @Transactional
    ResponseEntity<CartDTO> updateCart(@RequestBody @Validated CartDTO cartDTO, @PathVariable Long id) {
        Optional<Cart> oldCart = cartRepository.findById(id);
        if(oldCart.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Cart newCart = oldCart.get();

        if(newCart.getUser().getId() != currentUser.get().getId()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        for(var f: newCart.getFlowersInCart()){
            flowerInCartRepository.delete(f);
        }

        List<FlowerInCart> flowersInCart = new ArrayList<>();
        for (var f : cartDTO.getFlowersInCart()) {
            var flower = flowerRepository.findById(f.getFlowerId());
            FlowerInCart flowerInCart = convertFromDTO(f);
            flowerInCart.setFlower(flower.get());
            flowerInCart.setCart(newCart);
            flowerInCart.setCartTemplate(null);
            flowerInCartRepository.save(flowerInCart);
            flowersInCart.add(flowerInCart);
        }

        newCart.setFlowersInCart(flowersInCart);
        return new ResponseEntity<>(convertToDTO(newCart), HttpStatus.OK);
    }

    private CartDTO convertToDTO(Cart cart) { return modelMapper.map(cart, CartDTO.class); }

    private Cart convertFromDTO(CartDTO cartDTO) { return modelMapper.map(cartDTO, Cart.class); }

    private FlowerInCart convertFromDTO(FlowerInCartDTO flowerInCartDTO) { return modelMapper.map(flowerInCartDTO, FlowerInCart.class); }
}
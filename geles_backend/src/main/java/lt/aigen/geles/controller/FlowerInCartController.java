package lt.aigen.geles.controller;

import lt.aigen.geles.annotations.Authorized;
import lt.aigen.geles.models.Cart;
import lt.aigen.geles.models.Flower;
import lt.aigen.geles.models.FlowerInCart;
import lt.aigen.geles.models.dto.FlowerInCartDTO;
import lt.aigen.geles.repositories.CartRepository;
import lt.aigen.geles.repositories.FlowerInCartRepository;
import lt.aigen.geles.repositories.FlowerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/carts/flowers")
public class FlowerInCartController {
    FlowerInCartRepository flowerInCartRepository;
    FlowerRepository flowerRepository;
    CartRepository cartRepository;
    ModelMapper modelMapper;

    public FlowerInCartController(FlowerInCartRepository flowerInCartRepository, FlowerRepository flowerRepository, CartRepository cartRepository, ModelMapper modelMapper) {
        this.flowerInCartRepository = flowerInCartRepository;
        this.flowerRepository = flowerRepository;
        this.cartRepository = cartRepository;
        this.modelMapper = modelMapper;
    }

    @Authorized
    @GetMapping("/{id}")
    public List<FlowerInCartDTO> getFlowersInCart(@PathVariable long id) {
        return flowerInCartRepository.
                findAllByName(id).
                stream().
                map(this::convertToDTO).
                collect(Collectors.toList());
    }

    @Authorized
    @Transactional
    @PostMapping("/")
    public ResponseEntity<FlowerInCartDTO> postFlowerInCart(@RequestBody @Validated FlowerInCartDTO flowerInCartDTO) {
        long flower_id = flowerInCartDTO.getFlowerId();
        long cart_id = flowerInCartDTO.getCartId();

        List<FlowerInCart> flowersInCart = cartRepository.findById(cart_id).get().getFlowersInCart();

        for(var f: flowersInCart){
            int amount = flowerInCartDTO.getAmount();
            amount += f.getAmount();
            if(f.getFlower().getId() == flower_id) {
                flowerInCartDTO.setAmount(amount);
                flowerInCartRepository.deleteById(f.getId());
                break;
            }
        }

        var flowerInCart = convertFromDTO(flowerInCartDTO);
        flowerInCart.setCartTemplate(null);

        Optional<Flower> flower = flowerRepository.findById(flower_id);
        if(flower.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        flowerInCart.setFlower(flower.get());

        Optional<Cart> cart = cartRepository.findById(cart_id);
        if(cart.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        flowerInCart.setCart(cart.get());

        flowerInCartRepository.save(flowerInCart);
        return ResponseEntity.ok(convertToDTO(flowerInCart));
    }

    @Authorized
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id)
    {
        if (!flowerInCartRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        flowerInCartRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    private FlowerInCartDTO convertToDTO(FlowerInCart flowerInCart) { return modelMapper.map(flowerInCart, FlowerInCartDTO.class); }

    private FlowerInCart convertFromDTO(FlowerInCartDTO flowerInCartDTO) { return modelMapper.map(flowerInCartDTO, FlowerInCart.class); }
}

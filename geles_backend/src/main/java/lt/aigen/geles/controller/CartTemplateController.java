package lt.aigen.geles.controller;

import lt.aigen.geles.annotations.Authorized;
import lt.aigen.geles.components.CurrentUser;
import lt.aigen.geles.models.*;
import lt.aigen.geles.models.dto.CartTemplateDTO;
import lt.aigen.geles.models.dto.FlowerInCartDTO;
import lt.aigen.geles.repositories.CartTemplateRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/templates")
public class CartTemplateController {
    CartTemplateRepository cartTemplateRepository;
    FlowerInCartRepository flowerInCartRepository;
    FlowerRepository flowerRepository;
    ModelMapper modelMapper;
    CurrentUser currentUser;

    public CartTemplateController(CartTemplateRepository cartTemplateRepository, FlowerInCartRepository flowerInCartRepository,
                                  FlowerRepository flowerRepository, ModelMapper modelMapper, CurrentUser currentUser) {
        this.cartTemplateRepository = cartTemplateRepository;
        this.flowerInCartRepository = flowerInCartRepository;
        this.flowerRepository = flowerRepository;
        this.modelMapper = modelMapper;
        this.currentUser = currentUser;
    }

    @Authorized
    @GetMapping("/")
    public List<CartTemplateDTO> getCartTemplate(@CookieValue("user") String userCookie) {
        return cartTemplateRepository.
                findAllByUser(currentUser.get().getId()).
                stream().
                map(this::convertToDTO).
                collect(Collectors.toList());
    }

    @Authorized
    @Transactional
    @PostMapping("/")
    public ResponseEntity<CartTemplateDTO> postCartTemplate(@RequestBody @Validated CartTemplateDTO cartTemplateDTO){

        List<FlowerInCartDTO> flowerInCartDTOs = cartTemplateDTO.getFlowersInCart();
        List<FlowerInCart> flowersInCart = new ArrayList<>();

        CartTemplate cartTemplate = convertFromDTO(cartTemplateDTO);
        cartTemplate.setUser(currentUser.get());
        CartTemplate savedTemplate = cartTemplateRepository.save(cartTemplate);

        for(var f: flowerInCartDTOs){
            var flower = flowerRepository.findById(f.getFlowerId());
            FlowerInCart flowerInCart = convertFromDTO(f);
            flowerInCart.setFlower(flower.get());
            flowerInCart.setCart(null);
            flowerInCart.setCartTemplate(savedTemplate);
            flowersInCart.add(flowerInCartRepository.save(flowerInCart));
        }

        cartTemplate.setFlowersInCart(flowersInCart);
        cartTemplateRepository.save(cartTemplate);

        return new ResponseEntity<>(convertToDTO(cartTemplate), HttpStatus.OK);
    }

    @Authorized
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id)
    {
        if (!cartTemplateRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<CartTemplate> cartTemplate = cartTemplateRepository.findById(id);
        if(cartTemplate.get().getUser().getId() != currentUser.get().getId()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<FlowerInCart> flowersInTemplate = cartTemplate.get().getFlowersInCart();

        for(var f: flowersInTemplate){
            flowerInCartRepository.delete(f);
        }

        cartTemplateRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private CartTemplateDTO convertToDTO(CartTemplate cartTemplate) { return modelMapper.map(cartTemplate, CartTemplateDTO.class); }

    private CartTemplate convertFromDTO(CartTemplateDTO cartTemplateDTO) { return modelMapper.map(cartTemplateDTO, CartTemplate.class); }

    private FlowerInCart convertFromDTO(FlowerInCartDTO flowerInCartDTO) { return modelMapper.map(flowerInCartDTO, FlowerInCart.class); }
}

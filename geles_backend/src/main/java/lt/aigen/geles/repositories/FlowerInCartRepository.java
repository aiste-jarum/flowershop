package lt.aigen.geles.repositories;

import lt.aigen.geles.models.Flower;
import lt.aigen.geles.models.FlowerInCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowerInCartRepository extends JpaRepository<FlowerInCart, Long> {
    @Query("from FlowerInCart f where f.cart.id = :cartId")
    List<FlowerInCart> findAllByName(Long cartId);
}

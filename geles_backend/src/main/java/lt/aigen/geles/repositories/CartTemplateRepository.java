package lt.aigen.geles.repositories;

import lt.aigen.geles.models.CartTemplate;
import lt.aigen.geles.models.FlowerInCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartTemplateRepository extends JpaRepository<CartTemplate, Long> {
    @Query("from CartTemplate f where f.user.id = :userId")
    List<CartTemplate> findAllByUser(Long userId);
}

package lt.aigen.geles.repositories;

import lt.aigen.geles.models.FlowerInOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowerInOrderRepository extends JpaRepository<FlowerInOrder, Long> {
}

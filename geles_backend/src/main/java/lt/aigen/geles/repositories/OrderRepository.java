package lt.aigen.geles.repositories;

import lt.aigen.geles.models.Order;
import lt.aigen.geles.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedNativeQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    @Query("select SUM(f.price * fo.quantity) from FlowerInOrder fo,  Flower f  where fo.order.id = :#{#order.id} AND f.id = fo.flower.id")
    public Double getOrderPrice(@Param("order") Order order);

    @Query("from Order o where o.user.id = :#{#user.id}")
    public List<Order> findAllByUser(@Param("user") User user);
}

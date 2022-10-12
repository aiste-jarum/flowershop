package lt.aigen.geles.repositories;

import lt.aigen.geles.models.Flower;
import lt.aigen.geles.models.User;
import lt.aigen.geles.models.dto.FlowerDTO;
import lt.aigen.geles.models.dto.FlowerFavoriteAmountDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

@Repository
public interface FlowerRepository extends JpaRepository<Flower, Long> {
    List<Flower> findAllByNameContainsIgnoreCase(String name);

    @Query(nativeQuery=true)
    List<Long> findAllFavoriteFlowerIds(String username);

    @Query(nativeQuery = true)
    List<FlowerDTO> findAllFavoriteFlowers(String username);

    @Query("from Flower f where current_date + f.daysToExpire <= :searchDate")
    List<Flower> findExpiredBeforeDate(@Param("searchDate") Date searchDate);

    List<Flower> findAllByPriceBetweenAndNameContainingIgnoreCase (
            Pageable pageable, Double minPrice, Double maxPrice, String name);

    @Query(nativeQuery = true)
    List<FlowerFavoriteAmountDTO> findAllUsersFavoriteFlowers();
}

package lt.aigen.geles.repositories;

import lt.aigen.geles.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    @Query("from User user where user.username = :username")
    Optional<User> findByUsername(String username);
}
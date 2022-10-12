package lt.aigen.geles.repositories;

import lt.aigen.geles.models.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByName(String name);

    void deleteByName(String name);
}

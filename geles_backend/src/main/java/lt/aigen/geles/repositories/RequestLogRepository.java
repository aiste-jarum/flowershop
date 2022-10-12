package lt.aigen.geles.repositories;

import lt.aigen.geles.models.Flower;
import lt.aigen.geles.models.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

}

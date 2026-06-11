package eshop.com.eshoporderservice.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = "SELECT * FROM outbox WHERE published = false FOR UPDATE", nativeQuery = true)
    List<OutboxEvent> findUnpublishedForUpdate();
}

package eshop.com.eshoporderservice.order.repository;

import eshop.com.eshoporderservice.order.model.OrderCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderCommandRepository extends JpaRepository<OrderCommand, UUID> {
}

package eshop.com.eshopinventoryservice.repository;

import eshop.com.eshopinventoryservice.model.ProcessedOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedOrderEventRepository extends JpaRepository<ProcessedOrderEvent, UUID> {
}

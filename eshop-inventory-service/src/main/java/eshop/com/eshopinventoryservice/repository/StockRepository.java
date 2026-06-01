package eshop.com.eshopinventoryservice.repository;

import eshop.com.eshopinventoryservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    Optional<Stock> findByProductId(String productId);
}

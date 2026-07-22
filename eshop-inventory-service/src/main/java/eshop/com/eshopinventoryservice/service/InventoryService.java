package eshop.com.eshopinventoryservice.service;

import eshop.com.eshopinventoryservice.model.ProcessedOrderEvent;
import eshop.com.eshopinventoryservice.model.Stock;
import eshop.com.eshopinventoryservice.repository.ProcessedOrderEventRepository;
import eshop.com.eshopinventoryservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockRepository stockRepository;
    private final ProcessedOrderEventRepository processedOrderEventRepository;

    @Transactional
    public ReservationOutcome reserveStock(UUID orderId, String productId, int quantity) {
        if (processedOrderEventRepository.existsById(orderId)) {
            return ReservationOutcome.ALREADY_PROCESSED;
        }

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No stock record for product: " + productId));

        processedOrderEventRepository.save(new ProcessedOrderEvent(orderId));

        if (stock.getQuantity() < quantity) {
            return ReservationOutcome.INSUFFICIENT;
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
        return ReservationOutcome.RESERVED;
    }
}

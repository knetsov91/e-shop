package eshop.com.eshopinventoryservice.service;

import eshop.com.eshopinventoryservice.model.Stock;
import eshop.com.eshopinventoryservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockRepository stockRepository;

    @Transactional
    public boolean reserveStock(String productId, int quantity) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No stock record for product: " + productId));

        if (stock.getQuantity() < quantity) {
            return false;
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
        return true;
    }
}

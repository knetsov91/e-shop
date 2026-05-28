package eshop.com.eshopinventoryservice.service;

import eshop.com.eshopinventoryservice.model.Stock;
import eshop.com.eshopinventoryservice.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveStock_whenStockIsSufficient_thenDecrementsQuantityAndReturnsTrue() {
        Stock stock = new Stock();
        stock.setProductId("product-1");
        stock.setQuantity(10);

        when(stockRepository.findByProductId("product-1")).thenReturn(Optional.of(stock));

        boolean result = inventoryService.reserveStock("product-1", 3);

        assertThat(result).isTrue();
        assertThat(stock.getQuantity()).isEqualTo(7);
        verify(stockRepository).save(stock);
    }
}

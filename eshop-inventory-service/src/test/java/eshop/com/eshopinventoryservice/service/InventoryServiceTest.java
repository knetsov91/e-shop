package eshop.com.eshopinventoryservice.service;

import eshop.com.eshopinventoryservice.model.Stock;
import eshop.com.eshopinventoryservice.repository.ProcessedOrderEventRepository;
import eshop.com.eshopinventoryservice.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProcessedOrderEventRepository processedOrderEventRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveStock_whenStockIsSufficient_thenDecrementsQuantityAndReturnsReserved() {
        UUID orderId = UUID.randomUUID();
        Stock stock = new Stock();
        stock.setProductId("product-1");
        stock.setQuantity(10);

        when(processedOrderEventRepository.existsById(orderId)).thenReturn(false);
        when(stockRepository.findByProductId("product-1")).thenReturn(Optional.of(stock));

        ReservationOutcome result = inventoryService.reserveStock(orderId, "product-1", 3);

        assertThat(result).isEqualTo(ReservationOutcome.RESERVED);
        assertThat(stock.getQuantity()).isEqualTo(7);
        verify(stockRepository).save(stock);
    }

    @Test
    void reserveStock_whenStockIsInsufficient_thenReturnsInsufficient() {
        UUID orderId = UUID.randomUUID();
        Stock stock = new Stock();
        stock.setProductId("product-1");
        stock.setQuantity(2);

        when(processedOrderEventRepository.existsById(orderId)).thenReturn(false);
        when(stockRepository.findByProductId("product-1")).thenReturn(Optional.of(stock));

        ReservationOutcome result = inventoryService.reserveStock(orderId, "product-1", 5);

        assertThat(result).isEqualTo(ReservationOutcome.INSUFFICIENT);
        verify(stockRepository, never()).save(any());
    }

    @Test
    void reserveStock_whenOrderAlreadyProcessed_thenReturnsAlreadyProcessedAndSkipsStock() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderEventRepository.existsById(orderId)).thenReturn(true);

        ReservationOutcome result = inventoryService.reserveStock(orderId, "product-1", 3);

        assertThat(result).isEqualTo(ReservationOutcome.ALREADY_PROCESSED);
        verifyNoInteractions(stockRepository);
    }

    @Test
    void reserveStock_whenProductNotFound_thenThrowsIllegalArgumentException() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderEventRepository.existsById(orderId)).thenReturn(false);
        when(stockRepository.findByProductId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.reserveStock(orderId, "unknown", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }
}

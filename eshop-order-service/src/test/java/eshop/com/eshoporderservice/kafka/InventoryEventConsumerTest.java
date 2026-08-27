package eshop.com.eshoporderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryEventConsumerTest {

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private InventoryEventConsumer inventoryEventConsumer;

    @Test
    void consume_whenStatusIsReserved_thenUpdatesOrderToConfirmed() throws Exception {
        UUID orderId = UUID.randomUUID();
        String message = objectMapper.writeValueAsString(
                new eshop.com.eshoporderservice.event.InventoryEvent(orderId, "product-1", "RESERVED")
        );

        OrderCommand order = new OrderCommand();
        order.setId(orderId);
        order.setStatus(OrderStatus.AWAITING_INVENTORY);

        when(orderCommandRepository.findById(orderId)).thenReturn(Optional.of(order));

        inventoryEventConsumer.consume(message);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void consume_whenStatusIsInsufficient_thenUpdatesOrderToFailed() throws Exception {
        UUID orderId = UUID.randomUUID();
        String message = objectMapper.writeValueAsString(
                new eshop.com.eshoporderservice.event.InventoryEvent(orderId, "product-1", "INSUFFICIENT")
        );

        OrderCommand order = new OrderCommand();
        order.setId(orderId);
        order.setStatus(OrderStatus.AWAITING_INVENTORY);

        when(orderCommandRepository.findById(orderId)).thenReturn(Optional.of(order));

        inventoryEventConsumer.consume(message);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    void consume_whenOrderNotFound_thenDoesNotSave() throws Exception {
        UUID orderId = UUID.randomUUID();
        String message = objectMapper.writeValueAsString(
                new eshop.com.eshoporderservice.event.InventoryEvent(orderId, "product-1", "RESERVED")
        );

        when(orderCommandRepository.findById(orderId)).thenReturn(Optional.empty());

        inventoryEventConsumer.consume(message);

        verify(orderCommandRepository, never()).save(any());
    }
}

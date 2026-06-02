package eshop.com.eshopinventoryservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshopinventoryservice.event.OrderCreatedEvent;
import eshop.com.eshopinventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void consume_whenStockIsReserved_thenPublishesReservedEventToInventoryEvents() throws Exception {
        UUID orderId = UUID.randomUUID();
        String message = objectMapper.writeValueAsString(
                new OrderCreatedEvent(orderId, "product-1", 3)
        );

        when(inventoryService.reserveStock("product-1", 3)).thenReturn(true);

        orderEventConsumer.consume(message);

        verify(kafkaTemplate).send(eq("inventory-events"), contains("RESERVED"));
    }
}

package eshop.com.eshoporderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.PaymentEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.outbox.OutboxEvent;
import eshop.com.eshoporderservice.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    @Test
    void consume_whenStatusIsSucceeded_thenUpdatesOrderToAwaitingInventoryAndPublishesOrderCreatedEvent() throws Exception {
        UUID orderId = UUID.randomUUID();
        String message = objectMapper.writeValueAsString(new PaymentEvent(orderId, "SUCCEEDED"));

        OrderCommand order = new OrderCommand();
        order.setId(orderId);
        order.setProduct("Laptop");
        order.setQuantity(2);
        order.setStatus(OrderStatus.PENDING);

        when(orderCommandRepository.findById(orderId)).thenReturn(Optional.of(order));

        paymentEventConsumer.consume(message);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_INVENTORY);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getTopic()).isEqualTo("order-events");
        assertThat(captor.getValue().getPayload()).contains(orderId.toString());
    }
}
